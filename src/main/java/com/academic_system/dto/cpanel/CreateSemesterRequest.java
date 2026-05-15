package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateSemesterRequest {

    private UUID studyPlanId;

    @NotNull(message = "El número de semestre es requerido")
    @Min(value = 1, message = "El semestre debe estar entre 1 y 10")
    @Max(value = 10, message = "El semestre debe estar entre 1 y 10")
    private Integer semesterNumber;

    @NotBlank(message = "El nombre es requerido")
    private String name;
}
