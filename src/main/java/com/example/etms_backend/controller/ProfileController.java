package com.example.etms_backend.controller;

import com.example.etms_backend.entity.User;
import com.example.etms_backend.repository.UserRepository;
import com.example.etms_backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getMyProfile() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return ResponseEntity.ok(userRepository.findById(userDetails.getId()).get());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody User profileData) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).get();

        user.setName(profileData.getName());
        user.setDesignation(profileData.getDesignation());
        user.setDepartment(profileData.getDepartment());
        user.setContactDetails(profileData.getContactDetails());
        user.setProfilePictureUrl(profileData.getProfilePictureUrl());

        userRepository.save(user);
        return ResponseEntity.ok("Profile updated successfully");
    }
}