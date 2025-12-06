package com.dhuripara.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class VdfDepositResponse {
    private UUID id;
    private LocalDate depositDate;
    private BigDecimal amount;
    private String sourceType;
    private String sourceName;
    private String memberName;
    private Integer year;
    private String notes;
}
