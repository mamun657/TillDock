package com.tilldock.auth.service;

import com.tilldock.auth.dto.AuthResponse;
import com.tilldock.auth.dto.LoginRequest;
import com.tilldock.auth.dto.MerchantDto;
import com.tilldock.auth.dto.SignupRequest;
import com.tilldock.auth.entity.Merchant;
import com.tilldock.auth.entity.MerchantStatus;
import com.tilldock.auth.repository.MerchantRepository;
import com.tilldock.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final MerchantRepository merchants;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(MerchantRepository merchants,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.merchants = merchants;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest req) {
        String email = normaliseEmail(req.getEmail());
        if (merchants.existsByEmailIgnoreCase(email)) {
            throw new AuthExceptions.EmailAlreadyRegisteredException();
        }
        Merchant m = new Merchant();
        m.setName(req.getName().trim());
        m.setBusinessName(req.getBusinessName().trim());
        m.setEmail(email);
        m.setPhone(req.getPhone().trim());
        m.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        m.setStatus(MerchantStatus.ACTIVE);
        merchants.save(m);
        return issueToken(m);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = normaliseEmail(req.getEmail());
        Merchant m = merchants.findByEmailIgnoreCase(email)
                .orElseThrow(AuthExceptions.InvalidCredentialsException::new);
        if (!passwordEncoder.matches(req.getPassword(), m.getPasswordHash())) {
            throw new AuthExceptions.InvalidCredentialsException();
        }
        if (m.getStatus() != MerchantStatus.ACTIVE) {
            throw new AuthExceptions.AccountNotActiveException();
        }
        m.setLastLoginAt(OffsetDateTime.now());
        merchants.save(m);
        return issueToken(m);
    }

    @Transactional(readOnly = true)
    public Merchant me(UUID id) {
        return merchants.findById(id)
                .orElseThrow(AuthExceptions.MerchantNotFoundException::new);
    }

    private AuthResponse issueToken(Merchant m) {
        JwtService.IssuedToken issued = jwtService.issue(m.getId(), m.getEmail());
        long expiresIn = Math.max(1, issued.expiresAt().getEpochSecond() - Instant.now().getEpochSecond());
        return new AuthResponse(issued.token(), "Bearer", expiresIn, MerchantDto.from(m));
    }

    private String normaliseEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}