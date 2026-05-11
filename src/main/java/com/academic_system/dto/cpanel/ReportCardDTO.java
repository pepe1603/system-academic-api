package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardDTO {
    private UUID id;

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
    private LocalDate issueDate;
    private LocalDate deliveryDate;
    private String folio;
    private String series;
    private String observations;
    private Boolean isSigned;
    private Boolean isDeleted;
    private LocalDate createdAt;

    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID academicSemesterId;
    private String academicSemesterName;

    private UUID generationId;
    private String generationName;

    private List<ReportCardDetailDTO> details;
}
