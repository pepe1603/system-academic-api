package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "kardex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kardex {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "academic_semester_id", nullable = false)
    private UUID academicSemesterId;

    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Column(name = "final_grade", precision = 5, scale = 2)
    private BigDecimal finalGrade;

    @Column(name = "letter_grade", length = 2)
    private String letterGrade;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ENROLLED";

    @Column(name = "attempt_number")
    @Builder.Default
    private Integer attemptNumber = 1;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "official_folio", unique = true, length = 30)
    private String officialFolio;

    @Column(name = "kardex_folio", length = 30)
    private String kardexFolio;

    @Column(name = "kardex_sequence")
    private Integer kardexSequence;

    @Column(name = "is_officialized")
    @Builder.Default
    private Boolean isOfficialized = false;

    @Column(name = "officialized_by")
    private UUID officializedBy;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
