package com.dhuripara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private UUID id;
    private String userType;
    private UUID userId;
    private String username;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private Boolean isActive;
}

