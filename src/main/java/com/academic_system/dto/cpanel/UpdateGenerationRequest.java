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
public class UpdateGenerationRequest {
    private String name;
    private Integer entryYear;
    private Integer graduationYear;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
