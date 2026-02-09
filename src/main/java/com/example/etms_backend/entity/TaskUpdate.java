package com.example.etms_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore; 

@Entity
@Table(name = "task_updates")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TaskUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    @JsonIgnore // CRITICAL: Add this to prevent the recursive JSON error
    private Task task;

    private String comment;
    private String proofUrl; 
    private int progressPercentage;
    private LocalDateTime updateTime;
}
