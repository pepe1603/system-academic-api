package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
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
public class CreateAcademicGroupRequest {
    @NotBlank(message = "El nombre del grupo es requerido")
    private String name;

    @NotNull(message = "El semestre académico es requerido")
    private UUID academicSemesterId;

    @NotNull(message = "El curso es requerido")
    private UUID courseId;

    private UUID teacherId;

    private Integer capacity;
}
