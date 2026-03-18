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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.quizcontest.config.RedisCacheConfig.CACHE_USERS;

/**
 * Service implementation for managing {@link User} entities.
 * <p>
 * This service provides comprehensive user management functionality including:
 * <ul>
 *   <li>User registration and creation</li>
 *   <li>User authentication and login</li>
 *   <li>User retrieval by ID or all users</li>
 *   <li>User profile updates</li>
 *   <li>User deletion</li>
 * </ul>
 * </p>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching to improve performance. Cache entries are
 * automatically evicted when users are created, updated, or deleted.
 * </p>
 *
 * <p><b>Security:</b></p>
 * <p>
 * Passwords are encoded using the configured {@link PasswordEncoder}.
 * JWT tokens are generated for authenticated sessions.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IUserService
 * @see UserRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements IUserService {

    /** Repository for accessing user data. */
    private final UserRepository userRepository;

    /** Encoder for hashing user passwords. */
    private final PasswordEncoder passwordEncoder;

    /** Service for generating JWT tokens. */
    private final JwtService jwtService;

    /** Manager for handling authentication requests. */
    private final AuthenticationManager authenticationManager;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Creates a new user in the system.
     * <p>
     * The user's password is encoded before storage. After successful creation,
     * all user cache entries are evicted to ensure consistency.
     * </p>
     *
     * @param request the user creation request containing email, password, name, and role
     * @return the created user as a DTO
     */
    @Override
    @CacheEvict(value = CACHE_USERS, allEntries = true)
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
     * Authenticates a user and generates a JWT token.
     * <p>
     * This method uses the configured {@link AuthenticationManager} to validate
     * credentials. Upon successful authentication, a JWT token is generated for
     * subsequent authorized requests.
     * </p>
     *
     * @param request the login request containing email and password
     * @return a login response containing the user details and JWT token
     * @throws ResourceNotFoundException if the user is not found
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
     * Retrieves a user by their unique identifier.
     * <p>
     * This method is cached using Redis with the key pattern: {@code users:{id}}.
     * Subsequent calls with the same ID will return cached data until the cache
     * is evicted.
     * </p>
     *
     * @param id the unique identifier of the user
     * @return the user as a DTO
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_USERS, key = "#id")
    public UserDTO getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToDTO(user);
    }

    /**
     * Retrieves all users in the system.
     * <p>
     * This method is cached using Redis with the key: {@code users:all}.
     * The cache has a TTL of 1 hour as configured in {@link com.quizcontest.config.RedisCacheConfig}.
     * </p>
     *
     * @return a list of all users as DTOs
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_USERS, key = "'all'")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing user's information.
     * <p>
     * This method updates the user's cache entry and evicts the 'all' users cache
     * to ensure consistency. The version number is incremented for optimistic locking.
     * </p>
     *
     * @param id the unique identifier of the user to update
     * @param request the update request containing new user information
     * @return the updated user as a DTO
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    @Override
    @CachePut(value = CACHE_USERS, key = "#id")
    @CacheEvict(value = CACHE_USERS, key = "'all'")
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
     * Deletes a user from the system.
     * <p>
     * This method removes the user from the database and evicts all user cache
     * entries to ensure data consistency across the application.
     * </p>
     *
     * @param id the unique identifier of the user to delete
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    @Override
    @CacheEvict(value = CACHE_USERS, allEntries = true)
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        userRepository.delete(user);
    }

    /**
     * Converts a {@link User} entity to a {@link UserDTO}.
     * <p>
     * Uses ModelMapper for basic mapping and manually sets the name field
     * from the entity's full name.
     * </p>
     *
     * @param user the user entity to convert
     * @return the converted user DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.setName(user.getFullName());
        return userDTO;
    }
}