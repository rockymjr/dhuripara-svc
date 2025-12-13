package com.dhuripara.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class DocumentUploadRequest {
    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Document category ID is required")
    private UUID documentCategoryId;

    @NotNull(message = "File is required")
    private MultipartFile file;

    private String notes;
}

