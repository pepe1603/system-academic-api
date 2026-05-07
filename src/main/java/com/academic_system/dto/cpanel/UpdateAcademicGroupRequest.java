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
public class UpdateAcademicGroupRequest {
    private String name;
    private UUID academicSemesterId;
    private UUID courseId;
    private UUID teacherId;
    private Integer capacity;
    private Boolean isActive;
}
