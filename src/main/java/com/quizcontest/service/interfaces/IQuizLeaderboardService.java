package com.quizcontest.service.interfaces;

import com.quizcontest.dto.QuizLeaderboardDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Quiz Leaderboard Service
 */
public interface IQuizLeaderboardService {

    QuizLeaderboardDTO createOrUpdateLeaderboardEntry(UUID quizId, UUID userId, Integer score, Integer timeTakenSeconds);

    QuizLeaderboardDTO getLeaderboardEntryById(UUID id);

    List<QuizLeaderboardDTO> getLeaderboardByQuizId(UUID quizId);

    Integer getUserRank(UUID quizId, UUID userId);

    List<QuizLeaderboardDTO> getTopLeaderboardEntries(UUID quizId, Integer limit);
}
