package com.academic_system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "El email es requerido")
    private String email;

    @NotBlank(message = "El código es requerido")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    private String code;

    private String purpose;
}
