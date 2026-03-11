package com.quizcontest.service.interfaces;

import com.quizcontest.dto.CreateQuizRequest;
import com.quizcontest.dto.QuizDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Quiz Service
 */
public interface IQuizService {

    QuizDTO createQuiz(CreateQuizRequest request, UUID createdBy);

    QuizDTO getQuizById(UUID id);

    List<QuizDTO> getAllQuizzes();

    List<QuizDTO> getQuizzesByCreator(UUID creatorId);

    QuizDTO updateQuiz(UUID id, CreateQuizRequest request);

    void deleteQuiz(UUID id);

    boolean isQuizActive(UUID id);
}
