package com.mainstream.user.controller;

import com.mainstream.user.dto.UserDto;
import com.mainstream.user.entity.User;
import com.mainstream.user.service.FileStorageService;
import com.mainstream.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.debug("Getting user by ID: {}", id);
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-User-Role") String currentUserRole) {
        
        // Users can only update their own profile, unless they are admin/moderator
        if (!id.equals(currentUserId) && 
            !("ADMIN".equals(currentUserRole) || "MODERATOR".equals(currentUserRole))) {
            return ResponseEntity.status(403).build();
        }

        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.findAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<UserDto>> getPublicUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.findPublicUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam String term,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.searchUsers(term, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<Long> getActiveUsersCount() {
        long count = userService.countActiveUsers();
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUserRole(
            @PathVariable Long id,
            @RequestParam User.Role role) {
        
        UserDto updatedUser = userService.updateRole(id, role);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<Page<UserDto>> getUsersByRole(
            @PathVariable User.Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(sortBy).descending() :
                   Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.findUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-User-Role") String currentUserRole) {

        log.debug("Uploading avatar for user {}", id);

        // Users can only update their own avatar, unless they are admin/moderator
        if (!id.equals(currentUserId) &&
            !("ADMIN".equals(currentUserRole) || "MODERATOR".equals(currentUserRole))) {
            return ResponseEntity.status(403).body("You can only update your own avatar");
        }

        // Validate image
        if (!fileStorageService.isValidImage(file)) {
            return ResponseEntity.badRequest().body("Invalid image file. Allowed types: JPEG, PNG, GIF, WEBP. Max size: 5MB");
        }

        try {
            // Store the file
            String avatarUrl = fileStorageService.storeAvatar(file, id);

            // Update user profile with new avatar URL
            UserDto updatedUser = userService.updateAvatar(id, avatarUrl);

            log.info("Avatar uploaded successfully for user {}", id);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}", id, e);
            return ResponseEntity.internalServerError().body("Failed to upload avatar: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<?> deleteAvatar(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestHeader("X-User-Role") String currentUserRole) {

        log.debug("Deleting avatar for user {}", id);

        // Users can only delete their own avatar, unless they are admin/moderator
        if (!id.equals(currentUserId) &&
            !("ADMIN".equals(currentUserRole) || "MODERATOR".equals(currentUserRole))) {
            return ResponseEntity.status(403).body("You can only delete your own avatar");
        }

        try {
            UserDto updatedUser = userService.deleteAvatar(id);
            log.info("Avatar deleted successfully for user {}", id);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Failed to delete avatar for user {}", id, e);
            return ResponseEntity.internalServerError().body("Failed to delete avatar: " + e.getMessage());
        }
    }
}