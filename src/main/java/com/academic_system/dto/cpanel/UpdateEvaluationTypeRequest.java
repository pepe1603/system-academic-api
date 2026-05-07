package com.academic_system.dto.cpanel;

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
public class UpdateEvaluationTypeRequest {
    private UUID courseId;
    private String code;
    private String name;
    private BigDecimal weight;
    private Boolean isActive;
}
