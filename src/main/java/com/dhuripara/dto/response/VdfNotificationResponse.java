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
public class VdfNotificationResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private String title;
    private String titleBn;
    private String message;
    private String messageBn;
    private String type;
    private UUID relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

