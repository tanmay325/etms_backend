package com.example.etms_backend.repository;

import com.example.etms_backend.entity.TaskUpdate;
import com.example.etms_backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskUpdateRepository extends JpaRepository<TaskUpdate, Long> {
    List<TaskUpdate> findByTaskIdOrderByUpdateTimeDesc(Long taskId);
}