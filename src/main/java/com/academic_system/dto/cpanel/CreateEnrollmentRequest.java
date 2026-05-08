package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEnrollmentRequest {
    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    @NotNull(message = "El período académico es requerido")
    private UUID academicPeriodId;

    private UUID groupId;

    private String status;
}
