package com.quizcontest.service;

import com.quizcontest.dto.CreateQuestionOptionRequest;
import com.quizcontest.dto.QuestionOptionDTO;
import com.quizcontest.entity.QuestionOption;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuestionOptionRepository;
import com.quizcontest.service.interfaces.IQuestionOptionService;
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

import static com.quizcontest.config.RedisCacheConfig.CACHE_QUESTION_OPTIONS;

/**
 * Service implementation for managing {@link QuestionOption} entities.
 * <p>
 * This service provides comprehensive question option management functionality including:
 * <ul>
 *   <li>Option creation for multiple-choice questions</li>
 *   <li>Option retrieval by ID or by question</li>
 *   <li>Option updates with optimistic locking</li>
 *   <li>Option deletion with cache eviction</li>
 * </ul>
 * </p>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching with the following key patterns:
 * <ul>
 *   <li>{@code questionOptions:{id}} - Individual option by ID</li>
 *   <li>{@code questionOptions:question:{questionId}} - Options by specific question</li>
 * </ul>
 * Cache TTL is 30 minutes. Options are sorted by {@code optionIndex} within a question.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IQuestionOptionService
 * @see QuestionOptionRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionOptionService implements IQuestionOptionService {

    /** Repository for accessing question option data. */
    private final QuestionOptionRepository questionOptionRepository;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Creates a new question option in the system.
     * <p>
     * The option is initialized with default values:
     * <ul>
     *   <li>ID: Auto-generated UUID</li>
     *   <li>version: 1</li>
     *   <li>timestamps: current date/time</li>
     * </ul>
     * After creation, all question option cache entries are evicted.
     * </p>
     *
     * @param request the option creation request containing question ID, text, index, etc.
     * @return the created option as a DTO
     */
    @Override
    @CacheEvict(value = CACHE_QUESTION_OPTIONS, allEntries = true)
    public QuestionOptionDTO createOption(CreateQuestionOptionRequest request) {
        QuestionOption option = modelMapper.map(request, QuestionOption.class);
        option.setId(UUID.randomUUID());
        option.setCreatedAt(LocalDateTime.now());
        option.setUpdatedAt(LocalDateTime.now());
        option.setVersion(1);
        
        QuestionOption savedOption = questionOptionRepository.save(option);
        return convertToDTO(savedOption);
    }

    /**
     * Retrieves a question option by its unique identifier.
     * <p>
     * Cached with key pattern: {@code questionOptions:{id}}. TTL: 30 minutes.
     * </p>
     *
     * @param id the unique identifier of the option
     * @return the option as a DTO
     * @throws ResourceNotFoundException if no option is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUESTION_OPTIONS, key = "#id")
    public QuestionOptionDTO getOptionById(UUID id) {
        QuestionOption option = questionOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question option not found with ID: " + id));
        return convertToDTO(option);
    }

    /**
     * Retrieves all options for a specific question.
     * <p>
     * Options are sorted by their {@code optionIndex} field to maintain
     * the correct display order. Cached with key pattern: {@code questionOptions:question:{questionId}}.
     * TTL: 30 minutes.
     * </p>
     *
     * @param questionId the unique identifier of the question
     * @return a list of options for the specified question, sorted by option index
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUESTION_OPTIONS, key = "'question:' + #questionId")
    public List<QuestionOptionDTO> getOptionsByQuestionId(UUID questionId) {
        return questionOptionRepository.findAll().stream()
                .filter(option -> option.getQuestionId().equals(questionId))
                .sorted((o1, o2) -> o1.getOptionIndex().compareTo(o2.getOptionIndex()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing question option's information.
     * <p>
     * Updates the individual option cache entry and evicts the 'all' options cache.
     * Version is incremented for optimistic locking.
     * </p>
     *
     * @param id the unique identifier of the option to update
     * @param request the update request containing new option information
     * @return the updated option as a DTO
     * @throws ResourceNotFoundException if no option is found with the given ID
     */
    @Override
    @CachePut(value = CACHE_QUESTION_OPTIONS, key = "#id")
    @CacheEvict(value = CACHE_QUESTION_OPTIONS, key = "'all'")
    public QuestionOptionDTO updateOption(UUID id, CreateQuestionOptionRequest request) {
        QuestionOption option = questionOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question option not found with ID: " + id));
        
        modelMapper.map(request, option);
        option.setUpdatedAt(LocalDateTime.now());
        option.setVersion(option.getVersion() + 1);
        
        QuestionOption updatedOption = questionOptionRepository.save(option);
        return convertToDTO(updatedOption);
    }

    /**
     * Deletes a question option from the system.
     * <p>
     * All question option cache entries are evicted to ensure data consistency.
     * </p>
     *
     * @param id the unique identifier of the option to delete
     * @throws ResourceNotFoundException if no option is found with the given ID
     */
    @Override
    @CacheEvict(value = CACHE_QUESTION_OPTIONS, allEntries = true)
    public void deleteOption(UUID id) {
        QuestionOption option = questionOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question option not found with ID: " + id));
        questionOptionRepository.delete(option);
    }

    /**
     * Converts a {@link QuestionOption} entity to a {@link QuestionOptionDTO}.
     *
     * @param option the option entity to convert
     * @return the converted option DTO
     */
    private QuestionOptionDTO convertToDTO(QuestionOption option) {
        return modelMapper.map(option, QuestionOptionDTO.class);
    }
}
