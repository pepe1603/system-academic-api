package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessAuditDTO {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private String action;
    private String module;
    private String ipAddress;
    private Boolean success;
    private String metadata;
    private LocalDateTime createdAt;
}
