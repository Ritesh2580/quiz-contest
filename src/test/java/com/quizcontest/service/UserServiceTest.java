package com.quizcontest.service;

import com.quizcontest.dto.CreateUserRequest;
import com.quizcontest.dto.LoginRequest;
import com.quizcontest.dto.LoginResponse;
import com.quizcontest.dto.UserDTO;
import com.quizcontest.entity.User;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private UUID userId;
    private String email = "test@example.com";

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email(email)
                .fullName("Test User")
                .role("player")
                .isActive(true)
                .version(1)
                .build();

        userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setEmail(email);
        userDTO.setName("Test User");
        userDTO.setRole("player");
    }

    @Test
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail(email);
        request.setPassword("password");
        request.setName("Test User");
        request.setRole("player");

        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("password");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        LoginResponse result = userService.login(request);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("token", result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("password");

        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> userService.login(request));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        List<UserDTO> result = userService.getAllUsers();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void updateUser_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Updated Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Use doNothing for void methods or lenient stubbing if needed, 
        // but here we just need to ensure the map call is handled correctly.
        // Since we removed the map stubbing, let's add it back with any() to be safe.
        lenient().when(userRepository.save(any(User.class))).thenReturn(user);
        lenient().when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userId, request);

        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).delete(user);
    }
}
