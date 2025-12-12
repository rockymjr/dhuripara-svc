package com.dhuripara.service;

import com.dhuripara.model.UserSession;
import com.dhuripara.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserSessionRepository sessionRepository;

    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    public String getDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "Unknown";
        }
        
        // Simple device detection
        if (userAgent.contains("Mobile")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    @Transactional
    public UserSession createSession(String userType, UUID userId, String username, String token, HttpServletRequest request) {
        UserSession session = new UserSession();
        session.setUserType(userType);
        session.setUserId(userId);
        session.setUsername(username);
        session.setToken(token);
        session.setIpAddress(getClientIpAddress(request));
        session.setUserAgent(request.getHeader("User-Agent"));
        session.setDeviceInfo(getDeviceInfo(request));
        session.setIsActive(true);
        
        return sessionRepository.save(session);
    }

    @Transactional
    public void updateLastActivity(String token) {
        sessionRepository.updateLastActivity(token, LocalDateTime.now());
    }

    @Transactional
    public void deactivateSession(String token) {
        sessionRepository.deactivateSessionByToken(token);
    }

    @Transactional
    public void deactivateAllSessionsForUser(UUID userId, String userType) {
        sessionRepository.deactivateAllSessionsForUser(userId, userType);
    }

    public Optional<UserSession> getSessionByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public List<UserSession> getActiveSessionsForUser(UUID userId, String userType) {
        return sessionRepository.findByUserIdAndUserTypeAndIsActiveTrue(userId, userType);
    }

    public List<UserSession> getAllActiveSessions() {
        return sessionRepository.findByIsActiveTrueOrderByLastActivityDesc();
    }

    public Long countActiveSessionsForUser(UUID userId) {
        return sessionRepository.countActiveSessionsForUser(userId);
    }

    public Long countActiveUsersByType(String userType) {
        return sessionRepository.countActiveUsersByType(userType);
    }

    public Optional<UserSession> getSessionById(UUID sessionId) {
        return sessionRepository.findById(sessionId);
    }
}

