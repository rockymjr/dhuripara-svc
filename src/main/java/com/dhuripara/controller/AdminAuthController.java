package com.dhuripara.controller;

import com.dhuripara.dto.request.MemberLoginRequest;
import com.dhuripara.dto.response.MemberAuthResponse;
import com.dhuripara.service.MemberAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminAuthController {

    private final MemberAuthService memberAuthService;

    @PostMapping("/login")
    public ResponseEntity<MemberAuthResponse> login(@Valid @RequestBody MemberLoginRequest request, HttpServletRequest httpRequest) {
        MemberAuthResponse response = memberAuthService.authenticate(request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
