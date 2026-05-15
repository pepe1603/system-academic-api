package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGuardianRequest {

    @NotNull(message = "El estudiante es requerido")
    private UUID studentId;

    @NotBlank(message = "El nombre completo es requerido")
    private String fullName;

    @NotBlank(message = "El parentesco es requerido")
    private String relationship;

    private String curp;
    private String primaryPhone;
    private String secondaryPhone;
    private String email;
    private String occupation;
    private String company;
    private String address;
    private Boolean isEmergencyContact;
}
