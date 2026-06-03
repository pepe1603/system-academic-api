package com.academic_system.dto.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorDTO {
    private String status;
    private String timestamp;
    private Map<String, String> services;
}
