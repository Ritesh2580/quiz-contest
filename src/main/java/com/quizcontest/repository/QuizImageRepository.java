package com.quizcontest.repository;

import com.quizcontest.entity.QuizImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizImageRepository extends JpaRepository<QuizImage, UUID> {
    List<QuizImage> findByQuizId(UUID quizId);
    List<QuizImage> findByImageType(String imageType);
    List<QuizImage> findByLinkedQuestionIdOrderByImageIndex(UUID linkedQuestionId);
    Optional<QuizImage> findByLinkedOptionId(UUID linkedOptionId);
}