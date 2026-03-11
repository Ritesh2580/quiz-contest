package com.quizcontest.service;

import com.quizcontest.dto.CreateQuestionRequest;
import com.quizcontest.dto.QuestionDTO;
import com.quizcontest.entity.Question;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuestionRepository;
import com.quizcontest.service.interfaces.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Question entity
 * Handles business logic for question management
 * Implements IQuestionService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService implements IQuestionService {

    private final QuestionRepository questionRepository;
    private final ModelMapper modelMapper;

    /**
     * Create a new question
     */
    @Override
    public QuestionDTO createQuestion(CreateQuestionRequest request) {
        Question question = modelMapper.map(request, Question.class);
        question.setId(UUID.randomUUID());
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setVersion(1);
        
        Question savedQuestion = questionRepository.save(question);
        return convertToDTO(savedQuestion);
    }

    /**
     * Get question by ID
     */
    @Override
    @Transactional(readOnly = true)
    public QuestionDTO getQuestionById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        return convertToDTO(question);
    }

    /**
     * Get all questions
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get questions by quiz ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuestionDTO> getQuestionsByQuizId(UUID quizId) {
        return questionRepository.findAll().stream()
                .filter(question -> question.getQuizId().equals(quizId))
                .sorted((q1, q2) -> q1.getOrderIndex().compareTo(q2.getOrderIndex()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update question
     */
    @Override
    public QuestionDTO updateQuestion(UUID id, CreateQuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        
        // Use ModelMapper to map request to entity
        modelMapper.map(request, question);
        question.setUpdatedAt(LocalDateTime.now());
        question.setVersion(question.getVersion() + 1);
        
        Question updatedQuestion = questionRepository.save(question);
        return convertToDTO(updatedQuestion);
    }

    /**
     * Delete question
     */
    @Override
    public void deleteQuestion(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        questionRepository.delete(question);
    }

    /**
     * Convert Question entity to QuestionDTO using ModelMapper
     */
    private QuestionDTO convertToDTO(Question question) {
        return modelMapper.map(question, QuestionDTO.class);
    }
}
