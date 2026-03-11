package com.quizcontest.service.interfaces;

import com.quizcontest.dto.JoinQuizRequest;
import com.quizcontest.dto.QuizParticipantDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Quiz Participant Service
 */
public interface IQuizParticipantService {

    QuizParticipantDTO joinQuiz(JoinQuizRequest request);

    QuizParticipantDTO getParticipantById(UUID id);

    List<QuizParticipantDTO> getParticipantsByQuizId(UUID quizId);

    List<QuizParticipantDTO> getParticipationsByUserId(UUID userId);

    QuizParticipantDTO startQuiz(UUID participantId);

    QuizParticipantDTO completeQuiz(UUID participantId, Integer finalScore);
}
