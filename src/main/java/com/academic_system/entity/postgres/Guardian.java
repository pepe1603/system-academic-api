package com.academic_system.entity.postgres;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guardian")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guardian {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "curp", length = 18)
    private String curp;

    @Column(name = "primary_phone", length = 20)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "company", length = 150)
    private String company;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "is_emergency_contact")
    @Builder.Default
    private Boolean isEmergencyContact = true;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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
