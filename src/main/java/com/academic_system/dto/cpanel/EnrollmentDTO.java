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
public class EnrollmentDTO {
    private UUID id;
    private String status;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDate createdAt;

    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID courseId;
    private String courseCode;
    private String courseName;

    private UUID academicPeriodId;
    private String academicPeriodName;

    private UUID groupId;
    private String groupName;
}
