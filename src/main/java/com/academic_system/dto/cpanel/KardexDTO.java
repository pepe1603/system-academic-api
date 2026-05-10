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
public class KardexDTO {
    private UUID id;
    private BigDecimal finalGrade;
    private String letterGrade;
    private String status;
    private Integer attemptNumber;
    private LocalDate enrollmentDate;
    private LocalDate approvalDate;
    private String officialFolio;
    private String kardexFolio;
    private Integer kardexSequence;
    private Boolean isOfficialized;
    private String observations;
    private Boolean isDeleted;
    private LocalDate createdAt;

    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID courseId;
    private String courseCode;
    private String courseName;
    private Integer courseCredits;

    private UUID academicSemesterId;
    private String academicSemesterName;

    private UUID enrollmentId;
}
