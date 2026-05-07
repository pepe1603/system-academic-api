package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
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
public class CreateStudentRequest {
    @NotBlank(message = "El CURP es requerido")
    private String curp;

    @NotBlank(message = "El número de matrícula es requerido")
    private String enrollmentNumber;

    @NotBlank(message = "El nombre es requerido")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    private String lastName;

    @NotNull(message = "La generación es requerida")
    private UUID generationId;

    private UUID userId;
    private String institutionalEmail;
    private String phone;
    private LocalDate birthDate;
    private String gender;
    private LocalDate enrollmentDate;
}
