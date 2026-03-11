package com.quizcontest.service.interfaces;

import com.quizcontest.dto.PlayerAnswerDTO;
import com.quizcontest.dto.SubmitAnswerRequest;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Player Answer Service
 */
public interface IPlayerAnswerService {

    PlayerAnswerDTO submitAnswer(SubmitAnswerRequest request);

    PlayerAnswerDTO getAnswerById(UUID id);

    List<PlayerAnswerDTO> getAnswersByParticipantId(UUID participantId);

    List<PlayerAnswerDTO> getAnswersByQuestionId(UUID questionId);

    Integer calculateTotalScore(UUID participantId);
}
