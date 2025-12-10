package com.dhuripara.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vdf_deposits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdfDeposit {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(name = "source_name", length = 200)
    private String sourceName; // Name of villager/donor

    @Column(name = "source_name_bn", length = 200)
    private String sourceNameBn; // Bengali name of villager/donor

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // Optional: link to member if villager

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_category_id")
    private VdfDepositCategory category; // Category of deposit

    @Column(name = "year")
    private Integer year;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (year == null) {
            year = depositDate.getYear();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}