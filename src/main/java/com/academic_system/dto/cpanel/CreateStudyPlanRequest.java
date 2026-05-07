package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudyPlanRequest {
    @NotBlank(message = "El código es requerido")
    private String code;

    @NotBlank(message = "El nombre es requerido")
    private String name;

    private String version;
    private String description;
    private String titleDegree;
    private Integer totalCredits;
    private Integer durationSemesters;
}
