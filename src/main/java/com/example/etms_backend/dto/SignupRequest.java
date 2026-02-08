package com.example.etms_backend.dto;

import com.example.etms_backend.entity.Role;
import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String password;
    private String name;
    private Role role; // ADMIN or EMPLOYEE
}