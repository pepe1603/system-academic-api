package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
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
public class CreateCourseRequest {
    @NotNull(message = "El plan de estudio es requerido")
    private UUID studyPlanId;

    @NotNull(message = "El semestre es requerido")
    private UUID semesterId;

    @NotBlank(message = "El código del curso es requerido")
    private String courseCode;

    @NotBlank(message = "El nombre del curso es requerido")
    private String name;

    @NotNull(message = "Los créditos son requeridos")
    private Integer credits;

    private Integer hoursTheory;
    private Integer hoursPractice;
    private String description;
    private Boolean isMandatory;
    private Boolean isActive;
}
