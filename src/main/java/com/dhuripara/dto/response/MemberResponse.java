package com.dhuripara.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MemberResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String firstNameBn;
    private String lastNameBn;
    private String phone;
    private Boolean isOperator;
    private String pin;
    private LocalDate joiningDate;
    private Boolean isActive;

    private Boolean isBlocked;
    private LocalDateTime blockedUntil;
    private Integer failedLoginAttempts;
    private LocalDate dateOfBirth;
    private String aadharNo;
    private String voterNo;
    private String panNo;
    private UUID familyId;
}