package com.academic_system.dto.cpanel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGuardianRequest {
    private String fullName;
    private String relationship;
    private String curp;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String occupation;
    private String company;
    private String address;
    private Boolean isEmergencyContact;
    private Boolean isActive;
}
