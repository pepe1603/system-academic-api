package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKardexRequest {
    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    @NotNull(message = "El semestre académico es requerido")
    private UUID academicSemesterId;

    private UUID enrollmentId;

    private LocalDate enrollmentDate;

    private String status;

    private BigDecimal finalGrade;
    private String letterGrade;
    private Integer attemptNumber;
    private String observations;
}
