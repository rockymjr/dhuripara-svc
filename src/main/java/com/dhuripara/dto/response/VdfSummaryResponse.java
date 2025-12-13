package com.dhuripara.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class VdfSummaryResponse {
    private Integer totalFamilies;
    private Integer activeContributors;
    private BigDecimal totalCollected;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
    private Integer currentYear;
    private Map<String, BigDecimal> categoryWiseDeposits; // Category name -> total amount
    private Map<String, BigDecimal> categoryWiseExpenses; // Category name -> total amount
    private Map<String, BigDecimal> categoryWiseDepositsBn; // Bengali category name -> total amount
    private Map<String, BigDecimal> categoryWiseExpensesBn; // Bengali category name -> total amount
}