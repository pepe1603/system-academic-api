package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "conduct")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conduct {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "academic_semester_id", nullable = false)
    private UUID academicSemesterId;

    @Column(name = "grade", length = 2)
    private String grade;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "warnings")
    @Builder.Default
    private Integer warnings = 0;

    @Column(name = "congratulations")
    @Builder.Default
    private Integer congratulations = 0;

    @Column(name = "recorded_by")
    private UUID recordedBy;

    @Column(name = "registration_date", updatable = false)
    private LocalDate registrationDate;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        registrationDate = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
