package com.dhuripara.service;

import com.dhuripara.dto.request.DocumentUploadRequest;
import com.dhuripara.dto.response.DocumentCategoryResponse;
import com.dhuripara.dto.response.MemberDocumentResponse;
import com.dhuripara.exception.ResourceNotFoundException;
import com.dhuripara.model.DocumentCategory;
import com.dhuripara.model.Member;
import com.dhuripara.model.MemberDocument;
import com.dhuripara.repository.DocumentCategoryRepository;
import com.dhuripara.repository.MemberDocumentRepository;
import com.dhuripara.repository.MemberRepository;
import com.dhuripara.util.NameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final MemberDocumentRepository documentRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final OracleObjectStorageService objectStorageService;

    public List<DocumentCategoryResponse> getAllCategories() {
        return categoryRepository.findByIsActiveTrueOrderByCategoryNameAsc().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberDocumentResponse uploadDocument(DocumentUploadRequest request, UUID adminUserId) throws Exception {
        // Validate member
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        // Validate category
        DocumentCategory category = categoryRepository.findById(request.getDocumentCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Document category not found"));

        if (!Boolean.TRUE.equals(category.getIsActive())) {
            throw new IllegalArgumentException("Document category is not active");
        }

        // Validate file
        MultipartFile file = request.getFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/pdf"))) {
            throw new IllegalArgumentException("Only JPG and PDF files are allowed");
        }

        // Upload to Oracle Object Storage
        String folderPath = "members/" + member.getId().toString();
        String objectStoragePath = objectStorageService.uploadFile(file, folderPath);

        // Save document record
        MemberDocument document = new MemberDocument();
        document.setMember(member);
        document.setDocumentCategory(category);
        document.setFileName(objectStoragePath.substring(objectStoragePath.lastIndexOf("/") + 1));
        document.setOriginalFileName(file.getOriginalFilename());
        document.setFileSize(file.getSize());
        document.setContentType(contentType);
        document.setObjectStoragePath(objectStoragePath);
        document.setUploadedBy(adminUserId);
        document.setNotes(request.getNotes());

        MemberDocument saved = documentRepository.save(document);
        log.info("Document uploaded successfully: {} for member: {}", saved.getId(), member.getId());

        return mapToDocumentResponse(saved);
    }

    public List<MemberDocumentResponse> getMemberDocuments(UUID memberId) {
        List<MemberDocument> documents = documentRepository.findByMemberIdOrderByUploadedAtDesc(memberId);
        return documents.stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    public List<MemberDocumentResponse> getFamilyDocuments(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (member.getFamilyId() == null) {
            throw new IllegalArgumentException("Member is not associated with any family");
        }

        List<MemberDocument> documents = documentRepository.findByMemberFamilyId(member.getFamilyId());
        return documents.stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());
    }

    public InputStream downloadDocument(UUID documentId) throws Exception {
        MemberDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        return objectStorageService.downloadFile(document.getObjectStoragePath());
    }

    public String getDocumentDownloadUrl(UUID documentId) throws Exception {
        MemberDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Generate pre-signed URL valid for 1 hour
        return objectStorageService.generatePreSignedUrl(document.getObjectStoragePath(), 1);
    }

    public MemberDocument getDocumentById(UUID documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    @Transactional
    public void deleteDocument(UUID documentId) throws Exception {
        MemberDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));

        // Delete from Object Storage
        objectStorageService.deleteFile(document.getObjectStoragePath());

        // Delete from database
        documentRepository.delete(document);
        log.info("Document deleted successfully: {}", documentId);
    }

    private DocumentCategoryResponse mapToCategoryResponse(DocumentCategory category) {
        return new DocumentCategoryResponse(
                category.getId(),
                category.getCategoryName(),
                category.getDescription(),
                category.getIsActive()
        );
    }

    private MemberDocumentResponse mapToDocumentResponse(MemberDocument document) {
        String downloadUrl = null;
        try {
            downloadUrl = objectStorageService.generatePreSignedUrl(document.getObjectStoragePath(), 1);
        } catch (Exception e) {
            log.warn("Failed to generate download URL for document: {}", document.getId(), e);
        }

        return new MemberDocumentResponse(
                document.getId(),
                document.getMember().getId(),
                NameUtil.buildMemberName(document.getMember()),
                document.getDocumentCategory().getId(),
                document.getDocumentCategory().getCategoryName(),
                document.getFileName(),
                document.getOriginalFileName(),
                document.getFileSize(),
                document.getContentType(),
                downloadUrl,
                document.getUploadedAt(),
                null, // uploadedBy name - can be fetched if needed
                document.getNotes()
        );
    }
}

