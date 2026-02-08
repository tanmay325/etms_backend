package com.example.etms_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority; 

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime startDate;
    private String estimatedTime; // e.g., "5 hours"
    private boolean isDraft; //

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin; // Who assigned it

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee; // Who is doing it
}
