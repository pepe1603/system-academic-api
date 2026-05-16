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
public class UpdateEducationalResourceRequest {
    private String title;
    private String description;
    private String resourceType;
    private String resourceUrl;
    private UUID courseId;
    private Boolean isPublished;
}
