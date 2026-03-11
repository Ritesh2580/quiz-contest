package com.quizcontest.repository;

import com.quizcontest.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByCreatorId(UUID creatorId);
    Page<Quiz> findByCreatorId(UUID creatorId, Pageable pageable);
    List<Quiz> findByIsPublishedTrue();
    List<Quiz> findByIsPublishedTrueAndStartDatetimeBeforeAndEndDatetimeAfter(LocalDateTime startDate, LocalDateTime endDate);
    Page<Quiz> findByIsPublishedTrue(Pageable pageable);
}