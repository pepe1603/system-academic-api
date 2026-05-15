package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "retake_exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetakeExam {

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

    @Column(name = "origin_semester_id")
    private UUID originSemesterId;

    @Column(name = "previous_average", precision = 5, scale = 2)
    private BigDecimal previousAverage;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ENROLLED";

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
