package com.quizcontest.repository;

import com.quizcontest.entity.QuizLeaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizLeaderboardRepository extends JpaRepository<QuizLeaderboard, UUID> {
    List<QuizLeaderboard> findByQuizIdOrderByRankAsc(UUID quizId);
    Page<QuizLeaderboard> findByQuizIdOrderByRankAsc(UUID quizId, Pageable pageable);
    Optional<QuizLeaderboard> findByQuizIdAndPlayerId(UUID quizId, UUID playerId);
}