package com.example.etms_backend.dto;

import com.example.etms_backend.entity.Priority;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private String priority;
    private LocalDateTime deadline;
    private Long employeeId;
    private String startDate;
    private String estimatedTime;
    private boolean isDraft;
}