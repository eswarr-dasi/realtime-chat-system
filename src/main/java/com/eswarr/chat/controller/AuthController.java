package com.eswarr.chat.controller;

import com.eswarr.chat.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Demo authentication — issues a JWT for a display name with no password.
 * In production this would validate against a real user store (OAuth, etc).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String userName = body.getOrDefault("userName", "Guest" + System.currentTimeMillis() % 1000);
        String token = jwtService.generateToken(userName);
        return ResponseEntity.ok(Map.of("token", token, "userName", userName));
    }
}
