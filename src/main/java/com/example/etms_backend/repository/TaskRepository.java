package com.example.etms_backend.repository;

import com.example.etms_backend.entity.Task;
import com.example.etms_backend.entity.User;
import com.example.etms_backend.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Required for Filtering
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByEmployee(User employee);
    List<Task> findByAdmin(User admin);
    
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    @Query("SELECT t.employee.name, COUNT(t) FROM Task t WHERE t.status = 'APPROVED' GROUP BY t.employee.name")
    List<Object[]> countApprovedTasksByEmployee();

    long countByCreatedAtAfter(LocalDateTime date);
    long countByStatus(TaskStatus status);
    long countByStatusNotInAndDeadlineBefore(java.util.Collection<TaskStatus> statuses, LocalDateTime date);
    long countByEmployeeAndCreatedAtAfter(User employee, LocalDateTime date);
    long countByEmployeeAndStatus(User employee, TaskStatus status);
}