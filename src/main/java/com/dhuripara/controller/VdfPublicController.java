package com.dhuripara.controller;

import com.dhuripara.dto.response.VdfBalanceSummaryResponse;
import com.dhuripara.service.VdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/vdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VdfPublicController {

    private final VdfService vdfService;

    @GetMapping("/balance")
    public ResponseEntity<VdfBalanceSummaryResponse> getBalanceSummary() {
        VdfBalanceSummaryResponse summary = vdfService.getBalanceSummary();
        return ResponseEntity.ok(summary);
    }
}