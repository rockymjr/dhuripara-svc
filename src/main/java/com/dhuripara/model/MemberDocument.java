package com.dhuripara.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "member_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDocument {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_category_id", nullable = false)
    private DocumentCategory documentCategory;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize; // in bytes

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType; // e.g., "image/jpeg", "application/pdf"

    @Column(name = "object_storage_path", nullable = false, length = 500)
    private String objectStoragePath; // Full path in Oracle Object Storage

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy; // Member user ID (uploader)

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "notes", length = 1000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}

