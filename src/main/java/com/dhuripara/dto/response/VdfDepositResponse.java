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
    private String sourceName;
    private String sourceNameBn;
    private String memberName;
    private UUID memberId; // Added: Member ID for filtering
    private MemberResponse member; // Added: Full member object with firstName, lastName
    private Integer year;
    private String notes;
    private UUID categoryId;
    private String categoryName;
    private String categoryNameBn;
}
