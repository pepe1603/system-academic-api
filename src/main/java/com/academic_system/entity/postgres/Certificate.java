package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "generation_id")
    private UUID generationId;

    @Column(name = "certificate_type", nullable = false, length = 50)
    private String certificateType;

    @Column(name = "official_folio", unique = true, length = 50)
    private String officialFolio;

    @Column(name = "internal_folio", length = 50)
    private String internalFolio;

    @Column(name = "series", length = 20)
    private String series;

    @Column(name = "final_average", precision = 5, scale = 2)
    private BigDecimal finalAverage;

    @Column(name = "total_credits")
    private Integer totalCredits;

    @Column(name = "total_subjects")
    private Integer totalSubjects;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ISSUED";

    @Column(name = "director_signer")
    private UUID directorSigner;

    @Column(name = "secretary_signer")
    private UUID secretarySigner;

    @Column(name = "record_number", length = 30)
    private String recordNumber;

    @Column(name = "record_book", length = 20)
    private String recordBook;

    @Column(name = "record_page", length = 20)
    private String recordPage;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (issueDate == null) issueDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
