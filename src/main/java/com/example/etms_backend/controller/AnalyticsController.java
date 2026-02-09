package com.example.etms_backend.controller;

import com.example.etms_backend.dto.StatsResponse;
import com.example.etms_backend.entity.Role;
import com.example.etms_backend.entity.TaskStatus;
import com.example.etms_backend.entity.User;
import com.example.etms_backend.repository.TaskRepository;
import com.example.etms_backend.repository.UserRepository;
import com.example.etms_backend.security.UserDetailsImpl;
import com.example.etms_backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportService reportService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<?> getSummary() {
        long totalEmployees = userRepository.countByRole(Role.ROLE_EMPLOYEE);
        LocalDateTime startOfToday = LocalDateTime.now().with(java.time.LocalTime.MIN);
        long todayTasks = taskRepository.countByCreatedAtAfter(startOfToday);
        long completed = taskRepository.countByStatus(TaskStatus.COMPLETED) + taskRepository.countByStatus(TaskStatus.APPROVED);
        long pending = taskRepository.countByStatus(TaskStatus.PENDING) + taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        long overdue = taskRepository.countByStatusNotInAndDeadlineBefore(java.util.List.of(TaskStatus.APPROVED, TaskStatus.REJECTED), LocalDateTime.now());
        
        Map<String, Long> statusMap = taskRepository.countTasksByStatus().stream()
                .collect(Collectors.toMap(obj -> obj[0].toString(), obj -> (Long) obj[1]));
        Map<String, Long> employeeMap = taskRepository.countApprovedTasksByEmployee().stream()
                .collect(Collectors.toMap(obj -> obj[0].toString(), obj -> (Long) obj[1]));

        return ResponseEntity.ok(new StatsResponse(totalEmployees, todayTasks, completed, pending, overdue, statusMap, employeeMap));
    }

    @GetMapping("/my-summary")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<?> getMySummary() {
        User user = getCurrentUser(); 

        LocalDateTime startOfToday = LocalDateTime.now().with(java.time.LocalTime.MIN);
        long today = taskRepository.countByEmployeeAndCreatedAtAfter(user, startOfToday);
        long pending = taskRepository.countByEmployeeAndStatus(user, TaskStatus.PENDING);
        long inProgress = taskRepository.countByEmployeeAndStatus(user, TaskStatus.IN_PROGRESS);
        long completed = taskRepository.countByEmployeeAndStatus(user, TaskStatus.COMPLETED);

        return ResponseEntity.ok(Map.of(
            "today", today,
            "pending", pending,
            "inProgress", inProgress,
            "completed", completed
        ));
    }

    @GetMapping("/export-pdf")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<?> exportToPdf() {
        ByteArrayInputStream bis = reportService.generateTaskReport();
        var headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=tasks_report.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).get();
    }
}