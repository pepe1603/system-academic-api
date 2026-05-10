package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotNull;
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
public class CreateAttendanceRequest {
    @NotNull(message = "La inscripción es requerida")
    private UUID enrollmentId;

    @NotNull(message = "La fecha es requerida")
    private LocalDate attendanceDate;

    private String status;

    private String classTime;
    private String subjectCode;
    private String observations;
}
