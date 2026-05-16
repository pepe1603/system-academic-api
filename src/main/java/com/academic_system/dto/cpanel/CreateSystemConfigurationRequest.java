package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSystemConfigurationRequest {

    @NotBlank(message = "La clave de configuración es requerida")
    private String configKey;

    @NotBlank(message = "El valor es requerido")
    private String configValue;

    private String description;
    private String dataType;
    private String module;
}
