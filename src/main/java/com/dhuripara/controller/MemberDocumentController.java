package com.dhuripara.controller;

import com.dhuripara.dto.response.MemberDocumentResponse;
import com.dhuripara.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/member/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MemberDocumentController {

    private final DocumentService documentService;

    @GetMapping("/my-documents")
    public ResponseEntity<List<MemberDocumentResponse>> getMyDocuments(Authentication authentication) {
        String username = authentication.getName();
        UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
        return ResponseEntity.ok(documentService.getMemberDocuments(memberId));
    }

    @GetMapping("/family-documents")
    public ResponseEntity<List<MemberDocumentResponse>> getFamilyDocuments(Authentication authentication) {
        try {
            String username = authentication.getName();
            UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
            return ResponseEntity.ok(documentService.getFamilyDocuments(memberId));
        } catch (IllegalArgumentException e) {
            // Member not associated with family - return empty list
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error fetching family documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> downloadDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
            
            // Verify document belongs to member or their family
            List<MemberDocumentResponse> myDocs = documentService.getMemberDocuments(memberId);
            List<MemberDocumentResponse> familyDocs;
            try {
                familyDocs = documentService.getFamilyDocuments(memberId);
            } catch (IllegalArgumentException e) {
                // Member not associated with family - treat as no family docs
                familyDocs = List.of();
            }
            
            boolean hasAccess = myDocs.stream().anyMatch(d -> d.getId().equals(documentId)) ||
                               familyDocs.stream().anyMatch(d -> d.getId().equals(documentId));
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            InputStream inputStream = documentService.downloadDocument(documentId);
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            // Get document info for headers
            MemberDocumentResponse doc = myDocs.stream()
                    .filter(d -> d.getId().equals(documentId))
                    .findFirst()
                    .orElse(familyDocs.stream()
                            .filter(d -> d.getId().equals(documentId))
                            .findFirst()
                            .orElse(null));

            if (doc == null) {
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = doc.getContentType().startsWith("image/") 
                ? MediaType.IMAGE_JPEG 
                : MediaType.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{documentId}/url")
    public ResponseEntity<String> getDocumentUrl(@PathVariable UUID documentId, Authentication authentication) {
        try {
            String username = authentication.getName();
            UUID memberId = UUID.fromString(username.replace("MEMBER_", ""));
            
            // Verify document belongs to member or their family
            List<MemberDocumentResponse> myDocs = documentService.getMemberDocuments(memberId);
            List<MemberDocumentResponse> familyDocs;
            try {
                familyDocs = documentService.getFamilyDocuments(memberId);
            } catch (Exception e) {
                familyDocs = List.of(); // If family documents can't be fetched, use empty list
            }
            
            boolean hasAccess = myDocs.stream().anyMatch(d -> d.getId().equals(documentId)) ||
                               familyDocs.stream().anyMatch(d -> d.getId().equals(documentId));
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String url = documentService.getDocumentDownloadUrl(documentId);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(url);
        } catch (Exception e) {
            log.error("Error getting document URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

