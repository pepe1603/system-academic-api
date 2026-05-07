package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {
    private UUID studyPlanId;
    private UUID semesterId;
    private String courseCode;
    private String name;
    private Integer credits;
    private Integer hoursTheory;
    private Integer hoursPractice;
    private String description;
    private Boolean isMandatory;
    private Boolean isActive;
}
