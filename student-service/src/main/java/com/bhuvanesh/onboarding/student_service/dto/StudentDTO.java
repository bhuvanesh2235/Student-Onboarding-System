package com.bhuvanesh.onboarding.student_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotNull(message = "Age must not be null")
    @Min(value = 1, message = "Age must be greater than 0")
    private Integer age;
}
