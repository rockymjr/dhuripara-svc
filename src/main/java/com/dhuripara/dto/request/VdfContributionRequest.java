package com.dhuripara.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class VdfContributionRequest {
    @NotNull(message = "Family ID is required")
    private UUID familyId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotEmpty(message = "At least one month allocation is required")
    private List<MonthAllocationDto> monthAllocations;

    private String notes;

    @Data
    public static class MonthAllocationDto {
        @NotNull
        private String month; // Format: "YYYY-MM"

        @NotNull
        @DecimalMin(value = "0.01")
        private BigDecimal amount;
    }
}