package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAcademicSemesterRequest {
    private String name;
    private Integer year;
    private Integer period;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate classesStartDate;
    private LocalDate classesEndDate;
    private LocalDate enrollmentDeadline;
    private LocalDate dropDeadline;
    private String status;
    private Boolean isCurrent;
}
