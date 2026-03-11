package com.quizcontest.service.interfaces;

import com.quizcontest.dto.CreateQuestionRequest;
import com.quizcontest.dto.QuestionDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Question Service
 */
public interface IQuestionService {

    QuestionDTO createQuestion(CreateQuestionRequest request);

    QuestionDTO getQuestionById(UUID id);

    List<QuestionDTO> getAllQuestions();

    List<QuestionDTO> getQuestionsByQuizId(UUID quizId);

    QuestionDTO updateQuestion(UUID id, CreateQuestionRequest request);

    void deleteQuestion(UUID id);
}
