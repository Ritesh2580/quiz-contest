package com.quizcontest.service;

import com.quizcontest.dto.PlayerAnswerDTO;
import com.quizcontest.dto.SubmitAnswerRequest;
import com.quizcontest.entity.CorrectAnswer;
import com.quizcontest.entity.PlayerAnswer;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.CorrectAnswerRepository;
import com.quizcontest.repository.PlayerAnswerRepository;
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
class PlayerAnswerServiceTest {

    @Mock
    private PlayerAnswerRepository playerAnswerRepository;

    @Mock
    private CorrectAnswerRepository correctAnswerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PlayerAnswerService playerAnswerService;

    private PlayerAnswer answer;
    private PlayerAnswerDTO answerDTO;
    private UUID answerId;
    private UUID participantId;
    private UUID questionId;

    @BeforeEach
    void setUp() {
        answerId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        answer = PlayerAnswer.builder()
                .id(answerId)
                .quizParticipantId(participantId)
                .questionId(questionId)
                .answerValue("Test Answer")
                .isCorrect(true)
                .pointsEarned(10)
                .build();

        answerDTO = new PlayerAnswerDTO();
        answerDTO.setId(answerId);
        answerDTO.setAnswerText("Test Answer");
    }

    @Test
    void submitAnswer_Correct_Success() {
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setParticipantId(participantId);
        request.setQuestionId(questionId);
        request.setAnswerText("Correct");

        CorrectAnswer correctAnswer = new CorrectAnswer();
        correctAnswer.setQuestionId(questionId);
        correctAnswer.setAnswerValue("Correct");

        when(correctAnswerRepository.findAll()).thenReturn(Collections.singletonList(correctAnswer));
        when(playerAnswerRepository.save(any(PlayerAnswer.class))).thenReturn(answer);
        when(modelMapper.map(any(PlayerAnswer.class), eq(PlayerAnswerDTO.class))).thenReturn(answerDTO);

        PlayerAnswerDTO result = playerAnswerService.submitAnswer(request);

        assertNotNull(result);
        verify(playerAnswerRepository).save(any(PlayerAnswer.class));
    }

    @Test
    void getAnswerById_Success() {
        when(playerAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(modelMapper.map(answer, PlayerAnswerDTO.class)).thenReturn(answerDTO);

        PlayerAnswerDTO result = playerAnswerService.getAnswerById(answerId);

        assertNotNull(result);
        assertEquals(answerId, result.getId());
    }

    @Test
    void getAnswersByParticipantId_Success() {
        when(playerAnswerRepository.findAll()).thenReturn(Collections.singletonList(answer));
        when(modelMapper.map(answer, PlayerAnswerDTO.class)).thenReturn(answerDTO);

        List<PlayerAnswerDTO> result = playerAnswerService.getAnswersByParticipantId(participantId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void calculateTotalScore_Success() {
        when(playerAnswerRepository.findAll()).thenReturn(Collections.singletonList(answer));

        Integer score = playerAnswerService.calculateTotalScore(participantId);

        assertEquals(10, score);
    }
}
