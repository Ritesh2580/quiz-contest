package com.quizcontest.service;

import com.quizcontest.dto.QuizLeaderboardDTO;
import com.quizcontest.entity.QuizLeaderboard;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizLeaderboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizLeaderboardServiceTest {

    @Mock
    private QuizLeaderboardRepository quizLeaderboardRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private QuizLeaderboardService quizLeaderboardService;

    private QuizLeaderboard leaderboard;
    private QuizLeaderboardDTO leaderboardDTO;
    private UUID leaderboardId;
    private UUID quizId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        leaderboardId = UUID.randomUUID();
        quizId = UUID.randomUUID();
        userId = UUID.randomUUID();
        leaderboard = QuizLeaderboard.builder()
                .id(leaderboardId)
                .quizId(quizId)
                .playerId(userId)
                .totalScore(100)
                .completionTimeMinutes(10)
                .rank(1)
                .version(1)
                .build();

        leaderboardDTO = new QuizLeaderboardDTO();
        leaderboardDTO.setId(leaderboardId);
        leaderboardDTO.setUserId(userId);
        leaderboardDTO.setRank(1);
    }

    @Test
    void createOrUpdateLeaderboardEntry_New_Success() {
        when(quizLeaderboardRepository.findAll()).thenReturn(Collections.emptyList());
        when(quizLeaderboardRepository.save(any(QuizLeaderboard.class))).thenReturn(leaderboard);
        when(modelMapper.map(any(QuizLeaderboard.class), eq(QuizLeaderboardDTO.class))).thenReturn(leaderboardDTO);

        QuizLeaderboardDTO result = quizLeaderboardService.createOrUpdateLeaderboardEntry(quizId, userId, 100, 10);

        assertNotNull(result);
        verify(quizLeaderboardRepository).save(any(QuizLeaderboard.class));
    }

    @Test
    void createOrUpdateLeaderboardEntry_Update_Success() {
        when(quizLeaderboardRepository.findAll()).thenReturn(Collections.singletonList(leaderboard));
        when(quizLeaderboardRepository.save(any(QuizLeaderboard.class))).thenReturn(leaderboard);
        when(modelMapper.map(any(QuizLeaderboard.class), eq(QuizLeaderboardDTO.class))).thenReturn(leaderboardDTO);

        QuizLeaderboardDTO result = quizLeaderboardService.createOrUpdateLeaderboardEntry(quizId, userId, 110, 12);

        assertNotNull(result);
        verify(quizLeaderboardRepository).save(leaderboard);
    }

    @Test
    void getLeaderboardByQuizId_Success() {
        QuizLeaderboard entry2 = QuizLeaderboard.builder()
                .quizId(quizId)
                .totalScore(90)
                .completionTimeMinutes(15)
                .build();

        when(quizLeaderboardRepository.findAll()).thenReturn(Arrays.asList(leaderboard, entry2));
        when(modelMapper.map(any(QuizLeaderboard.class), eq(QuizLeaderboardDTO.class))).thenReturn(leaderboardDTO);

        List<QuizLeaderboardDTO> result = quizLeaderboardService.getLeaderboardByQuizId(quizId);

        assertEquals(2, result.size());
        assertEquals(1, leaderboard.getRank());
        assertEquals(2, entry2.getRank());
    }

    @Test
    void getLeaderboardEntryById_Success() {
        when(quizLeaderboardRepository.findById(leaderboardId)).thenReturn(Optional.of(leaderboard));
        when(modelMapper.map(leaderboard, QuizLeaderboardDTO.class)).thenReturn(leaderboardDTO);

        QuizLeaderboardDTO result = quizLeaderboardService.getLeaderboardEntryById(leaderboardId);

        assertNotNull(result);
        assertEquals(leaderboardId, result.getId());
    }

    @Test
    void getUserRank_Success() {
        when(quizLeaderboardRepository.findAll()).thenReturn(Collections.singletonList(leaderboard));
        when(modelMapper.map(any(QuizLeaderboard.class), eq(QuizLeaderboardDTO.class))).thenReturn(leaderboardDTO);

        Integer rank = quizLeaderboardService.getUserRank(quizId, userId);

        assertEquals(1, rank);
    }

    @Test
    void getTopLeaderboardEntries_Success() {
        when(quizLeaderboardRepository.findAll()).thenReturn(Collections.singletonList(leaderboard));
        when(modelMapper.map(any(QuizLeaderboard.class), eq(QuizLeaderboardDTO.class))).thenReturn(leaderboardDTO);

        List<QuizLeaderboardDTO> result = quizLeaderboardService.getTopLeaderboardEntries(quizId, 1);

        assertEquals(1, result.size());
    }
}
