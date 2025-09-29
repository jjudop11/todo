package com.example.todo.controller;

import com.example.todo.domain.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.security.JwtTokenProvider;
import com.example.todo.security.RefreshTokenService;
import com.example.todo.security.EmailTokenService;
import com.example.todo.service.EmailService;
import com.example.todo.dto.auth.RegisterRequest;
import com.example.todo.dto.auth.LoginRequest;
import com.example.todo.dto.auth.RefreshRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final EmailTokenService emailTokenService;
    private final EmailService emailService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService, EmailTokenService emailTokenService, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.emailTokenService = emailTokenService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "username already exists"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "email already exists"));
        }
        // 영소문자 1개 이상, 특수문자 1개 이상, 전체 길이 8자 이상
        String passwordPattern = "^(?=.*[a-z])(?=.*[!@#$%^&*()_+\\\\-\\\\[\\\\]{};':\"\\\\\\\\|,.<>/?]).{8,}$";
        if (!request.getPassword().matches(passwordPattern)) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid password format"));
        }
        var user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .name(request.getName())
                .roles(Set.of("ROLE_USER"))
                .build();
        userRepository.save(user);
        // 이메일 인증 토큰 발송
        String token = emailTokenService.generateAndStore("verify", user.getUsername(), java.time.Duration.ofHours(24));
        String link = "http://localhost:8080/api/auth/verify?token=" + token;
        emailService.send(user.getEmail(), "이메일 인증", "아래 링크를 클릭하여 이메일을 인증하세요: " + link);
        return ResponseEntity.ok(Map.of("message", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authentication.getAuthorities().stream().map(Object::toString).toList());

        String accessToken = jwtTokenProvider.createAccessToken(request.getUsername(), claims, 60 * 60);
        String refreshToken = jwtTokenProvider.createRefreshToken(request.getUsername(), 60L * 60 * 24);

        refreshTokenService.storeRefreshToken(request.getUsername(), refreshToken, Duration.ofHours(24));

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        var username = request.getUsername();
        var stored = refreshTokenService.getRefreshToken(username);
        if (stored.isEmpty() || !stored.get().equals(request.getRefreshToken())) {
            return ResponseEntity.status(401).body(Map.of("message", "invalid refresh token"));
        }

        Map<String, Object> claims = new HashMap<>();
        userRepository.findByUsername(username).ifPresent(u -> claims.put("roles", u.getRoles()));

        String newAccessToken = jwtTokenProvider.createAccessToken(username, claims, 60 * 60);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        var usernameOpt = emailTokenService.consume("verify", token);
        if (usernameOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "invalid token"));
        var user = userRepository.findByUsername(usernameOpt.get()).orElseThrow();
        user = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .passwordHash(user.getPasswordHash())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .verified(true)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "verified"));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<?> resetRequest(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.ok(Map.of("message", "ok"));
        String token = emailTokenService.generateAndStore("reset", username, java.time.Duration.ofHours(1));
        String link = "http://localhost:8080/reset?token=" + token;
        emailService.send(userOpt.get().getEmail(), "비밀번호 재설정", "아래 링크에서 비밀번호를 재설정하세요: " + link);
        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("password");
        var usernameOpt = emailTokenService.consume("reset", token);
        if (usernameOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "invalid token"));
        var user = userRepository.findByUsername(usernameOpt.get()).orElseThrow();
        user = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .passwordHash(passwordEncoder.encode(newPassword))
                .email(user.getEmail())
                .name(user.getName())
                .roles(user.getRoles())
                .verified(user.isVerified())
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "reset"));
    }

    
}


