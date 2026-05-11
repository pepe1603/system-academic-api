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
public class UpdateConductRequest {
    private String grade;
    private String observations;
    private Integer warnings;
    private Integer congratulations;
    private UUID recordedBy;
}
