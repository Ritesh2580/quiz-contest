package com.quizcontest.repository;

import com.quizcontest.entity.PlayerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, UUID> {
    List<PlayerAnswer> findByQuizParticipantId(UUID quizParticipantId);
    Optional<PlayerAnswer> findByQuizParticipantIdAndQuestionId(UUID quizParticipantId, UUID questionId);
    long countByQuizParticipantIdAndIsCorrectTrue(UUID quizParticipantId);
}