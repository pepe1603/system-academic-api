package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConductIncidentRequest {
    private String incidentType;
    private String description;
    private LocalDate incidentDate;
    private String severity;
    private String actionsTaken;
    private LocalDate attentionDate;
}
