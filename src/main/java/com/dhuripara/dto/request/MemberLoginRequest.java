package com.dhuripara.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MemberLoginRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    // PIN will be used by Member and Operator; Admins must login using password
    private String pin;

    // Password will be used by Admin users; stored in DB as BCrypt hashed
    private String password;
}