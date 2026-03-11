package com.quizcontest.repository;

import com.quizcontest.entity.QuizParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizParticipantRepository extends JpaRepository<QuizParticipant, UUID> {
    List<QuizParticipant> findByQuizId(UUID quizId);
    List<QuizParticipant> findByPlayerId(UUID playerId);
    Optional<QuizParticipant> findByQuizIdAndPlayerId(UUID quizId, UUID playerId);
    long countByQuizId(UUID quizId);
}