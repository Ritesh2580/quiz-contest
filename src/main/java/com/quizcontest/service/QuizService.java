package com.quizcontest.service;

import com.quizcontest.dto.CreateQuizRequest;
import com.quizcontest.dto.QuizDTO;
import com.quizcontest.entity.Quiz;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizRepository;
import com.quizcontest.service.interfaces.IQuizService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Quiz entity
 * Handles business logic for quiz management
 * Implements IQuizService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService implements IQuizService {

    private final QuizRepository quizRepository;
    private final ModelMapper modelMapper;

    /**
     * Create a new quiz
     */
    @Override
    public QuizDTO createQuiz(CreateQuizRequest request, UUID createdBy) {
        Quiz quiz = modelMapper.map(request, Quiz.class);
        quiz.setId(UUID.randomUUID());
        quiz.setCreatorId(createdBy);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setVersion(1);
        quiz.setIsPublished(false);
        quiz.setTotalPoints(0);
        
        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToDTO(savedQuiz);
    }

    /**
     * Get quiz by ID
     */
    @Override
    @Transactional(readOnly = true)
    public QuizDTO getQuizById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        return convertToDTO(quiz);
    }

    /**
     * Get all quizzes
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get quizzes created by a specific user
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizDTO> getQuizzesByCreator(UUID creatorId) {
        return quizRepository.findAll().stream()
                .filter(quiz -> quiz.getCreatorId().equals(creatorId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update quiz
     */
    @Override
    public QuizDTO updateQuiz(UUID id, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        
        // Use ModelMapper to map request to entity
        modelMapper.map(request, quiz);
        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setVersion(quiz.getVersion() + 1);
        
        Quiz updatedQuiz = quizRepository.save(quiz);
        return convertToDTO(updatedQuiz);
    }

    /**
     * Delete quiz
     */
    @Override
    public void deleteQuiz(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        quizRepository.delete(quiz);
    }

    /**
     * Check if quiz is active (within start and end time)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isQuizActive(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(quiz.getStartDatetime()) && now.isBefore(quiz.getEndDatetime());
    }

    /**
     * Convert Quiz entity to QuizDTO using ModelMapper
     */
    private QuizDTO convertToDTO(Quiz quiz) {
        return modelMapper.map(quiz, QuizDTO.class);
    }
}
