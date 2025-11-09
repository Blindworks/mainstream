package com.mainstream.user.service.impl;

import com.mainstream.user.dto.UserDto;
import com.mainstream.user.dto.UserRegistrationDto;
import com.mainstream.user.entity.User;
import com.mainstream.user.exception.ResourceNotFoundException;
import com.mainstream.user.exception.UserAlreadyExistsException;
import com.mainstream.user.mapper.UserMapper;
import com.mainstream.user.repository.UserRepository;
import com.mainstream.user.service.FileStorageService;
import com.mainstream.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        log.debug("Registering new user with email: {}", registrationDto.getEmail());

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + registrationDto.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(registrationDto);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toDto);
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .filter(User::getIsActive)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.debug("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        userMapper.updateEntityFromDto(existingUser, userDto);
        User updatedUser = userRepository.save(existingUser);

        log.info("User updated successfully with ID: {}", id);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void deactivateUser(Long id) {
        log.debug("Deactivating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User deactivated successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void activateUser(Long id) {
        log.debug("Activating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setIsActive(true);
        userRepository.save(user);

        log.info("User activated successfully with ID: {}", id);
    }

    @Override
    public Page<UserDto> findAllUsers(Pageable pageable) {
        log.debug("Finding all active users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAllActiveUsers(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> findPublicUsers(Pageable pageable) {
        log.debug("Finding all public users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAllPublicUsers(pageable)
                .map(userMapper::toDto);
    }

    @Override
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}, page: {}, size: {}", 
                  searchTerm, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findUsersBySearchTerm(searchTerm, pageable)
                .map(userMapper::toDto);
    }

    @Override
    public long countActiveUsers() {
        long count = userRepository.countActiveUsers();
        log.debug("Active users count: {}", count);
        return count;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UserDto updateRole(Long id, User.Role role) {
        log.debug("Updating role for user ID: {} to: {}", id, role);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        log.info("User role updated successfully for ID: {} to: {}", id, role);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public Page<UserDto> findUsersByRole(User.Role role, Pageable pageable) {
        log.debug("Finding users by role: {}, page: {}, size: {}",
                  role, pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findUsersByRole(role, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto updateAvatar(Long id, String avatarUrl) {
        log.debug("Updating avatar for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Delete old avatar if it exists and is not a UI Avatars URL
        String oldAvatarUrl = user.getProfilePictureUrl();
        if (oldAvatarUrl != null && !oldAvatarUrl.contains("ui-avatars.com")) {
            fileStorageService.deleteAvatar(oldAvatarUrl);
        }

        user.setProfilePictureUrl(avatarUrl);
        User updatedUser = userRepository.save(user);

        log.info("Avatar updated successfully for user ID: {}", id);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public UserDto deleteAvatar(Long id) {
        log.debug("Deleting avatar for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        String avatarUrl = user.getProfilePictureUrl();
        if (avatarUrl != null && !avatarUrl.contains("ui-avatars.com")) {
            fileStorageService.deleteAvatar(avatarUrl);
        }

        user.setProfilePictureUrl(null);
        User updatedUser = userRepository.save(user);

        log.info("Avatar deleted successfully for user ID: {}", id);
        return userMapper.toDto(updatedUser);
    }
}