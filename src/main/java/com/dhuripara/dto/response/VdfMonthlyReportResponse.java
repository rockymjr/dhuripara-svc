package com.dhuripara.dto.response;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class VdfMonthlyReportResponse {
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal totalContributions;
    private BigDecimal totalExpenses;
    private Integer familiesPaid;
    private Integer familiesPending;
}
