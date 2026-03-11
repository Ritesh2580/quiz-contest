package com.quizcontest.service;

import com.quizcontest.dto.CreateQuestionOptionRequest;
import com.quizcontest.dto.QuestionOptionDTO;
import com.quizcontest.entity.QuestionOption;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuestionOptionRepository;
import com.quizcontest.service.interfaces.IQuestionOptionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for QuestionOption entity
 * Implements IQuestionOptionService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuestionOptionService implements IQuestionOptionService {

    private final QuestionOptionRepository questionOptionRepository;
    private final ModelMapper modelMapper;

    /**
     * Create a new question option
     */
    @Override
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
     * Get option by ID
     */
    @Override
    @Transactional(readOnly = true)
    public QuestionOptionDTO getOptionById(UUID id) {
        QuestionOption option = questionOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question option not found with ID: " + id));
        return convertToDTO(option);
    }

    /**
     * Get options by question ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuestionOptionDTO> getOptionsByQuestionId(UUID questionId) {
        return questionOptionRepository.findAll().stream()
                .filter(option -> option.getQuestionId().equals(questionId))
                .sorted((o1, o2) -> o1.getOptionIndex().compareTo(o2.getOptionIndex()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update option
     */
    @Override
    public QuestionOptionDTO updateOption(UUID id, CreateQuestionOptionRequest request) {
        QuestionOption option = questionOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question option not found with ID: " + id));
        
        // Use ModelMapper to map request to entity
        modelMapper.map(request, option);
        option.setUpdatedAt(LocalDateTime.now());
        option.setVersion(option.getVersion() + 1);
        
        QuestionOption updatedOption = questionOptionRepository.save(option);
        return convertToDTO(updatedOption);
    }

    /**
     * Delete option
     */
    @Override
    public void deleteOption(UUID id) {
        QuestionOption option = questionOptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question option not found with ID: " + id));
        questionOptionRepository.delete(option);
    }

    /**
     * Convert QuestionOption entity to QuestionOptionDTO using ModelMapper
     */
    private QuestionOptionDTO convertToDTO(QuestionOption option) {
        return modelMapper.map(option, QuestionOptionDTO.class);
    }
}
