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
public class UpdateExtraordinaryExamRequest {
    private Integer attemptNumber;
    private String status;
    private LocalDate scheduledDate;
    private LocalDate applicationDate;
    private String applicationTime;
    private String applicationLocation;
    private BigDecimal previousGrade;
    private BigDecimal grade;
    private String gradeLetter;
    private UUID examinerId;
    private String observation;
    private BigDecimal cost;
    private String paymentReceipt;
    private String paymentFolio;
}
