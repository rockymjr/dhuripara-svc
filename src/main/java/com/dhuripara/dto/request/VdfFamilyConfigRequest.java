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

    @NotNull(message = "Contribution enabled flag is required")
    private Boolean isContributionEnabled;

    private LocalDate effectiveFrom;

    @NotNull(message = "Monthly amount is required")
    @DecimalMin(value = "0", message = "Monthly amount must be >= 0")
    private BigDecimal monthlyAmount;

    private String notes;
}