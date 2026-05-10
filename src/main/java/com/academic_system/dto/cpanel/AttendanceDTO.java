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
public class AttendanceDTO {
    private UUID id;
    private LocalDate attendanceDate;
    private String status;
    private String classTime;
    private String subjectCode;
    private String observations;
    private LocalDate justificationDate;
    private LocalDate recordedAt;
    private Boolean isDeleted;

    private UUID enrollmentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID courseId;
    private String courseCode;
    private String courseName;
}
