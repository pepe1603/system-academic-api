package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateConductIncidentRequest {

    @NotNull(message = "La inscripción es requerida")
    private UUID enrollmentId;

    @NotBlank(message = "El tipo de incidente es requerido")
    private String incidentType;

    @NotBlank(message = "La descripción es requerida")
    private String description;

    @NotNull(message = "La fecha del incidente es requerida")
    private LocalDate incidentDate;

    private String severity;
    private String actionsTaken;
    private LocalDate attentionDate;
    private UUID recordedBy;
}
