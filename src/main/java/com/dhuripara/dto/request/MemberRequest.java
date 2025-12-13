package com.dhuripara.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MemberRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String firstNameBn;

    @Size(max = 100)
    private String lastNameBn;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits",
            groups = ValidationGroups.PhoneValidation.class)
    private String phone;

    @Pattern(regexp = "^[0-9]{4}$", message = "PIN must be 4 digits")
    private String pin;

    private LocalDate dateOfBirth;

    @Size(max = 12)
    @Pattern(regexp = "^$|^[0-9]{12}$", message = "Aadhar number must be 12 digits or empty")
    private String aadharNo;

    @Size(max = 20)
    private String voterNo;

    @Size(max = 10)
    @Pattern(regexp = "^$|^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN must be in format ABCDE1234F or empty")
    private String panNo;

    private UUID familyId; // Link to VdfFamilyConfig

    private Boolean isOperator = false;
    private String role = "MEMBER";

    public interface ValidationGroups {
        interface PhoneValidation {}
    }
}