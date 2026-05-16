package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigurationDTO {
    private UUID id;
    private String configKey;
    private String configValue;
    private String description;
    private String dataType;
    private String module;
    private Boolean isActive;
    private Boolean isDeleted;
}
