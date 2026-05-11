package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "report_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCard {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "academic_semester_id", nullable = false)
    private UUID academicSemesterId;

    @Column(name = "generation_id")
    private UUID generationId;

    @Column(name = "report_card_type", nullable = false, length = 30)
    @Builder.Default
    private String reportCardType = "ORDINARY";

    @Column(name = "generation_mode", nullable = false, length = 20)
    @Builder.Default
    private String generationMode = "ONLINE";

    @Column(name = "overall_average", precision = 5, scale = 2)
    private BigDecimal overallAverage;

    @Column(name = "average_letter", length = 2)
    private String averageLetter;

    @Column(name = "attendance_average", precision = 5, scale = 2)
    private BigDecimal attendanceAverage;

    @Column(name = "total_credits_enrolled")
    @Builder.Default
    private Integer totalCreditsEnrolled = 0;

    @Column(name = "total_credits_approved")
    @Builder.Default
    private Integer totalCreditsApproved = 0;

    @Column(name = "total_subjects")
    @Builder.Default
    private Integer totalSubjects = 0;

    @Column(name = "total_subjects_approved")
    @Builder.Default
    private Integer totalSubjectsApproved = 0;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ISSUED";

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "origin_semester_id")
    private UUID originSemesterId;

    @Column(name = "destination_semester_id")
    private UUID destinationSemesterId;

    @Column(name = "folio", unique = true, length = 30)
    private String folio;

    @Column(name = "series", length = 20)
    private String series;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "is_signed")
    @Builder.Default
    private Boolean isSigned = false;

    @Column(name = "signed_by")
    private UUID signedBy;

    @Column(name = "signed_at")
    private LocalDate signedAt;

    @Column(name = "signed_seal_url", columnDefinition = "TEXT")
    private String signedSealUrl;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        if (issueDate == null) issueDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
