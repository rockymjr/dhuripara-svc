package com.dhuripara.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class VdfExemptionRequest {
    @NotNull(message = "Family ID is required")
    private UUID familyId;

    @NotBlank(message = "Month year is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Month year must be in format YYYY-MM")
    private String monthYear;

    @NotBlank(message = "Reason is required")
    private String reason;
}