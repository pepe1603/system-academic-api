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
public class UpdateSemesterRequest {
    private UUID studyPlanId;
    private Integer semesterNumber;
    private String name;
    private Boolean isActive;
}
