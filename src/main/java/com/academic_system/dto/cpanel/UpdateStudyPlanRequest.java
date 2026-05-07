package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudyPlanRequest {
    private String code;
    private String name;
    private String version;
    private String description;
    private String titleDegree;
    private Integer totalCredits;
    private Integer durationSemesters;
}
