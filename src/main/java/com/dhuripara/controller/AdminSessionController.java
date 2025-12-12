package com.dhuripara.controller;

import com.dhuripara.dto.response.SessionResponse;
import com.dhuripara.model.UserSession;
import com.dhuripara.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getAllActiveSessions() {
        List<UserSession> sessions = sessionService.getAllActiveSessions();
        List<SessionResponse> responses = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionResponse>> getSessionsForUser(
            @PathVariable UUID userId,
            @RequestParam String userType) {
        List<UserSession> sessions = sessionService.getActiveSessionsForUser(userId, userType);
        List<SessionResponse> responses = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats")
    public ResponseEntity<SessionStatsResponse> getSessionStats() {
        Long activeAdmins = sessionService.countActiveUsersByType("ADMIN");
        Long activeMembers = sessionService.countActiveUsersByType("MEMBER");
        
        SessionStatsResponse stats = new SessionStatsResponse();
        stats.setActiveAdminSessions(activeAdmins);
        stats.setActiveMemberSessions(activeMembers);
        stats.setTotalActiveSessions(activeAdmins + activeMembers);
        
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> forceLogout(@PathVariable UUID sessionId) {
        UserSession session = sessionService.getSessionById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        
        sessionService.deactivateSession(session.getToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/token/{token}")
    public ResponseEntity<Void> forceLogoutByToken(@PathVariable String token) {
        sessionService.deactivateSession(token);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> forceLogoutAllForUser(
            @PathVariable UUID userId,
            @RequestParam String userType) {
        sessionService.deactivateAllSessionsForUser(userId, userType);
        return ResponseEntity.ok().build();
    }

    private SessionResponse convertToResponse(UserSession session) {
        return SessionResponse.builder()
                .id(session.getId())
                .userType(session.getUserType())
                .userId(session.getUserId())
                .username(session.getUsername())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceInfo(session.getDeviceInfo())
                .loginTime(session.getLoginTime())
                .lastActivity(session.getLastActivity())
                .isActive(session.getIsActive())
                .build();
    }


    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SessionStatsResponse {
        private Long activeAdminSessions;
        private Long activeMemberSessions;
        private Long totalActiveSessions;
    }
}

