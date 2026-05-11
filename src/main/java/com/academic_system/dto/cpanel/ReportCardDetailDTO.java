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
public class ReportCardDetailDTO {
    private UUID id;
    private UUID reportCardId;
    private UUID kardexId;
    private UUID courseId;
    private String subjectName;
    private String subjectCode;
    private Integer credits;
    private BigDecimal grade;
    private String gradeLetter;
    private String subjectStatus;
    private BigDecimal attendancePercentage;
    private Integer totalAttendances;
    private Integer classesAttended;
    private String observations;
}
