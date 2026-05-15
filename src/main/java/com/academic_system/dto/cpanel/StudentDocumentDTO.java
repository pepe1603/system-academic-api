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
public class StudentDocumentDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String enrollmentNumber;
    private String documentType;
    private String originalName;
    private String fileName;
    private String filePath;
    private Long fileSizeBytes;
    private String mimeType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private Boolean isVerified;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
