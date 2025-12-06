package com.dhuripara.dto.response;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class VdfMonthlyReportResponse {
    private Integer year;
    private Integer month;
    private String monthName;
    private Integer familiesPaid;
    private Integer familiesPending;
    private BigDecimal totalCollected;
    private BigDecimal totalExpenses;
}
