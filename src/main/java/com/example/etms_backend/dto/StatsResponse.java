package com.example.etms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class StatsResponse {
    private long totalEmployees;
    private long tasksAssignedToday;    
    private long tasksCompleted;
    private long tasksPending;
    private long tasksOverdue;
    private Map<String, Long> statusCounts;
    private Map<String, Long> employeeTaskCounts;
}