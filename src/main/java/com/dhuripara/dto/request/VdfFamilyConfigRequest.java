package com.dhuripara.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfFamilyConfigRequest {
    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Family size is required")
    @Min(value = 1, message = "Family size must be at least 1")
    private Integer familySize;

    private LocalDate joinedDate;
    private String notes;
}