package com.quizcontest.service;

import com.quizcontest.dto.JoinQuizRequest;
import com.quizcontest.dto.QuizParticipantDTO;
import com.quizcontest.entity.QuizParticipant;
import com.quizcontest.exception.InvalidOperationException;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizParticipantRepository;
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
class QuizParticipantServiceTest {

    @Mock
    private QuizParticipantRepository quizParticipantRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private QuizParticipantService quizParticipantService;

    private QuizParticipant participant;
    private QuizParticipantDTO participantDTO;
    private UUID participantId;
    private UUID quizId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        participantId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        userId = UUID.randomUUID();
        participant = QuizParticipant.builder()
                .id(participantId)
                .quizId(quizId)
                .playerId(userId)
                .status("joined")
                .totalScore(0)
                .version(1)
                .build();

        participantDTO = new QuizParticipantDTO();
        participantDTO.setId(participantId);
        participantDTO.setStatus("joined");
    }

    @Test
    void joinQuiz_Success() {
        JoinQuizRequest request = new JoinQuizRequest();
        request.setQuizId(quizId);
        request.setUserId(userId);

        when(quizParticipantRepository.findAll()).thenReturn(Collections.emptyList());
        when(quizParticipantRepository.save(any(QuizParticipant.class))).thenReturn(participant);
        when(modelMapper.map(any(QuizParticipant.class), eq(QuizParticipantDTO.class))).thenReturn(participantDTO);

        QuizParticipantDTO result = quizParticipantService.joinQuiz(request);

        assertNotNull(result);
        assertEquals(participantId, result.getId());
        verify(quizParticipantRepository).save(any(QuizParticipant.class));
    }

    @Test
    void joinQuiz_AlreadyJoined_ThrowsException() {
        JoinQuizRequest request = new JoinQuizRequest();
        request.setQuizId(quizId);
        request.setUserId(userId);

        when(quizParticipantRepository.findAll()).thenReturn(Collections.singletonList(participant));

        assertThrows(InvalidOperationException.class, () -> quizParticipantService.joinQuiz(request));
    }

    @Test
    void getParticipantById_Success() {
        when(quizParticipantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(modelMapper.map(participant, QuizParticipantDTO.class)).thenReturn(participantDTO);

        QuizParticipantDTO result = quizParticipantService.getParticipantById(participantId);

        assertNotNull(result);
        assertEquals(participantId, result.getId());
    }

    @Test
    void getParticipantsByQuizId_Success() {
        when(quizParticipantRepository.findAll()).thenReturn(Collections.singletonList(participant));
        when(modelMapper.map(participant, QuizParticipantDTO.class)).thenReturn(participantDTO);

        List<QuizParticipantDTO> result = quizParticipantService.getParticipantsByQuizId(quizId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void startQuiz_Success() {
        participant.setStatus("pending");
        when(quizParticipantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(quizParticipantRepository.save(any(QuizParticipant.class))).thenReturn(participant);
        when(modelMapper.map(any(QuizParticipant.class), eq(QuizParticipantDTO.class))).thenReturn(participantDTO);

        QuizParticipantDTO result = quizParticipantService.startQuiz(participantId);

        assertNotNull(result);
        verify(quizParticipantRepository).save(participant);
    }

    @Test
    void completeQuiz_Success() {
        participant.setStatus("in_progress");
        when(quizParticipantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(quizParticipantRepository.save(any(QuizParticipant.class))).thenReturn(participant);
        when(modelMapper.map(any(QuizParticipant.class), eq(QuizParticipantDTO.class))).thenReturn(participantDTO);

        QuizParticipantDTO result = quizParticipantService.completeQuiz(participantId, 100);

        assertNotNull(result);
        assertEquals(100, participant.getTotalScore());
        verify(quizParticipantRepository).save(participant);
    }
}
