package com.dhuripara.controller;

import com.dhuripara.model.VdfContribution;
import com.dhuripara.service.VdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/member/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VdfMemberController {

    private final VdfService vdfService;

    @GetMapping("/my-contributions")
    public ResponseEntity<List<VdfContribution>> getMyContributions(Authentication authentication) {
        // Extract member ID from authentication
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));

        // TODO: Get family ID from member ID
        // For now, returning empty list - implement based on your logic
        return ResponseEntity.ok(List.of());
    }
}
