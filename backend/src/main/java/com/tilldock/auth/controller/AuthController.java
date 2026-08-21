package com.tilldock.auth.controller;

import com.tilldock.auth.dto.AuthResponse;
import com.tilldock.auth.dto.LoginRequest;
import com.tilldock.auth.dto.MerchantDto;
import com.tilldock.auth.dto.SignupRequest;
import com.tilldock.auth.entity.Merchant;
import com.tilldock.auth.security.AuthenticatedMerchant;
import com.tilldock.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(201).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/me")
    public ResponseEntity<MerchantDto> me(@AuthenticationPrincipal AuthenticatedMerchant principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        Merchant merchant = authService.me(principal.id());
        return ResponseEntity.ok(MerchantDto.from(merchant));
    }
}