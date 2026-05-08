package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGradeRequest {
    @DecimalMin(value = "0", message = "La calificación mínima es 0")
    @DecimalMax(value = "100", message = "La calificación máxima es 100")
    private BigDecimal score;
}
