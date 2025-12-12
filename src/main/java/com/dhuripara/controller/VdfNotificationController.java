package com.dhuripara.controller;

import com.dhuripara.dto.response.VdfNotificationResponse;
import com.dhuripara.service.VdfNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/member/vdf/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VdfNotificationController {

    private final VdfNotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<VdfNotificationResponse>> getNotifications(Authentication authentication) {
        UUID memberId = extractMemberId(authentication);
        List<VdfNotificationResponse> notifications = notificationService.getNotificationsForMember(memberId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<VdfNotificationResponse>> getUnreadNotifications(Authentication authentication) {
        UUID memberId = extractMemberId(authentication);
        List<VdfNotificationResponse> notifications = notificationService.getUnreadNotificationsForMember(memberId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, Authentication authentication) {
        UUID memberId = extractMemberId(authentication);
        notificationService.markAsRead(id, memberId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UUID memberId = extractMemberId(authentication);
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id, Authentication authentication) {
        UUID memberId = extractMemberId(authentication);
        notificationService.deleteNotification(id, memberId);
        return ResponseEntity.ok().build();
    }

    private UUID extractMemberId(Authentication authentication) {
        String username = authentication.getName();
        if (username.startsWith("MEMBER_")) {
            return UUID.fromString(username.replace("MEMBER_", ""));
        }
        throw new IllegalArgumentException("Invalid authentication for member");
    }
}

