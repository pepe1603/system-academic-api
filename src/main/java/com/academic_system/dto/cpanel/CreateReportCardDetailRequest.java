package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
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
public class CreateReportCardDetailRequest {

    private UUID kardexId;

    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    @NotBlank(message = "El nombre de la materia es requerido")
    private String subjectName;

    @NotBlank(message = "El código de la materia es requerido")
    private String subjectCode;

    @NotNull(message = "Los créditos son requeridos")
    private Integer credits;

    private BigDecimal grade;
    private String gradeLetter;
    private String subjectStatus;
    private BigDecimal attendancePercentage;
    private Integer totalAttendances;
    private Integer classesAttended;
    private String observations;
}
