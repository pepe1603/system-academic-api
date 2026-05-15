package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRetakeExamRequest {

    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    @NotNull(message = "El semestre académico es requerido")
    private UUID academicSemesterId;

    private UUID originSemesterId;
    private BigDecimal previousAverage;
}
