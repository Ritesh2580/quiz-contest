package com.quizcontest.controller;

import com.quizcontest.dto.QuizLeaderboardDTO;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IQuizLeaderboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuizLeaderboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = QuizLeaderboardController.class)
class QuizLeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IQuizLeaderboardService quizLeaderboardService;

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
    void createOrUpdateLeaderboardEntryReturnsCreatedEntry() throws Exception {
        UUID quizId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        QuizLeaderboardDTO response = QuizLeaderboardDTO.builder()
                .id(entryId)
                .quizId(quizId)
                .userId(userId)
                .rank(1)
                .score(95)
                .timeTakenSeconds(600)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizLeaderboardService.createOrUpdateLeaderboardEntry(quizId, userId, 95, 600))
                .willReturn(response);

        mockMvc.perform(post("/api/v1/leaderboard")
                        .param("quizId", quizId.toString())
                        .param("userId", userId.toString())
                        .param("score", "95")
                        .param("timeTakenSeconds", "600"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(entryId.toString()))
                .andExpect(jsonPath("$.rank").value(1))
                .andExpect(jsonPath("$.score").value(95));
    }

    @Test
    void getLeaderboardEntryByIdReturnsEntry() throws Exception {
        UUID entryId = UUID.randomUUID();
        QuizLeaderboardDTO response = QuizLeaderboardDTO.builder()
                .id(entryId)
                .quizId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .rank(2)
                .score(80)
                .timeTakenSeconds(700)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizLeaderboardService.getLeaderboardEntryById(entryId)).willReturn(response);

        mockMvc.perform(get("/api/v1/leaderboard/{id}", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entryId.toString()))
                .andExpect(jsonPath("$.rank").value(2));
    }

    @Test
    void getLeaderboardByQuizIdReturnsEntries() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuizLeaderboardDTO response = QuizLeaderboardDTO.builder()
                .id(UUID.randomUUID())
                .quizId(quizId)
                .userId(UUID.randomUUID())
                .rank(1)
                .score(100)
                .timeTakenSeconds(500)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizLeaderboardService.getLeaderboardByQuizId(quizId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/leaderboard/quiz/{quizId}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(quizId.toString()));
    }

    @Test
    void getUserRankReturnsRank() throws Exception {
        UUID quizId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        given(quizLeaderboardService.getUserRank(quizId, userId)).willReturn(3);

        mockMvc.perform(get("/api/v1/leaderboard/quiz/{quizId}/rank", quizId)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));
    }

    @Test
    void getTopLeaderboardEntriesReturnsEntries() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuizLeaderboardDTO response = QuizLeaderboardDTO.builder()
                .id(UUID.randomUUID())
                .quizId(quizId)
                .userId(UUID.randomUUID())
                .rank(1)
                .score(100)
                .timeTakenSeconds(500)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizLeaderboardService.getTopLeaderboardEntries(quizId, 5)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/leaderboard/quiz/{quizId}/top", quizId)
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(100));
    }
}
