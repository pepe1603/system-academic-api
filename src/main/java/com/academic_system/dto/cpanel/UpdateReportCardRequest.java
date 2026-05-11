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
public class UpdateReportCardRequest {
    private String reportCardType;
    private String generationMode;
    private BigDecimal overallAverage;
    private String averageLetter;
    private BigDecimal attendanceAverage;
    private Integer totalCreditsEnrolled;
    private Integer totalCreditsApproved;
    private Integer totalSubjects;
    private Integer totalSubjectsApproved;
    private String status;
    private LocalDate deliveryDate;
    private String folio;
    private String series;
    private String observations;
    private Boolean isSigned;
    private LocalDate signedAt;
    private String signedSealUrl;
}
