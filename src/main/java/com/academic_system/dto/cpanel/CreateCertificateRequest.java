package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateRequest {

    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    private UUID generationId;

    @NotBlank(message = "El tipo de certificado es requerido")
    private String certificateType;

    private String officialFolio;
    private String internalFolio;
    private String series;

    private BigDecimal finalAverage;
    private Integer totalCredits;
    private Integer totalSubjects;

    private LocalDate issueDate;

    private UUID directorSigner;
    private UUID secretarySigner;

    private String recordNumber;
    private String recordBook;
    private String recordPage;
    private String observations;
}
