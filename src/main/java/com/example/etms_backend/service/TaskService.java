package com.example.etms_backend.service;

import com.example.etms_backend.dto.TaskRequest;
import com.example.etms_backend.dto.UpdateProgressRequest;
import com.example.etms_backend.entity.*;
import com.example.etms_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TaskUpdateRepository taskUpdateRepository;
    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    FcmService fcmService;

    /**
     * Dynamic filtering logic for Admin and Employee task lists.
     */
    public List<Task> getFilteredTasks(User currentUser, String status, String priority, 
                                      Long employeeId, LocalDateTime date, Sort sort) {
        return taskRepository.findAll((Specification<Task>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (currentUser.getRole() == Role.ROLE_EMPLOYEE) {
                predicates.add(cb.equal(root.get("employee"), currentUser));
            } else if (employeeId != null) {
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), TaskStatus.valueOf(status)));
            }
            if (priority != null && !priority.isEmpty()) {
                predicates.add(cb.equal(root.get("priority"), Priority.valueOf(priority)));
            }
            if (date != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("deadline"), date));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, sort);
    }

    public Task createTask(TaskRequest request, User admin) {
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        
        task.setPriority(Priority.valueOf(request.getPriority()));
        
        task.setDeadline(request.getDeadline());
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedAt(LocalDateTime.now());
        task.setAdmin(admin);
        task.setEmployee(employee);

        Task savedTask = taskRepository.save(task);
        
        // Notify Employee - Task ID for redirection
        createInAppNotification(employee, "New Task Assigned", 
                "Admin assigned you a new task: " + savedTask.getTitle(), savedTask.getId());
        
        return savedTask;
    }

    public List<TaskUpdate> getTaskUpdates(Long taskId) {
        return taskUpdateRepository.findByTaskIdOrderByUpdateTimeDesc(taskId);
    }

    public Task updateTaskProgress(Long taskId, UpdateProgressRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskUpdate update = new TaskUpdate();
        update.setTask(task);
        update.setComment(request.getComment());
        update.setProofUrl(request.getProofUrl());
        update.setProgressPercentage(request.getProgressPercentage());
        update.setUpdateTime(LocalDateTime.now());
        taskUpdateRepository.save(update);

        // Update task status based on progress and optional manual status (e.g. ON_HOLD)
        if (request.getProgressPercentage() == 100) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            task.setStatus(TaskStatus.valueOf(request.getStatus()));
        } else {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        
        Task savedTask = taskRepository.save(task);

        // Notify Admin of progress - Added Task ID for redirection
        createInAppNotification(savedTask.getAdmin(), "Task Update", 
                savedTask.getEmployee().getName() + " updated progress for: " + savedTask.getTitle(), savedTask.getId());

        return savedTask;
    }

    public Task changeTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        Task savedTask = taskRepository.save(task);

        String title = (status == TaskStatus.APPROVED) ? "Task Approved" : "Task Rejected";
        String msg = (status == TaskStatus.APPROVED) ? "Great job! Your task has been approved: " 
                                                     : "Please check feedback and rework on: ";
        createInAppNotification(savedTask.getEmployee(), title, msg + savedTask.getTitle(), savedTask.getId());

        return savedTask;
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        List<TaskUpdate> updates = taskUpdateRepository.findByTaskIdOrderByUpdateTimeDesc(taskId);
        taskUpdateRepository.deleteAll(updates);
        taskRepository.delete(task);
    }

    private void createInAppNotification(User user, String title, String message, Long taskId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTaskId(taskId); // Link notification to the task
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
        
        if (user.getFcmToken() != null) {
            fcmService.sendNotification(user.getFcmToken(), title, message);
        }
    }


    public List<Task> getTasksForEmployee(User employee) {
        return taskRepository.findByEmployee(employee);
    }

    public List<Task> getAllTasksForAdmin(User admin) {
        return taskRepository.findByAdmin(admin);
    }
}