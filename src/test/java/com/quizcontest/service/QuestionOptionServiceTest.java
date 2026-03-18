package com.quizcontest.service;

import com.quizcontest.dto.CreateQuestionOptionRequest;
import com.quizcontest.dto.QuestionOptionDTO;
import com.quizcontest.entity.QuestionOption;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuestionOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionOptionServiceTest {

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private QuestionOptionService questionOptionService;

    private QuestionOption option;
    private QuestionOptionDTO optionDTO;
    private UUID optionId;
    private UUID questionId;

    @BeforeEach
    void setUp() {
        optionId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        option = QuestionOption.builder()
                .id(optionId)
                .questionId(questionId)
                .optionText("Test Option")
                .optionIndex((short) 1)
                .isCorrect(true)
                .version(1)
                .build();

        optionDTO = new QuestionOptionDTO();
        optionDTO.setId(optionId);
        optionDTO.setOptionText("Test Option");
    }

    @Test
    void createOption_Success() {
        CreateQuestionOptionRequest request = new CreateQuestionOptionRequest();
        request.setOptionText("New Option");

        when(modelMapper.map(request, QuestionOption.class)).thenReturn(new QuestionOption());
        when(questionOptionRepository.save(any(QuestionOption.class))).thenReturn(option);
        when(modelMapper.map(any(QuestionOption.class), eq(QuestionOptionDTO.class))).thenReturn(optionDTO);

        QuestionOptionDTO result = questionOptionService.createOption(request);

        assertNotNull(result);
        assertEquals(optionId, result.getId());
        verify(questionOptionRepository).save(any(QuestionOption.class));
    }

    @Test
    void getOptionById_Success() {
        when(questionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        when(modelMapper.map(option, QuestionOptionDTO.class)).thenReturn(optionDTO);

        QuestionOptionDTO result = questionOptionService.getOptionById(optionId);

        assertNotNull(result);
        assertEquals(optionId, result.getId());
    }

    @Test
    void getOptionById_NotFound_ThrowsException() {
        when(questionOptionRepository.findById(optionId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> questionOptionService.getOptionById(optionId));
    }

    @Test
    void getOptionsByQuestionId_Success() {
        when(questionOptionRepository.findAll()).thenReturn(Collections.singletonList(option));
        when(modelMapper.map(option, QuestionOptionDTO.class)).thenReturn(optionDTO);

        List<QuestionOptionDTO> result = questionOptionService.getOptionsByQuestionId(questionId);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void updateOption_Success() {
        CreateQuestionOptionRequest request = new CreateQuestionOptionRequest();
        request.setOptionText("Updated Option");

        when(questionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));
        lenient().when(questionOptionRepository.save(any(QuestionOption.class))).thenReturn(option);
        lenient().when(modelMapper.map(any(QuestionOption.class), eq(QuestionOptionDTO.class))).thenReturn(optionDTO);

        QuestionOptionDTO result = questionOptionService.updateOption(optionId, request);

        assertNotNull(result);
        verify(questionOptionRepository).save(option);
    }

    @Test
    void deleteOption_Success() {
        when(questionOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        questionOptionService.deleteOption(optionId);

        verify(questionOptionRepository).delete(option);
    }
}
