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
public class InstitutionDTO {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String mission;
    private String vision;
    private String history;
    private String values;
    private String logoUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}