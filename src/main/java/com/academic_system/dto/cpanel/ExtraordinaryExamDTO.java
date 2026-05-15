package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtraordinaryExamDTO {
    private UUID id;
    private Integer attemptNumber;
    private String status;
    private LocalDate scheduledDate;
    private LocalDate applicationDate;
    private String applicationTime;
    private String applicationLocation;
    private BigDecimal previousGrade;
    private BigDecimal grade;
    private String gradeLetter;
    private String observation;
    private BigDecimal cost;
    private String paymentReceipt;
    private String paymentFolio;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID courseId;
    private String courseCode;
    private String courseName;

    private UUID academicSemesterId;
    private String academicSemesterName;

    private UUID examinerId;
    private String examinerName;
}
