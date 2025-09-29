package com.example.todo.controller;

import com.example.todo.domain.User;
import com.example.todo.repository.UserRepository;
import com.example.todo.security.JwtTokenProvider;
import com.example.todo.security.RefreshTokenService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
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

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
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

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @NotBlank
        private String name;
        @NotBlank
        @jakarta.validation.constraints.Email
        private String email;
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String refreshToken;
    }
}


