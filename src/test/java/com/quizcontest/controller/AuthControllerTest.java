package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.CreateUserRequest;
import com.quizcontest.dto.LoginRequest;
import com.quizcontest.dto.LoginResponse;
import com.quizcontest.dto.UserDTO;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;


    @MockBean
    private UserRepository userRepository;

    @MockBean
    private org.modelmapper.ModelMapper modelMapper;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    private com.quizcontest.security.JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void registerReturnsCreatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .email("user@example.com")
                .password("securePassword123")
                .name("Jane Doe")
                .role("CREATOR")
                .build();
        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .email(request.getEmail())
                .name(request.getName())
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(userService.createUser(any(CreateUserRequest.class))).willReturn(userDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.role").value(request.getRole()));
    }

    @Test
    void loginReturnsToken() throws Exception {
        UUID userId = UUID.randomUUID();
        LoginRequest request = LoginRequest.builder()
                .email("user@example.com")
                .password("securePassword123")
                .build();
        LoginResponse response = LoginResponse.builder()
                .id(userId)
                .email(request.getEmail())
                .name("Jane Doe")
                .role("CREATOR")
                .token("jwt-token")
                .loginTime(LocalDateTime.now())
                .build();

        given(userService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}
