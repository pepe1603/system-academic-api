package com.academic_system.dto.cpanel;

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
public class EvaluationTypeDTO {
    private UUID id;
    private String code;
    private String name;
    private BigDecimal weight;
    private Boolean isActive;
    private LocalDate createdAt;

    private UUID courseId;
    private String courseCode;
    private String courseName;
}
