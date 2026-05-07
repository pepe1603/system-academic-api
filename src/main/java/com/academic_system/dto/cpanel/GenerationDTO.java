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
public class GenerationDTO {
    private UUID id;
    private String name;
    private Integer entryYear;
    private Integer graduationYear;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDate createdAt;
}
