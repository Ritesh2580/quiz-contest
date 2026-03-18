package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.CreateQuizRequest;
import com.quizcontest.dto.QuizDTO;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IQuizService;
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

@WebMvcTest(controllers = QuizController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IQuizService quizService;

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
    void createQuizReturnsCreatedQuiz() throws Exception {
        UUID creatorId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();
        CreateQuizRequest request = CreateQuizRequest.builder()
                .title("General Knowledge Quiz")
                .description("Test your knowledge")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .durationMinutes(60)
                .bannerImageId(UUID.randomUUID())
                .build();
        QuizDTO response = QuizDTO.builder()
                .id(quizId)
                .title(request.getTitle())
                .description(request.getDescription())
                .bannerImageId(request.getBannerImageId())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .durationMinutes(request.getDurationMinutes())
                .createdBy(creatorId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizService.createQuiz(any(CreateQuizRequest.class), eq(creatorId))).willReturn(response);

        mockMvc.perform(post("/api/v1/quizzes")
                        .param("createdBy", creatorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(quizId.toString()))
                .andExpect(jsonPath("$.title").value(request.getTitle()));
    }

    @Test
    void getQuizByIdReturnsQuiz() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuizDTO response = QuizDTO.builder()
                .id(quizId)
                .title("General Knowledge Quiz")
                .description("Test your knowledge")
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizService.getQuizById(quizId)).willReturn(response);

        mockMvc.perform(get("/api/v1/quizzes/{id}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quizId.toString()));
    }

    @Test
    void getAllQuizzesReturnsQuizzes() throws Exception {
        QuizDTO response = QuizDTO.builder()
                .id(UUID.randomUUID())
                .title("General Knowledge Quiz")
                .description("Test your knowledge")
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizService.getAllQuizzes()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getId().toString()));
    }

    @Test
    void getQuizzesByCreatorReturnsQuizzes() throws Exception {
        UUID creatorId = UUID.randomUUID();
        QuizDTO response = QuizDTO.builder()
                .id(UUID.randomUUID())
                .title("General Knowledge Quiz")
                .description("Test your knowledge")
                .createdBy(creatorId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(quizService.getQuizzesByCreator(creatorId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/quizzes/creator/{creatorId}", creatorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].createdBy").value(creatorId.toString()));
    }

    @Test
    void updateQuizReturnsUpdatedQuiz() throws Exception {
        UUID quizId = UUID.randomUUID();
        CreateQuizRequest request = CreateQuizRequest.builder()
                .title("Updated Quiz")
                .description("Updated description")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .durationMinutes(45)
                .bannerImageId(UUID.randomUUID())
                .build();
        QuizDTO response = QuizDTO.builder()
                .id(quizId)
                .title(request.getTitle())
                .description(request.getDescription())
                .bannerImageId(request.getBannerImageId())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .durationMinutes(request.getDurationMinutes())
                .createdBy(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(2)
                .build();

        given(quizService.updateQuiz(eq(quizId), any(CreateQuizRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/quizzes/{id}", quizId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quizId.toString()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void deleteQuizReturnsNoContent() throws Exception {
        UUID quizId = UUID.randomUUID();
        willDoNothing().given(quizService).deleteQuiz(quizId);

        mockMvc.perform(delete("/api/v1/quizzes/{id}", quizId))
                .andExpect(status().isNoContent());
    }

    @Test
    void isQuizActiveReturnsBoolean() throws Exception {
        UUID quizId = UUID.randomUUID();
        given(quizService.isQuizActive(quizId)).willReturn(true);

        mockMvc.perform(get("/api/v1/quizzes/{id}/active", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
}
