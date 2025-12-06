package com.dhuripara.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class VdfBalanceSummaryResponse {
    private BigDecimal totalContributions;
    private BigDecimal totalExpenses;
    private BigDecimal currentBalance;
}