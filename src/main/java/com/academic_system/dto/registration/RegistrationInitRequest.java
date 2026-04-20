package com.academic_system.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationInitRequest {

    @NotBlank(message = "CURP es requerido")
    @Size(min = 18, max = 18, message = "CURP debe tener 18 caracteres")
    private String curp;

    @NotBlank(message = "Email es requerido")
    @Email(message = "Email inválido")
    private String email;
}