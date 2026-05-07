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
public class StudyPlanDTO {
    private UUID id;
    private String code;
    private String name;
    private String version;
    private String description;
    private String titleDegree;
    private Integer totalCredits;
    private Integer durationSemesters;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDate createdAt;
}
