package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConductIncidentDTO {
    private UUID id;
    private UUID enrollmentId;
    private String incidentType;
    private String description;
    private LocalDate incidentDate;
    private String severity;
    private String actionsTaken;
    private LocalDate attentionDate;
    private Boolean isDeleted;
    private LocalDate createdAt;

    private String studentName;
    private String enrollmentNumber;
}
