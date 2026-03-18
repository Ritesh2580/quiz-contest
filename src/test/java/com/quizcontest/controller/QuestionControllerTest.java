package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.CreateQuestionRequest;
import com.quizcontest.dto.QuestionDTO;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IQuestionService;
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

@WebMvcTest(controllers = QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IQuestionService questionService;

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
    void createQuestionReturnsCreatedQuestion() throws Exception {
        UUID quizId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .quizId(quizId)
                .questionText("What is the capital of France?")
                .questionType("multiple_choice")
                .timeLimitSeconds(30)
                .displayOrder(1)
                .build();
        QuestionDTO response = QuestionDTO.builder()
                .id(questionId)
                .quizId(quizId)
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .displayOrder(request.getDisplayOrder())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionService.createQuestion(any(CreateQuestionRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(questionId.toString()))
                .andExpect(jsonPath("$.quizId").value(quizId.toString()))
                .andExpect(jsonPath("$.questionText").value(request.getQuestionText()));
    }

    @Test
    void getQuestionByIdReturnsQuestion() throws Exception {
        UUID questionId = UUID.randomUUID();
        QuestionDTO response = QuestionDTO.builder()
                .id(questionId)
                .quizId(UUID.randomUUID())
                .questionText("What is the capital of France?")
                .questionType("multiple_choice")
                .timeLimitSeconds(30)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionService.getQuestionById(questionId)).willReturn(response);

        mockMvc.perform(get("/api/v1/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questionId.toString()));
    }

    @Test
    void getAllQuestionsReturnsQuestions() throws Exception {
        QuestionDTO response = QuestionDTO.builder()
                .id(UUID.randomUUID())
                .quizId(UUID.randomUUID())
                .questionText("What is the capital of France?")
                .questionType("multiple_choice")
                .timeLimitSeconds(30)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionService.getAllQuestions()).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(response.getId().toString()));
    }

    @Test
    void getQuestionsByQuizIdReturnsQuestions() throws Exception {
        UUID quizId = UUID.randomUUID();
        QuestionDTO response = QuestionDTO.builder()
                .id(UUID.randomUUID())
                .quizId(quizId)
                .questionText("What is the capital of France?")
                .questionType("multiple_choice")
                .timeLimitSeconds(30)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionService.getQuestionsByQuizId(quizId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/questions/quiz/{quizId}", quizId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quizId").value(quizId.toString()));
    }

    @Test
    void updateQuestionReturnsUpdatedQuestion() throws Exception {
        UUID questionId = UUID.randomUUID();
        CreateQuestionRequest request = CreateQuestionRequest.builder()
                .quizId(UUID.randomUUID())
                .questionText("Updated question?")
                .questionType("multiple_choice")
                .timeLimitSeconds(45)
                .displayOrder(2)
                .build();
        QuestionDTO response = QuestionDTO.builder()
                .id(questionId)
                .quizId(request.getQuizId())
                .questionText(request.getQuestionText())
                .questionType(request.getQuestionType())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .displayOrder(request.getDisplayOrder())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(2)
                .build();

        given(questionService.updateQuestion(eq(questionId), any(CreateQuestionRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/questions/{id}", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(questionId.toString()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void deleteQuestionReturnsNoContent() throws Exception {
        UUID questionId = UUID.randomUUID();
        willDoNothing().given(questionService).deleteQuestion(questionId);

        mockMvc.perform(delete("/api/v1/questions/{id}", questionId))
                .andExpect(status().isNoContent());
    }
}
