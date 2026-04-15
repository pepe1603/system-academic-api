package com.academic_system.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdvertisementDTO {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private Integer displayOrder;
    private Boolean isPublished;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}