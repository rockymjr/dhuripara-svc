package com.dhuripara.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class VdfBulkContributionRequest {
    private UUID familyConfigId;
    private Integer year;
    // List of monthly contributions
    private List<MonthlyContributionInput> contributions;
    private LocalDate paymentDate;
    private String notes;

    @Data
    public static class MonthlyContributionInput {
        private Integer month;  // 1-12
        private BigDecimal amount;
    }
}
