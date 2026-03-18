package com.quizcontest.service;

import com.quizcontest.dto.CreateQuizRequest;
import com.quizcontest.dto.QuizDTO;
import com.quizcontest.entity.Quiz;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private QuizService quizService;

    private Quiz quiz;
    private QuizDTO quizDTO;
    private UUID quizId;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        quizId = UUID.randomUUID();
        creatorId = UUID.randomUUID();
        quiz = Quiz.builder()
                .id(quizId)
                .creatorId(creatorId)
                .title("Test Quiz")
                .startDatetime(LocalDateTime.now().minusHours(1))
                .endDatetime(LocalDateTime.now().plusHours(1))
                .version(1)
                .build();

        quizDTO = new QuizDTO();
        quizDTO.setId(quizId);
        quizDTO.setTitle("Test Quiz");
    }

    @Test
    void createQuiz_Success() {
        CreateQuizRequest request = new CreateQuizRequest();
        request.setTitle("New Quiz");

        when(modelMapper.map(request, Quiz.class)).thenReturn(new Quiz());
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);
        when(modelMapper.map(any(Quiz.class), eq(QuizDTO.class))).thenReturn(quizDTO);

        QuizDTO result = quizService.createQuiz(request, creatorId);

        assertNotNull(result);
        assertEquals(quizId, result.getId());
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void getQuizById_Success() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(modelMapper.map(quiz, QuizDTO.class)).thenReturn(quizDTO);

        QuizDTO result = quizService.getQuizById(quizId);

        assertNotNull(result);
        assertEquals(quizId, result.getId());
    }

    @Test
    void getQuizById_NotFound_ThrowsException() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quizService.getQuizById(quizId));
    }

    @Test
    void getAllQuizzes_Success() {
        when(quizRepository.findAll()).thenReturn(Collections.singletonList(quiz));
        when(modelMapper.map(quiz, QuizDTO.class)).thenReturn(quizDTO);

        List<QuizDTO> result = quizService.getAllQuizzes();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getQuizzesByCreator_Success() {
        when(quizRepository.findAll()).thenReturn(Collections.singletonList(quiz));
        when(modelMapper.map(quiz, QuizDTO.class)).thenReturn(quizDTO);

        List<QuizDTO> result = quizService.getQuizzesByCreator(creatorId);

        assertEquals(1, result.size());
    }

    @Test
    void updateQuiz_Success() {
        CreateQuizRequest request = new CreateQuizRequest();
        request.setTitle("Updated Title");

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        lenient().when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);
        lenient().when(modelMapper.map(any(Quiz.class), eq(QuizDTO.class))).thenReturn(quizDTO);

        QuizDTO result = quizService.updateQuiz(quizId, request);

        assertNotNull(result);
        verify(quizRepository).save(quiz);
    }

    @Test
    void deleteQuiz_Success() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        quizService.deleteQuiz(quizId);

        verify(quizRepository).delete(quiz);
    }

    @Test
    void isQuizActive_ReturnsTrue() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        assertTrue(quizService.isQuizActive(quizId));
    }

    @Test
    void isQuizActive_ReturnsFalse() {
        quiz.setStartDatetime(LocalDateTime.now().plusHours(1));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        assertFalse(quizService.isQuizActive(quizId));
    }
}
