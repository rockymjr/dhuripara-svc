package com.dhuripara.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfFamilyConfigRequest {
    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotBlank(message = "Family head name is required")
    private String familyHeadName;

    private Boolean isContributionEnabled = false;

    private LocalDate effectiveFrom;

    @DecimalMin(value = "0.01", message = "Monthly amount must be greater than 0")
    private BigDecimal monthlyAmount = new BigDecimal("20.00");

    private String notes;
}