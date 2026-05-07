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
public class CourseDTO {
    private UUID id;
    private String courseCode;
    private String name;
    private Integer credits;
    private Integer hoursTheory;
    private Integer hoursPractice;
    private String description;
    private Boolean isMandatory;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDate createdAt;

    private UUID studyPlanId;
    private String studyPlanCode;
    private String studyPlanName;

    private UUID semesterId;
    private String semesterName;
    private Integer semesterNumber;
}
