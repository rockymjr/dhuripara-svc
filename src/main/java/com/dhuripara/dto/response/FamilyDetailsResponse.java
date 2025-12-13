package com.dhuripara.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDetailsResponse {
    private UUID familyId;
    private String familyHeadName;
    private Boolean isContributionEnabled;
    private LocalDate effectiveFrom;
    private BigDecimal monthlyAmount;
    private String notes;
    
    // Family members
    private List<FamilyMemberInfo> members;
    
    // Family financial summary
    private BigDecimal totalDeposits;
    private BigDecimal totalLoans;
    private BigDecimal totalVdfContributions;
    private BigDecimal totalVdfExpenses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FamilyMemberInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String phone;
        private LocalDate dateOfBirth;
        private String aadharNo;
        private String voterNo;
        private String panNo;
        private BigDecimal totalDeposits;
        private BigDecimal totalLoans;
    }
}

