package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private UUID id;
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
    private String recordNumber;
    private String recordBook;
    private String recordPage;
    private String observations;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;

    private UUID generationId;
    private String generationName;

    private UUID directorSigner;
    private String directorName;

    private UUID secretarySigner;
    private String secretaryName;
}
