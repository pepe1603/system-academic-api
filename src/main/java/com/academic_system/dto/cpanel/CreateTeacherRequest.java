package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeacherRequest {

    private UUID userId;

    private String employeeNumber;

    private String rfc;

    private String curp;

    @NotBlank(message = "El nombre es requerido")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    private String lastName;

    private String institutionalEmail;
    private String secondaryEmail;
    private String phone;
    private String secondaryPhone;
}
