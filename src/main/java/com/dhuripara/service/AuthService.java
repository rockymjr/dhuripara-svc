package com.dhuripara.service;

import com.dhuripara.dto.request.LoginRequest;
import com.dhuripara.dto.response.AuthResponse;
import com.dhuripara.exception.AuthenticationException;
import com.dhuripara.model.AdminUser;
import com.dhuripara.repository.AdminUserRepository;
import com.dhuripara.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    @Transactional
    public AuthResponse authenticate(LoginRequest request, HttpServletRequest httpRequest) {
        AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(admin.getUsername());

        // Create session
        sessionService.createSession("ADMIN", admin.getId(), admin.getUsername(), token, httpRequest);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(admin.getUsername());
        response.setExpiresIn(null); // Never expires

        return response;
    }
}