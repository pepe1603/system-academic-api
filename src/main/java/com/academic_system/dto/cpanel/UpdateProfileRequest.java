package com.academic_system.dto.cpanel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @NotBlank(message = "El nombre es requerido")
    private String firstName;

    @NotBlank(message = "Los apellidos son requeridos")
    private String lastName;

    private String curp;
    private String rfc;
    private String phone;
    private String secondaryPhone;
    private LocalDate birthDate;
    private String gender; // M, F, O

    private String employeeNumber;
    private String enrollmentNumber;
    private String institutionalEmail;
    private String secondaryEmail;

    private String address;
    private String city;
    private String state;
    private String postalCode;

    private String profilePictureUrl;
}