package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateKardexRequest {
    private BigDecimal finalGrade;
    private String letterGrade;
    private String status;
    private Integer attemptNumber;
    private LocalDate approvalDate;
    private String officialFolio;
    private String kardexFolio;
    private Integer kardexSequence;
    private Boolean isOfficialized;
    private String observations;
}
