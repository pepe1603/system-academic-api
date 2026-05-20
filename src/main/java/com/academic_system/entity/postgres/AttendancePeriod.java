package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_period",
       uniqueConstraints = @UniqueConstraint(columnNames = {"enrollment_id", "academic_semester_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendancePeriod {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "academic_semester_id", nullable = false)
    private UUID academicSemesterId;

    @Column(name = "total_classes")
    @Builder.Default
    private Integer totalClasses = 0;

    @Column(name = "total_present")
    @Builder.Default
    private Integer totalPresent = 0;

    @Column(name = "total_absent")
    @Builder.Default
    private Integer totalAbsent = 0;

    @Column(name = "total_justified")
    @Builder.Default
    private Integer totalJustified = 0;

    @Column(name = "total_late")
    @Builder.Default
    private Integer totalLate = 0;

    @Column(name = "attendance_percentage", precision = 5, scale = 2)
    private BigDecimal attendancePercentage;

    @Column(name = "attendance_status", length = 20)
    @Builder.Default
    private String attendanceStatus = "IN_RANGE";

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        recalculatePercentage();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        recalculatePercentage();
    }

    public void recalculatePercentage() {
        int total = totalClasses != null ? totalClasses : 0;
        if (total > 0) {
            int present = totalPresent != null ? totalPresent : 0;
            double pct = (present * 100.0) / total;
            attendancePercentage = BigDecimal.valueOf(Math.round(pct * 100.0) / 100.0);

            if (attendancePercentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
                attendanceStatus = "SATISFACTORY";
            } else if (attendancePercentage.compareTo(BigDecimal.valueOf(60)) >= 0) {
                attendanceStatus = "AT_RISK";
            } else {
                attendanceStatus = "INSUFFICIENT";
            }
        } else {
            attendancePercentage = BigDecimal.ZERO;
            attendanceStatus = "IN_RANGE";
        }
    }
}
