package com.quizcontest.service;

import com.quizcontest.dto.CreateQuizRequest;
import com.quizcontest.dto.QuizDTO;
import com.quizcontest.entity.Quiz;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizRepository;
import com.quizcontest.service.interfaces.IQuizService;
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

import static com.quizcontest.config.RedisCacheConfig.CACHE_QUIZZES;

/**
 * Service implementation for managing {@link Quiz} entities.
 * <p>
 * This service provides comprehensive quiz management functionality including:
 * <ul>
 *   <li>Quiz creation with automatic UUID generation</li>
 *   <li>Quiz retrieval by ID, all quizzes, or by creator</li>
 *   <li>Quiz updates with optimistic locking</li>
 *   <li>Quiz deletion with cache eviction</li>
 *   <li>Quiz activity status checking</li>
 * </ul>
 * </p>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching with the following key patterns:
 * <ul>
 *   <li>{@code quizzes:{id}} - Individual quiz by ID</li>
 *   <li>{@code quizzes:all} - All quizzes list</li>
 *   <li>{@code quizzes:creator:{creatorId}} - Quizzes by specific creator</li>
 * </ul>
 * Cache TTL is 30 minutes as configured in {@link com.quizcontest.config.RedisCacheConfig}.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IQuizService
 * @see QuizRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService implements IQuizService {

    /** Repository for accessing quiz data. */
    private final QuizRepository quizRepository;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Creates a new quiz in the system.
     * <p>
     * The quiz is initialized with default values:
     * <ul>
     *   <li>ID: Auto-generated UUID</li>
     *   <li>isPublished: false</li>
     *   <li>totalPoints: 0</li>
     *   <li>version: 1</li>
     * </ul>
     * After creation, all quiz cache entries are evicted.
     * </p>
     *
     * @param request the quiz creation request containing title, description, timing, etc.
     * @param createdBy the UUID of the user creating the quiz
     * @return the created quiz as a DTO
     */
    @Override
    @CacheEvict(value = CACHE_QUIZZES, allEntries = true)
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
     * Retrieves a quiz by its unique identifier.
     * <p>
     * Cached with key pattern: {@code quizzes:{id}}. TTL: 30 minutes.
     * </p>
     *
     * @param id the unique identifier of the quiz
     * @return the quiz as a DTO
     * @throws ResourceNotFoundException if no quiz is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUIZZES, key = "#id")
    public QuizDTO getQuizById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        return convertToDTO(quiz);
    }

    /**
     * Retrieves all quizzes in the system.
     * <p>
     * Cached with key: {@code quizzes:all}. TTL: 30 minutes.
     * </p>
     *
     * @return a list of all quizzes as DTOs
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUIZZES, key = "'all'")
    public List<QuizDTO> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all quizzes created by a specific user.
     * <p>
     * Cached with key pattern: {@code quizzes:creator:{creatorId}}. TTL: 30 minutes.
     * </p>
     *
     * @param creatorId the UUID of the quiz creator
     * @return a list of quizzes created by the specified user
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUIZZES, key = "'creator:' + #creatorId")
    public List<QuizDTO> getQuizzesByCreator(UUID creatorId) {
        return quizRepository.findAll().stream()
                .filter(quiz -> quiz.getCreatorId().equals(creatorId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing quiz's information.
     * <p>
     * Updates the individual quiz cache entry and evicts the 'all' quizzes cache.
     * Version is incremented for optimistic locking.
     * </p>
     *
     * @param id the unique identifier of the quiz to update
     * @param request the update request containing new quiz information
     * @return the updated quiz as a DTO
     * @throws ResourceNotFoundException if no quiz is found with the given ID
     */
    @Override
    @CachePut(value = CACHE_QUIZZES, key = "#id")
    @CacheEvict(value = CACHE_QUIZZES, key = "'all'")
    public QuizDTO updateQuiz(UUID id, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        
        modelMapper.map(request, quiz);
        quiz.setUpdatedAt(LocalDateTime.now());
        quiz.setVersion(quiz.getVersion() + 1);
        
        Quiz updatedQuiz = quizRepository.save(quiz);
        return convertToDTO(updatedQuiz);
    }

    /**
     * Deletes a quiz from the system.
     * <p>
     * All quiz cache entries are evicted to ensure data consistency.
     * </p>
     *
     * @param id the unique identifier of the quiz to delete
     * @throws ResourceNotFoundException if no quiz is found with the given ID
     */
    @Override
    @CacheEvict(value = CACHE_QUIZZES, allEntries = true)
    public void deleteQuiz(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
        quizRepository.delete(quiz);
    }

    /**
     * Checks if a quiz is currently active based on its start and end datetime.
     * <p>
     * A quiz is considered active if the current time is between the quiz's
     * start datetime and end datetime.
     * </p>
     *
     * @param id the unique identifier of the quiz
     * @return {@code true} if the quiz is currently active, {@code false} otherwise
     * @throws ResourceNotFoundException if no quiz is found with the given ID
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
     * Converts a {@link Quiz} entity to a {@link QuizDTO}.
     *
     * @param quiz the quiz entity to convert
     * @return the converted quiz DTO
     */
    private QuizDTO convertToDTO(Quiz quiz) {
        return modelMapper.map(quiz, QuizDTO.class);
    }
}
