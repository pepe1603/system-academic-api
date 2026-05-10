package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PRESENT";

    @Column(name = "class_time", length = 10)
    private String classTime;

    @Column(name = "subject_code", length = 20)
    private String subjectCode;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "justified_by")
    private UUID justifiedBy;

    @Column(name = "justification_date")
    private LocalDate justificationDate;

    @Column(name = "recorded_by")
    private UUID recordedBy;

    @Column(name = "recorded_at", updatable = false)
    private LocalDate recordedAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDate.now();
    }
}
