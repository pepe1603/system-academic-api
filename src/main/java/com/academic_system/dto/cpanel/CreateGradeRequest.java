package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class CreateGradeRequest {
    @NotNull(message = "La inscripción es requerida")
    private UUID enrollmentId;

    @NotNull(message = "El tipo de evaluación es requerido")
    private UUID evaluationTypeId;

    @NotNull(message = "La calificación es requerida")
    @DecimalMin(value = "0", message = "La calificación mínima es 0")
    @DecimalMax(value = "100", message = "La calificación máxima es 100")
    private BigDecimal score;
}
