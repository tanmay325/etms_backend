package com.example.etms_backend.dto;

import lombok.Data;
@Data
public class UpdateProgressRequest {
    private String comment;
    private int progressPercentage;
    private String proofUrl;
    private String status; 
}