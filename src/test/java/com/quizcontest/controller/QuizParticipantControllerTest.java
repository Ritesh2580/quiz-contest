package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.JoinQuizRequest;
import com.quizcontest.dto.QuizParticipantDTO;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IQuizParticipantService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuizParticipantController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = QuizParticipantController.class)
class QuizParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IQuizParticipantService quizParticipantService;

    @MockBean
    private UserService authUserService;


    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void joinQuizReturnsCreatedParticipant() throws Exception {
        UUID quizId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        JoinQuizRequest request = JoinQuizRequest.builder()
                .quizId(quizId)
                .userId(userId)
                .build();
        QuizParticipantDTO response = QuizParticipantDTO.builder()
                .id(participantId)
                .quizId(quizId)
                .userId(userId)
                .status("joined")
                .score(0)
                .startedAt(null)
                .completedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizParticipantService.joinQuiz(any(JoinQuizRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/participants/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(participantId.toString()))
                .andExpect(jsonPath("$.quizId").value(quizId.toString()));
    }

    @Test
    void getParticipantByIdReturnsParticipant() throws Exception {
        UUID participantId = UUID.randomUUID();
        QuizParticipantDTO response = QuizParticipantDTO.builder()
                .id(participantId)
                .quizId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status("joined")
                .score(0)
                .startedAt(null)
                .completedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizParticipantService.getParticipantById(participantId)).willReturn(response);

        mockMvc.perform(get("/api/v1/participants/{id}", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(participantId.toString()));
    }

    @Test
    void getParticipantsByQuizIdReturnsParticipants() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuizParticipantDTO response = QuizParticipantDTO.builder()
                .id(UUID.randomUUID())
                .quizId(quizId)
                .userId(UUID.randomUUID())
                .status("joined")
                .score(0)
                .startedAt(null)
                .completedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizParticipantService.getParticipantsByQuizId(quizId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/participants/quiz/{quizId}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(quizId.toString()));
    }

    @Test
    void getParticipationsByUserIdReturnsParticipations() throws Exception {
        UUID userId = UUID.randomUUID();
        QuizParticipantDTO response = QuizParticipantDTO.builder()
                .id(UUID.randomUUID())
                .quizId(UUID.randomUUID())
                .userId(userId)
                .status("joined")
                .score(0)
                .startedAt(null)
                .completedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizParticipantService.getParticipationsByUserId(userId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/participants/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()));
    }

    @Test
    void startQuizReturnsParticipant() throws Exception {
        UUID participantId = UUID.randomUUID();
        QuizParticipantDTO response = QuizParticipantDTO.builder()
                .id(participantId)
                .quizId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status("in_progress")
                .score(0)
                .startedAt(LocalDateTime.now())
                .completedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(2)
                .build();

        given(quizParticipantService.startQuiz(participantId)).willReturn(response);

        mockMvc.perform(post("/api/v1/participants/{id}/start", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("in_progress"));
    }

    @Test
    void completeQuizReturnsParticipant() throws Exception {
        UUID participantId = UUID.randomUUID();
        QuizParticipantDTO response = QuizParticipantDTO.builder()
                .id(participantId)
                .quizId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .status("completed")
                .score(85)
                .startedAt(LocalDateTime.now().minusMinutes(10))
                .completedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(3)
                .build();

        given(quizParticipantService.completeQuiz(participantId, 85)).willReturn(response);

        mockMvc.perform(post("/api/v1/participants/{id}/complete", participantId)
                        .param("finalScore", "85"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.score").value(85));
    }
}
