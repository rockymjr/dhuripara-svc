package com.dhuripara.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vdf_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdfNotification {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // null means notification is for all members

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "title_bn", length = 200)
    private String titleBn;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_bn", columnDefinition = "TEXT")
    private String messageBn;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // "EXPENSE" or "DEPOSIT"

    @Column(name = "related_id")
    private UUID relatedId; // ID of related expense or deposit

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

