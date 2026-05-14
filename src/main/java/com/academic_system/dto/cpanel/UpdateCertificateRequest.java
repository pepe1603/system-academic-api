package com.academic_system.dto.cpanel;

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
public class UpdateCertificateRequest {
    private String certificateType;
    private String officialFolio;
    private String internalFolio;
    private String series;
    private BigDecimal finalAverage;
    private Integer totalCredits;
    private Integer totalSubjects;
    private LocalDate issueDate;
    private LocalDate deliveryDate;
    private String status;
    private UUID directorSigner;
    private UUID secretarySigner;
    private String recordNumber;
    private String recordBook;
    private String recordPage;
    private String observations;
}
