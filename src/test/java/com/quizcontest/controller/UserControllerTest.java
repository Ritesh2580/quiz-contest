package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.CreateUserRequest;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private com.quizcontest.service.interfaces.IUserService userService;

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
    void createUserReturnsCreatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .email("user@example.com")
                .password("securePassword123")
                .name("Jane Doe")
                .role("CREATOR")
                .build();
        UserDTO response = UserDTO.builder()
                .id(userId)
                .email(request.getEmail())
                .name(request.getName())
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(userService.createUser(any(CreateUserRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    void getUserByIdReturnsUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDTO response = UserDTO.builder()
                .id(userId)
                .email("user@example.com")
                .name("Jane Doe")
                .role("CREATOR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(userService.getUserById(userId)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void getAllUsersReturnsUsers() throws Exception {
        UserDTO response = UserDTO.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("Jane Doe")
                .role("CREATOR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(userService.getAllUsers()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user@example.com"));
    }

    @Test
    void updateUserReturnsUpdatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        CreateUserRequest request = CreateUserRequest.builder()
                .email("user@example.com")
                .password("securePassword123")
                .name("Jane Doe")
                .role("CREATOR")
                .build();
        UserDTO response = UserDTO.builder()
                .id(userId)
                .email(request.getEmail())
                .name(request.getName())
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(2)
                .build();

        given(userService.updateUser(eq(userId), any(CreateUserRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void deleteUserReturnsNoContent() throws Exception {
        UUID userId = UUID.randomUUID();
        willDoNothing().given(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());
    }
}
