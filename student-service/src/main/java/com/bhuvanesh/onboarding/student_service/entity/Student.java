package com.bhuvanesh.onboarding.student_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull(message = "Age must not be null")
    @Min(value = 1, message = "Age must be greater than 0")
    @Column(nullable = false)
    private Integer age;
}
