package com.dhuripara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDocumentResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private UUID documentCategoryId;
    private String documentCategoryName;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String downloadUrl; // Pre-signed URL for download
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private String notes;
}

