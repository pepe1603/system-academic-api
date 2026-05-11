package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConductRequest {

    @NotNull(message = "La inscripción es requerida")
    private UUID enrollmentId;

    @NotNull(message = "El semestre académico es requerido")
    private UUID academicSemesterId;

    private String grade;
    private String observations;
    private Integer warnings;
    private Integer congratulations;
    private UUID recordedBy;
}
