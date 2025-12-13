package com.dhuripara.controller;

import com.dhuripara.dto.request.DocumentUploadRequest;
import com.dhuripara.dto.response.DocumentCategoryResponse;
import com.dhuripara.dto.response.MemberDocumentResponse;
import com.dhuripara.repository.AdminUserRepository;
import com.dhuripara.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDocumentController {

    private final DocumentService documentService;
    private final AdminUserRepository adminUserRepository;

    @GetMapping("/categories")
    public ResponseEntity<List<DocumentCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(documentService.getAllCategories());
    }

    @PostMapping("/upload")
    public ResponseEntity<MemberDocumentResponse> uploadDocument(
            @RequestParam("memberId") UUID memberId,
            @RequestParam("documentCategoryId") UUID documentCategoryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            UUID adminUserId = adminUserRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Admin user not found"))
                    .getId();
            
            DocumentUploadRequest request = new DocumentUploadRequest();
            request.setMemberId(memberId);
            request.setDocumentCategoryId(documentCategoryId);
            request.setFile(file);
            request.setNotes(notes);

            MemberDocumentResponse response = documentService.uploadDocument(request, adminUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MemberDocumentResponse>> getMemberDocuments(@PathVariable UUID memberId) {
        return ResponseEntity.ok(documentService.getMemberDocuments(memberId));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        try {
            documentService.deleteDocument(documentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> downloadDocument(
            @PathVariable UUID documentId) {
        try {
            java.io.InputStream inputStream = documentService.downloadDocument(documentId);
            org.springframework.core.io.InputStreamResource resource = 
                    new org.springframework.core.io.InputStreamResource(inputStream);
            
            // Get document info for headers
            com.dhuripara.model.MemberDocument document = documentService.getDocumentById(documentId);

            org.springframework.http.MediaType mediaType = document.getContentType().startsWith("image/") 
                ? org.springframework.http.MediaType.IMAGE_JPEG 
                : org.springframework.http.MediaType.APPLICATION_PDF;

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{documentId}/url")
    public ResponseEntity<String> getDocumentUrl(@PathVariable UUID documentId) {
        try {
            String url = documentService.getDocumentDownloadUrl(documentId);
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                    .body(url);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

