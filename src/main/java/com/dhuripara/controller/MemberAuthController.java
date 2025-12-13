package com.dhuripara.controller;

import com.dhuripara.dto.request.ChangePinRequest;
import com.dhuripara.dto.request.MemberLoginRequest;
import com.dhuripara.dto.response.FamilyDetailsResponse;
import com.dhuripara.dto.response.MemberAuthResponse;
import com.dhuripara.dto.response.MemberDashboardResponse;
import com.dhuripara.service.FamilyService;
import com.dhuripara.service.MemberAuthService;
import com.dhuripara.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemberAuthController {

    private final MemberAuthService memberAuthService;
    private final MemberService memberService;
    private final FamilyService familyService;

    @PostMapping("/auth/login")
    public ResponseEntity<MemberAuthResponse> login(@Valid @RequestBody MemberLoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        MemberAuthResponse response = memberAuthService.authenticate(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<MemberDashboardResponse> getDashboard(Authentication authentication) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
        MemberDashboardResponse response = memberAuthService.getMemberDashboard(memberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-pin")
    public ResponseEntity<Void> changePin(
            Authentication authentication,
            @Valid @RequestBody ChangePinRequest request) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
        memberService.changePin(memberId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/family-details")
    public ResponseEntity<FamilyDetailsResponse> getFamilyDetails(Authentication authentication) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
        FamilyDetailsResponse response = familyService.getFamilyDetails(memberId);
        return ResponseEntity.ok(response);
    }
}