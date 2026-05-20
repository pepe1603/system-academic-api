package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAttendancePeriodRequest {
    private Integer totalClasses;
    private Integer totalPresent;
    private Integer totalAbsent;
    private Integer totalJustified;
    private Integer totalLate;
    private String observations;
}
