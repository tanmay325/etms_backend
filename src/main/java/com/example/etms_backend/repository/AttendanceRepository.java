package com.example.etms_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.etms_backend.entity.Attendance;
import com.example.etms_backend.entity.User;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;


public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserAndDate(User user, LocalDate date);
    List<Attendance> findByDate(LocalDate date);
}
