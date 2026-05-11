package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConductDTO {
    private UUID id;
    private String grade;
    private String observations;
    private Integer warnings;
    private Integer congratulations;
    private Boolean isDeleted;
    private LocalDate registrationDate;

    private UUID enrollmentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID academicSemesterId;
    private String academicSemesterName;

    private UUID courseId;
    private String courseCode;
    private String courseName;

    private UUID studentId;
}
