package com.quizcontest.repository;

import com.quizcontest.entity.CorrectAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CorrectAnswerRepository extends JpaRepository<CorrectAnswer, UUID> {
    List<CorrectAnswer> findByQuestionId(UUID questionId);
}