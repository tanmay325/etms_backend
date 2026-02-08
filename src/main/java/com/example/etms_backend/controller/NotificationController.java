package com.example.etms_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.etms_backend.repository.NotificationRepository;
import com.example.etms_backend.repository.UserRepository;
import com.example.etms_backend.security.UserDetailsImpl;
import com.example.etms_backend.entity.User;
import com.example.etms_backend.entity.Notification;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyNotifications() {
        User user = getCurrentUser();
        return ResponseEntity.ok(notificationRepository.findByUserOrderByCreatedAtDesc(user));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id).orElseThrow();
        n.setRead(true);
        notificationRepository.save(n);
        return ResponseEntity.ok("Marked as read");
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).get();
    }
}
