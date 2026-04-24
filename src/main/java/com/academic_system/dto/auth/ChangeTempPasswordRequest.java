package com.academic_system.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTempPasswordRequest {

    @NotBlank(message = "Email es requerido")
    private String email;

    @NotBlank(message = "Contraseña temporal es requerida")
    private String tempPassword;

    @NotBlank(message = "Nueva contraseña es requerida")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String newPassword;

    @NotBlank(message = "Confirmación de contraseña es requerida")
    private String confirmPassword;

    public boolean isPasswordMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}