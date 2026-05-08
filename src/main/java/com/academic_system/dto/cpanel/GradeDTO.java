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
public class GradeDTO {
    private UUID id;
    private BigDecimal score;
    private LocalDate recordedAt;
    private Boolean isDeleted;

    private UUID enrollmentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID evaluationTypeId;
    private String evaluationCode;
    private String evaluationName;
    private BigDecimal evaluationWeight;

    private UUID courseId;
    private String courseCode;
    private String courseName;
}
