package com.example.todo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    // 웹푸시 구독 저장 스텁 (실서비스는 DB/Redis 저장 + VAPID 키 필요)
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, Object> subscription, Authentication authentication) {
        // TODO: persist subscription by user
        return ResponseEntity.ok(Map.of("message", "subscribed"));
    }

    // 테스트 전송 스텁
    @PostMapping("/test")
    public ResponseEntity<?> test(Authentication authentication) {
        // TODO: send push via webpush lib
        return ResponseEntity.ok(Map.of("message", "sent"));
    }
}


