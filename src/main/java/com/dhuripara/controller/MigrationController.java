package com.dhuripara.controller;

import com.dhuripara.util.BengaliDataMigration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only controller to manually trigger Bengali data migration.
 * 
 * WARNING: This should only be accessible to admins.
 * The migration is safe to run multiple times - it only updates null fields.
 * 
 * Endpoint: POST /api/admin/migrate/bengali
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/migrate")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MigrationController {

    private final BengaliDataMigration bengaliDataMigration;

    @PostMapping("/bengali")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> migrateToBengali() {
        log.info("Admin triggered Bengali transliteration migration");

        try {
            bengaliDataMigration.migrate();
            String message = "Bengali transliteration migration completed successfully. " +
                    "Please review results and make manual corrections as needed.";
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            String error = "Migration failed: " + e.getMessage();
            log.error(error, e);
            return ResponseEntity.status(500).body(error);
        }
    }
}
