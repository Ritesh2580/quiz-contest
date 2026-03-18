package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.PlayerAnswerDTO;
import com.quizcontest.dto.SubmitAnswerRequest;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IPlayerAnswerService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PlayerAnswerController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = PlayerAnswerController.class)
class PlayerAnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IPlayerAnswerService playerAnswerService;

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
    void submitAnswerReturnsCreatedAnswer() throws Exception {
        UUID questionId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID answerId = UUID.randomUUID();
        SubmitAnswerRequest request = SubmitAnswerRequest.builder()
                .questionId(questionId)
                .participantId(participantId)
                .answerText("Paris")
                .timeTakenSeconds(15)
                .build();
        PlayerAnswerDTO response = PlayerAnswerDTO.builder()
                .id(answerId)
                .participantId(participantId)
                .questionId(questionId)
                .answerText(request.getAnswerText())
                .isCorrect(true)
                .score(10)
                .timeTakenSeconds(request.getTimeTakenSeconds())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(playerAnswerService.submitAnswer(any(SubmitAnswerRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/answers/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(answerId.toString()))
                .andExpect(jsonPath("$.questionId").value(questionId.toString()))
                .andExpect(jsonPath("$.score").value(10));
    }

    @Test
    void getAnswerByIdReturnsAnswer() throws Exception {
        UUID answerId = UUID.randomUUID();
        PlayerAnswerDTO response = PlayerAnswerDTO.builder()
                .id(answerId)
                .participantId(UUID.randomUUID())
                .questionId(UUID.randomUUID())
                .answerText("Paris")
                .isCorrect(true)
                .score(10)
                .timeTakenSeconds(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(playerAnswerService.getAnswerById(answerId)).willReturn(response);

        mockMvc.perform(get("/api/v1/answers/{id}", answerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answerId.toString()));
    }

    @Test
    void getAnswersByParticipantIdReturnsAnswers() throws Exception {
        UUID participantId = UUID.randomUUID();
        PlayerAnswerDTO response = PlayerAnswerDTO.builder()
                .id(UUID.randomUUID())
                .participantId(participantId)
                .questionId(UUID.randomUUID())
                .answerText("Paris")
                .isCorrect(true)
                .score(10)
                .timeTakenSeconds(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(playerAnswerService.getAnswersByParticipantId(participantId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/answers/participant/{participantId}", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].participantId").value(participantId.toString()));
    }

    @Test
    void getAnswersByQuestionIdReturnsAnswers() throws Exception {
        UUID questionId = UUID.randomUUID();
        PlayerAnswerDTO response = PlayerAnswerDTO.builder()
                .id(UUID.randomUUID())
                .participantId(UUID.randomUUID())
                .questionId(questionId)
                .answerText("Paris")
                .isCorrect(true)
                .score(10)
                .timeTakenSeconds(15)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(playerAnswerService.getAnswersByQuestionId(questionId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/answers/question/{questionId}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(questionId.toString()));
    }

    @Test
    void calculateTotalScoreReturnsScore() throws Exception {
        UUID participantId = UUID.randomUUID();
        given(playerAnswerService.calculateTotalScore(participantId)).willReturn(90);

        mockMvc.perform(get("/api/v1/answers/participant/{participantId}/score", participantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(90));
    }
}
