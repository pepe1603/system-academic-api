package com.academic_system.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationInitRequest {

    @NotBlank(message = "CURP es requerido")
    @Pattern(regexp = "^[A-Z]{4}\\d{6}[A-Z]{6}\\d{2}$", message = "CURP inválido")
    private String curp;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email inválido")
    private String email;
}