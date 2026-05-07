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
public class CreateGenerationRequest {
    @NotBlank(message = "El nombre es requerido")
    private String name;

    @NotNull(message = "El año de entrada es requerido")
    private Integer entryYear;

    private Integer graduationYear;

    @NotBlank(message = "El estado es requerido")
    private String status;

    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate startDate;

    private LocalDate endDate;
}
