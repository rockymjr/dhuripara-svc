package com.graminbank.controller;

import com.graminbank.dto.response.VdfFamilyConfigResponse;
import com.graminbank.dto.response.VdfContributionResponse;
import com.graminbank.service.VdfService;
import com.graminbank.repository.VdfFamilyConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/member/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('MEMBER')")
public class MemberVdfController {

    private final VdfService vdfService;
    private final VdfFamilyConfigRepository familyConfigRepository;

    @GetMapping("/my-contributions")
    public ResponseEntity<List<VdfContributionResponse>> getMyContributions(
            Authentication authentication,
            @RequestParam(required = false) Integer year) {

        // Extract member ID from authentication
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));

        // Find family config for this member
        var familyConfig = familyConfigRepository.findByMemberId(memberId);

        if (familyConfig.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        if (year == null) {
            year = java.time.Year.now().getValue();
        }

        List<VdfContributionResponse> contributions =
                vdfService.getFamilyContributions(familyConfig.get().getId(), year);

        return ResponseEntity.ok(contributions);
    }

    @GetMapping("/my-status")
    public ResponseEntity<VdfFamilyConfigResponse> getMyStatus(Authentication authentication) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));

        var familyConfig = familyConfigRepository.findByMemberId(memberId);

        if (familyConfig.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Convert to response with calculated dues
        VdfFamilyConfigResponse response = convertToResponse(familyConfig.get());
        return ResponseEntity.ok(response);
    }

    private VdfFamilyConfigResponse convertToResponse(
            com.graminbank.model.VdfFamilyConfig config) {
        VdfFamilyConfigResponse response = new VdfFamilyConfigResponse();
        response.setId(config.getId());
        response.setMemberId(config.getMember().getId());
        response.setMemberName(config.getMember().getFirstName() + " " +
                config.getMember().getLastName());
        response.setFamilyHeadName(config.getFamilyHeadName());
        response.setIsContributionEnabled(config.getIsContributionEnabled());
        response.setEffectiveFrom(config.getEffectiveFrom());
        response.setMonthlyAmount(config.getMonthlyAmount());
        response.setNotes(config.getNotes());
        return response;
    }
}