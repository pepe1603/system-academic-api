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
public class CreateEducationalResourceRequest {

    @NotBlank(message = "El título es requerido")
    private String title;

    private String description;

    @NotBlank(message = "El tipo de recurso es requerido")
    private String resourceType;

    @NotBlank(message = "La URL del recurso es requerida")
    private String resourceUrl;

    private UUID courseId;
}
