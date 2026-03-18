package com.quizcontest.service;

import com.quizcontest.dto.CreateQuestionRequest;
import com.quizcontest.dto.QuestionDTO;
import com.quizcontest.entity.Question;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuestionRepository;
import com.quizcontest.service.interfaces.IQuestionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.quizcontest.config.RedisCacheConfig.CACHE_QUESTIONS;
import static com.quizcontest.config.RedisCacheConfig.CACHE_QUIZZES;

/**
 * Service implementation for managing {@link Question} entities.
 * <p>
 * This service provides comprehensive question management functionality including:
 * <ul>
 *   <li>Question creation with automatic UUID generation</li>
 *   <li>Question retrieval by ID, all questions, or by quiz</li>
 *   <li>Question updates with optimistic locking</li>
 *   <li>Question deletion with cascade cache eviction</li>
 * </ul>
 * </p>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching with the following key patterns:
 * <ul>
 *   <li>{@code questions:{id}} - Individual question by ID</li>
 *   <li>{@code questions:all} - All questions list</li>
 *   <li>{@code questions:quiz:{quizId}} - Questions by specific quiz</li>
 * </ul>
 * Cache TTL is 30 minutes. Questions are sorted by {@code orderIndex} within a quiz.
 * </p>
 *
 * <p><b>Cross-Entity Cache Eviction:</b></p>
 * <p>
 * When questions are modified, both question and quiz caches are evicted
 * to maintain data consistency across related entities.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IQuestionService
 * @see QuestionRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService implements IQuestionService {

    /** Repository for accessing question data. */
    private final QuestionRepository questionRepository;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Creates a new question in the system.
     * <p>
     * The question is initialized with default values:
     * <ul>
     *   <li>ID: Auto-generated UUID</li>
     *   <li>version: 1</li>
     *   <li>timestamps: current date/time</li>
     * </ul>
     * After creation, both question and quiz caches are evicted to ensure consistency.
     * </p>
     *
     * @param request the question creation request containing quiz ID, text, type, etc.
     * @return the created question as a DTO
     */
    @Override
    @CacheEvict(value = {CACHE_QUESTIONS, CACHE_QUIZZES}, allEntries = true)
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
     * Retrieves a question by its unique identifier.
     * <p>
     * Cached with key pattern: {@code questions:{id}}. TTL: 30 minutes.
     * </p>
     *
     * @param id the unique identifier of the question
     * @return the question as a DTO
     * @throws ResourceNotFoundException if no question is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUESTIONS, key = "#id")
    public QuestionDTO getQuestionById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        return convertToDTO(question);
    }

    /**
     * Retrieves all questions in the system.
     * <p>
     * Cached with key: {@code questions:all}. TTL: 30 minutes.
     * </p>
     *
     * @return a list of all questions as DTOs
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUESTIONS, key = "'all'")
    public List<QuestionDTO> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all questions belonging to a specific quiz.
     * <p>
     * Questions are sorted by their {@code orderIndex} field to maintain
     * the correct sequence. Cached with key pattern: {@code questions:quiz:{quizId}}.
     * TTL: 30 minutes.
     * </p>
     *
     * @param quizId the unique identifier of the quiz
     * @return a list of questions for the specified quiz, sorted by order index
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUESTIONS, key = "'quiz:' + #quizId")
    public List<QuestionDTO> getQuestionsByQuizId(UUID quizId) {
        return questionRepository.findAll().stream()
                .filter(question -> question.getQuizId().equals(quizId))
                .sorted((q1, q2) -> q1.getOrderIndex().compareTo(q2.getOrderIndex()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing question's information.
     * <p>
     * Updates the individual question cache entry and evicts both question and
     * quiz caches to ensure consistency. Version is incremented for optimistic locking.
     * </p>
     *
     * @param id the unique identifier of the question to update
     * @param request the update request containing new question information
     * @return the updated question as a DTO
     * @throws ResourceNotFoundException if no question is found with the given ID
     */
    @Override
    @CachePut(value = CACHE_QUESTIONS, key = "#id")
    @CacheEvict(value = {CACHE_QUESTIONS, CACHE_QUIZZES}, key = "'all'", allEntries = true)
    public QuestionDTO updateQuestion(UUID id, CreateQuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        
        modelMapper.map(request, question);
        question.setUpdatedAt(LocalDateTime.now());
        question.setVersion(question.getVersion() + 1);
        
        Question updatedQuestion = questionRepository.save(question);
        return convertToDTO(updatedQuestion);
    }

    /**
     * Deletes a question from the system.
     * <p>
     * Both question and quiz caches are evicted to ensure data consistency
     * across related entities.
     * </p>
     *
     * @param id the unique identifier of the question to delete
     * @throws ResourceNotFoundException if no question is found with the given ID
     */
    @Override
    @CacheEvict(value = {CACHE_QUESTIONS, CACHE_QUIZZES}, allEntries = true)
    public void deleteQuestion(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + id));
        questionRepository.delete(question);
    }

    /**
     * Converts a {@link Question} entity to a {@link QuestionDTO}.
     *
     * @param question the question entity to convert
     * @return the converted question DTO
     */
    private QuestionDTO convertToDTO(Question question) {
        return modelMapper.map(question, QuestionDTO.class);
    }
}
