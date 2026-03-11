package com.quizcontest.service.interfaces;

import com.quizcontest.dto.CreateQuestionOptionRequest;
import com.quizcontest.dto.QuestionOptionDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface for Question Option Service
 */
public interface IQuestionOptionService {

    QuestionOptionDTO createOption(CreateQuestionOptionRequest request);

    QuestionOptionDTO getOptionById(UUID id);

    List<QuestionOptionDTO> getOptionsByQuestionId(UUID questionId);

    QuestionOptionDTO updateOption(UUID id, CreateQuestionOptionRequest request);

    void deleteOption(UUID id);
}
