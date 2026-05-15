package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "extraordinary_exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtraordinaryExam {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "academic_semester_id")
    private UUID academicSemesterId;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "SCHEDULED";

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "application_time", length = 10)
    private String applicationTime;

    @Column(name = "application_location", length = 100)
    private String applicationLocation;

    @Column(name = "previous_grade", precision = 5, scale = 2)
    private BigDecimal previousGrade;

    @Column(name = "grade", precision = 5, scale = 2)
    private BigDecimal grade;

    @Column(name = "grade_letter", length = 2)
    private String gradeLetter;

    @Column(name = "examiner_id")
    private UUID examinerId;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @Column(name = "cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "payment_receipt", length = 100)
    private String paymentReceipt;

    @Column(name = "payment_folio", length = 50)
    private String paymentFolio;

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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
