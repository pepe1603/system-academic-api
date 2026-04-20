package com.academic_system.dto.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationVerifyRequest {

    @NotBlank(message = "CURP es requerido")
    private String curp;

    @NotBlank(message = "OTP es requerido")
    @Size(min = 6, max = 6, message = "OTP debe tener 6 dígitos")
    private String otp;
}