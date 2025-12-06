package com.graminbank.dto.response;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
