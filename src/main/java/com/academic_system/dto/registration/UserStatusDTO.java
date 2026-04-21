package com.academic_system.dto.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {
    private String username;
    private String email;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean mustVerifyEmail;
    private Boolean mustChangePassword;
}