package com.quizcontest.service;

import com.quizcontest.dto.CreateQuestionRequest;
import com.quizcontest.dto.QuestionDTO;
import com.quizcontest.entity.Question;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private QuestionService questionService;

    private Question question;
    private QuestionDTO questionDTO;
    private UUID questionId;
    private UUID quizId;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        question = Question.builder()
                .id(questionId)
                .quizId(quizId)
                .questionText("Test Question")
                .orderIndex(1)
                .version(1)
                .build();

        questionDTO = new QuestionDTO();
        questionDTO.setId(questionId);
        questionDTO.setQuestionText("Test Question");
    }

    @Test
    void createQuestion_Success() {
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setQuestionText("New Question");

        when(modelMapper.map(request, Question.class)).thenReturn(new Question());
        when(questionRepository.save(any(Question.class))).thenReturn(question);
        when(modelMapper.map(any(Question.class), eq(QuestionDTO.class))).thenReturn(questionDTO);

        QuestionDTO result = questionService.createQuestion(request);

        assertNotNull(result);
        assertEquals(questionId, result.getId());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void getQuestionById_Success() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        when(modelMapper.map(question, QuestionDTO.class)).thenReturn(questionDTO);

        QuestionDTO result = questionService.getQuestionById(questionId);

        assertNotNull(result);
        assertEquals(questionId, result.getId());
    }

    @Test
    void getQuestionById_NotFound_ThrowsException() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> questionService.getQuestionById(questionId));
    }

    @Test
    void getAllQuestions_Success() {
        when(questionRepository.findAll()).thenReturn(Collections.singletonList(question));
        when(modelMapper.map(question, QuestionDTO.class)).thenReturn(questionDTO);

        List<QuestionDTO> result = questionService.getAllQuestions();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getQuestionsByQuizId_Success() {
        when(questionRepository.findAll()).thenReturn(Collections.singletonList(question));
        when(modelMapper.map(question, QuestionDTO.class)).thenReturn(questionDTO);

        List<QuestionDTO> result = questionService.getQuestionsByQuizId(quizId);

        assertEquals(1, result.size());
    }

    @Test
    void updateQuestion_Success() {
        CreateQuestionRequest request = new CreateQuestionRequest();
        request.setQuestionText("Updated Text");

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));
        lenient().when(questionRepository.save(any(Question.class))).thenReturn(question);
        lenient().when(modelMapper.map(any(Question.class), eq(QuestionDTO.class))).thenReturn(questionDTO);

        QuestionDTO result = questionService.updateQuestion(questionId, request);

        assertNotNull(result);
        verify(questionRepository).save(question);
    }

    @Test
    void deleteQuestion_Success() {
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(question));

        questionService.deleteQuestion(questionId);

        verify(questionRepository).delete(question);
    }
}
