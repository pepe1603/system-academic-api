package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentDocumentRequest {
    private String originalName;
    private String fileName;
    private String filePath;
    private Long fileSizeBytes;
    private String mimeType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private Boolean isVerified;
    private UUID verifiedBy;
    private LocalDateTime verificationDate;
    private String observations;
    private Boolean isActive;
}
