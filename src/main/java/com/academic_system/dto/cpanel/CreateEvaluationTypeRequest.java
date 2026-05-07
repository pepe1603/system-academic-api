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
public class CreateEvaluationTypeRequest {
    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    @NotBlank(message = "El código es requerido")
    private String code;

    private String name;

    private BigDecimal weight;
}
