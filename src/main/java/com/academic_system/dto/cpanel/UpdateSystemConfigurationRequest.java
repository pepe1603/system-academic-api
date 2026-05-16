package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSystemConfigurationRequest {
    private String configValue;
    private String description;
    private String dataType;
    private String module;
    private Boolean isActive;
}
