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
public class SemesterDTO {
    private UUID id;
    private UUID studyPlanId;
    private String studyPlanName;
    private Integer semesterNumber;
    private String name;
    private Boolean isActive;
    private Boolean isDeleted;
}
