package com.quizcontest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizcontest.dto.CreateQuestionOptionRequest;
import com.quizcontest.dto.QuestionOptionDTO;
import com.quizcontest.repository.UserRepository;
import com.quizcontest.security.JwtAuthenticationFilter;
import com.quizcontest.service.UserService;
import com.quizcontest.service.interfaces.IQuestionOptionService;
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

@WebMvcTest(controllers = QuestionOptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.context.ContextConfiguration(classes = QuestionOptionController.class)
class QuestionOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IQuestionOptionService questionOptionService;

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
    void createOptionReturnsCreatedOption() throws Exception {
        UUID questionId = UUID.randomUUID();
        UUID optionId = UUID.randomUUID();
        CreateQuestionOptionRequest request = CreateQuestionOptionRequest.builder()
                .questionId(questionId)
                .optionText("Paris")
                .isCorrect(true)
                .displayOrder(1)
                .build();
        QuestionOptionDTO response = QuestionOptionDTO.builder()
                .id(optionId)
                .questionId(questionId)
                .optionText(request.getOptionText())
                .isCorrect(request.getIsCorrect())
                .displayOrder(request.getDisplayOrder())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionOptionService.createOption(any(CreateQuestionOptionRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/question-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(optionId.toString()))
                .andExpect(jsonPath("$.questionId").value(questionId.toString()))
                .andExpect(jsonPath("$.optionText").value(request.getOptionText()));
    }

    @Test
    void getOptionByIdReturnsOption() throws Exception {
        UUID optionId = UUID.randomUUID();
        QuestionOptionDTO response = QuestionOptionDTO.builder()
                .id(optionId)
                .questionId(UUID.randomUUID())
                .optionText("Paris")
                .isCorrect(true)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionOptionService.getOptionById(optionId)).willReturn(response);

        mockMvc.perform(get("/api/v1/question-options/{id}", optionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(optionId.toString()));
    }

    @Test
    void getOptionsByQuestionIdReturnsOptions() throws Exception {
        UUID questionId = UUID.randomUUID();
        QuestionOptionDTO response = QuestionOptionDTO.builder()
                .id(UUID.randomUUID())
                .questionId(questionId)
                .optionText("Paris")
                .isCorrect(true)
                .displayOrder(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(1)
                .build();

        given(questionOptionService.getOptionsByQuestionId(questionId)).willReturn(List.of(response));

        mockMvc.perform(get("/api/v1/question-options/question/{questionId}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(questionId.toString()));
    }

    @Test
    void updateOptionReturnsUpdatedOption() throws Exception {
        UUID optionId = UUID.randomUUID();
        CreateQuestionOptionRequest request = CreateQuestionOptionRequest.builder()
                .questionId(UUID.randomUUID())
                .optionText("Berlin")
                .isCorrect(false)
                .displayOrder(2)
                .build();
        QuestionOptionDTO response = QuestionOptionDTO.builder()
                .id(optionId)
                .questionId(request.getQuestionId())
                .optionText(request.getOptionText())
                .isCorrect(request.getIsCorrect())
                .displayOrder(request.getDisplayOrder())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(2)
                .build();

        given(questionOptionService.updateOption(eq(optionId), any(CreateQuestionOptionRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/question-options/{id}", optionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(optionId.toString()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    void deleteOptionReturnsNoContent() throws Exception {
        UUID optionId = UUID.randomUUID();
        willDoNothing().given(questionOptionService).deleteOption(optionId);

        mockMvc.perform(delete("/api/v1/question-options/{id}", optionId))
                .andExpect(status().isNoContent());
    }
}
