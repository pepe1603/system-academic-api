package com.academic_system.dto.cpanel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportCardRequest {

    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotNull(message = "El semestre académico es requerido")
    private UUID academicSemesterId;

    private UUID generationId;

    private String reportCardType;
    private String generationMode;
    private String folio;
    private String series;
    private String observations;

    @NotEmpty(message = "Debe incluir al menos una materia")
    @Valid
    private List<CreateReportCardDetailRequest> details;
}
