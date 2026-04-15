package com.academic_system.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private Boolean isRead;
    private Boolean isResponded;
    private String response;
    private LocalDateTime responseDate;
    private LocalDateTime createdAt;
}