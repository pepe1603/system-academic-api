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
public class AcademicGroupDTO {
    private UUID id;
    private String name;
    private Integer capacity;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDate createdAt;

    private UUID academicSemesterId;
    private String academicSemesterName;

    private UUID courseId;
    private String courseCode;
    private String courseName;

    private UUID teacherId;
    private String teacherFullName;
}
