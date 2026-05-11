package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "conduct_incident")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductIncident {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "incident_type", nullable = false, length = 50)
    private String incidentType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "severity", length = 20)
    @Builder.Default
    private String severity = "MINOR";

    @Column(name = "actions_taken", columnDefinition = "TEXT")
    private String actionsTaken;

    @Column(name = "attention_date")
    private LocalDate attentionDate;

    @Column(name = "recorded_by")
    private UUID recordedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}
