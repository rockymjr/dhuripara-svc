package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VdfSummaryResponse {
    private Integer totalFamilies;
    private Integer activeFamilies;
    private BigDecimal totalContributions;
    private BigDecimal totalDeposits;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private String currentYear;
}