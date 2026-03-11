package com.quizcontest.service;

import com.quizcontest.dto.CreateUserRequest;
import com.quizcontest.dto.LoginRequest;
import com.quizcontest.dto.LoginResponse;
import com.quizcontest.dto.UserDTO;
import com.quizcontest.entity.User;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtService;
import com.quizcontest.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for User entity
 * Handles business logic for user management
 * Implements IUserService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    /**
     * Create a new user
     */
    @Override
    public UserDTO createUser(CreateUserRequest request) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getName())
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .isActive(true)
                .username(request.getEmail())
                .build();
        
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Authenticate user
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(request.getEmail()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getFullName())
                .role(user.getRole())
                .token(token)
                .loginTime(LocalDateTime.now())
                .build();
    }

    /**
     * Get user by ID
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToDTO(user);
    }

    /**
     * Get all users
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update user
     */
    @Override
    public UserDTO updateUser(UUID id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        // Use ModelMapper to map request to entity
        modelMapper.map(request, user);
        user.setFullName(request.getName());
        user.setUpdatedAt(LocalDateTime.now());
        user.setVersion(user.getVersion() + 1);
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Delete user
     */
    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userRepository.delete(user);
    }

    /**
     * Convert User entity to UserDTO using ModelMapper
     */
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.setName(user.getFullName());
        return userDTO;
    }
}