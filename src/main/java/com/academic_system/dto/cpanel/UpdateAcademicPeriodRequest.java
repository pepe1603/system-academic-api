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
public class UpdateAcademicPeriodRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
