package com.example.etms_backend.controller;

import com.example.etms_backend.dto.TaskRequest;
import com.example.etms_backend.dto.UpdateProgressRequest;
import com.example.etms_backend.entity.Role;
import com.example.etms_backend.entity.Task;
import com.example.etms_backend.entity.TaskStatus;
import com.example.etms_backend.entity.User;
import com.example.etms_backend.repository.UserRepository;
import com.example.etms_backend.security.UserDetailsImpl;
import com.example.etms_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.etms_backend.dto.UrlResponse;
import com.example.etms_backend.service.S3Service;
import com.example.etms_backend.entity.TaskUpdate;
import java.util.List;
import com.example.etms_backend.repository.TaskRepository;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    TaskService taskService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    S3Service s3Service;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTask(@RequestBody TaskRequest taskRequest) {
        User admin = getCurrentUser();
        return ResponseEntity.ok(taskService.createTask(taskRequest, admin));
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<UrlResponse> getPresignedUrl(@RequestParam String fileName) {
        String url = s3Service.generatePresignedUrl(fileName);
        return ResponseEntity.ok(new UrlResponse(url));
    }

    @GetMapping("/{id}/updates")
    public ResponseEntity<List<TaskUpdate>> getTaskUpdates(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskUpdates(id));
    }

    /**
     * UPDATED: Handles dynamic filtering and sorting
     */
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        User user = getCurrentUser();
        
        // Handle sorting
        String sortField = (sortBy == null || sortBy.isEmpty()) ? "deadline" : sortBy;
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortField).descending() : Sort.by(sortField).ascending();

        List<Task> tasks = taskService.getFilteredTasks(user, status, priority, employeeId, null, sort);
        
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{id}/progress")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody UpdateProgressRequest request) {
        return ResponseEntity.ok(taskService.updateTaskProgress(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.changeTaskStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).get();
    }

    @GetMapping("/single/{id}")
    public ResponseEntity<Task> getSingleTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskRepository.findById(id).orElseThrow());
    }
    }