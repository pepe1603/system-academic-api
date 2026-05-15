package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotNull;
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
public class CreateExtraordinaryExamRequest {

    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    private UUID academicSemesterId;

    private Integer attemptNumber;

    private LocalDate scheduledDate;
    private String applicationTime;
    private String applicationLocation;

    private BigDecimal previousGrade;

    private UUID examinerId;
    private String observation;

    private BigDecimal cost;
    private String paymentReceipt;
    private String paymentFolio;
}
