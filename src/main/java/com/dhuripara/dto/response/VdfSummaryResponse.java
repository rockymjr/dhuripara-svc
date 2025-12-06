package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class VdfSummaryResponse {
    private Integer totalFamilies;
    private Integer activeContributors;
    private BigDecimal totalCollected;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private Integer currentYear;
}