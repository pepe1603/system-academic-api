package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAcademicSemesterRequest {
    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotNull(message = "El año es requerido")
    private Integer year;

    @NotNull(message = "El período es requerido")
    private Integer period;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate endDate;

    @NotNull(message = "La fecha de inicio de clases es requerida")
    private LocalDate classesStartDate;

    @NotNull(message = "La fecha de fin de clases es requerida")
    private LocalDate classesEndDate;

    private LocalDate enrollmentDeadline;
    private LocalDate dropDeadline;
    private String status;
    private Boolean isCurrent;
}
