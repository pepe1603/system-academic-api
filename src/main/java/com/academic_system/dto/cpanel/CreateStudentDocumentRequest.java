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
public class CreateStudentDocumentRequest {

    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotBlank(message = "El tipo de documento es requerido")
    private String documentType;

    @NotBlank(message = "El nombre original es requerido")
    private String originalName;

    @NotBlank(message = "El nombre del archivo es requerido")
    private String fileName;

    @NotBlank(message = "La ruta del archivo es requerida")
    private String filePath;

    private Long fileSizeBytes;
    private String mimeType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private String observations;
}
