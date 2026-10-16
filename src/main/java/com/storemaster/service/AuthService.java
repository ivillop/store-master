package com.storemaster.service;

import com.storemaster.common.ApiException;
import com.storemaster.dto.Dtos.LoginRequest;
import com.storemaster.dto.Dtos.TokenResponse;
import com.storemaster.repository.AppUserRepository;
import com.storemaster.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        var user = users.findByUsername(request.username())
                .filter(u -> u.isEnabled())
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> {
                    log.warn("Failed login attempt for username '{}'", request.username().replaceAll("[\r\n]", "_"));
                    return ApiException.unauthorized("Invalid username or password");
                });
        return new TokenResponse(jwt.generate(user.getUsername(), user.getRole()), "Bearer", jwt.expiresInSeconds());
    }
}
