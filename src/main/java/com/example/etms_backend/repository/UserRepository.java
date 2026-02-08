package com.example.etms_backend.repository;

import com.example.etms_backend.entity.User;
import com.example.etms_backend.entity.Role; // Added
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // Added
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role); // Now this will work

    long countByRole(Role role);
}