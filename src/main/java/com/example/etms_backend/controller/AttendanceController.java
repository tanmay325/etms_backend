package com.example.etms_backend.controller;

import com.example.etms_backend.entity.Attendance;
import com.example.etms_backend.entity.User;
import com.example.etms_backend.repository.AttendanceRepository;
import com.example.etms_backend.repository.UserRepository;
import com.example.etms_backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    // Set your Office coordinates here (Target Location)
    private static final double OFFICE_LAT = 18.561278848656077;
    private static final double OFFICE_LON = 73.94472859501289;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestParam double lat, @RequestParam double lon) {
        User user = getCurrentUser();
        
        // 1. Check Geofencing (100m range)
        double distance = calculateDistance(lat, lon, OFFICE_LAT, OFFICE_LON);
        if (distance > 100) {
            return ResponseEntity.badRequest().body("You are out of range (" + (int)distance + "m). Must be within 100m of office.");
        }

        // 2. Check if already checked in today
        Optional<Attendance> existing = attendanceRepository.findByUserAndDate(user, LocalDate.now());
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body("You have already checked in today.");
        }

        // 3. Save Attendance record
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setDate(LocalDate.now());
        attendance.setLatitude(lat);
        attendance.setLongitude(lon);
        attendance.setInRange(true);

        attendanceRepository.save(attendance);
        return ResponseEntity.ok("Checked in successfully!");
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut() {
        User user = getCurrentUser();
        Attendance att = attendanceRepository.findByUserAndDate(user, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No check-in record found for today."));
        
        if (att.getCheckOutTime() != null) {
            return ResponseEntity.badRequest().body("You have already checked out today.");
        }

        att.setCheckOutTime(LocalDateTime.now());
        attendanceRepository.save(att);
        return ResponseEntity.ok("Checked out successfully");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        User user = getCurrentUser();
        Optional<Attendance> att = attendanceRepository.findByUserAndDate(user, LocalDate.now());
        
        if (att.isPresent()) {
            String status = att.get().getCheckOutTime() == null ? "CHECKED_IN" : "CHECKED_OUT";
            return ResponseEntity.ok(Map.of("status", status));
        }
        return ResponseEntity.ok(Map.of("status", "NOT_CHECKED_IN"));
    }

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).get();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance in meters
        double earthRadius = 6371000; 
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllAttendance(@RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(attendanceRepository.findByDate(targetDate));
    }
}