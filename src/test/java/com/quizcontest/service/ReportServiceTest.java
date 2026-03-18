package com.quizcontest.service;

import com.quizcontest.entity.*;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizParticipantRepository quizParticipantRepository;

    @Mock
    private PlayerAnswerRepository playerAnswerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @InjectMocks
    private ReportService reportService;

    private UUID userId;
    private UUID quizId;
    private User user;
    private Quiz quiz;
    private QuizParticipant participant;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .fullName("Test User")
                .username("testuser")
                .build();

        quiz = Quiz.builder()
                .id(quizId)
                .title("Test Quiz")
                .description("Test Description")
                .build();

        participant = QuizParticipant.builder()
                .id(UUID.randomUUID())
                .quizId(quizId)
                .playerId(userId)
                .totalScore(100)
                .status("completed")
                .joinedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void generateUserReport_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(quizParticipantRepository.findByPlayerId(userId)).thenReturn(Collections.singletonList(participant));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(questionRepository.countByQuizId(quizId)).thenReturn(10L);
        when(playerAnswerRepository.findByQuizParticipantId(any())).thenReturn(Collections.emptyList());

        ByteArrayInputStream result = reportService.generateUserReport(userId);

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    void generateUserReport_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.generateUserReport(userId));
    }

    @Test
    void generateQuizConsolidatedReport_Success() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));
        when(questionRepository.findByQuizIdOrderByOrderIndex(quizId)).thenReturn(Collections.emptyList());
        when(quizParticipantRepository.findByQuizId(quizId)).thenReturn(Collections.singletonList(participant));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerAnswerRepository.findByQuizParticipantId(any())).thenReturn(Collections.emptyList());

        ByteArrayInputStream result = reportService.generateQuizConsolidatedReport(quizId);

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    void generateQuizConsolidatedReport_QuizNotFound_ThrowsException() {
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.generateQuizConsolidatedReport(quizId));
    }
}
