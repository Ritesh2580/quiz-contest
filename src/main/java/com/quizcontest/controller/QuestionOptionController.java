package com.quizcontest.controller;

import com.quizcontest.dto.CreateQuestionOptionRequest;
import com.quizcontest.dto.QuestionOptionDTO;
import com.quizcontest.service.interfaces.IQuestionOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Question Option management
 * Uses IQuestionOptionService interface for Dependency Inversion Principle
 */
@RestController
@RequestMapping("/api/v1/question-options")
@RequiredArgsConstructor
@Tag(name = "Question Options", description = "Question option management endpoints")
public class QuestionOptionController {

    private final IQuestionOptionService questionOptionService;

    @PostMapping
    @Operation(summary = "Create a new question option")
    public ResponseEntity<QuestionOptionDTO> createOption(@Valid @RequestBody CreateQuestionOptionRequest request) {
        QuestionOptionDTO optionDTO = questionOptionService.createOption(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(optionDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get option by ID")
    public ResponseEntity<QuestionOptionDTO> getOptionById(@PathVariable UUID id) {
        QuestionOptionDTO optionDTO = questionOptionService.getOptionById(id);
        return ResponseEntity.ok(optionDTO);
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "Get options by question ID")
    public ResponseEntity<List<QuestionOptionDTO>> getOptionsByQuestionId(
            @Parameter(description = "Question ID")
            @PathVariable UUID questionId) {
        List<QuestionOptionDTO> options = questionOptionService.getOptionsByQuestionId(questionId);
        return ResponseEntity.ok(options);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update question option")
    public ResponseEntity<QuestionOptionDTO> updateOption(
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuestionOptionRequest request) {
        QuestionOptionDTO optionDTO = questionOptionService.updateOption(id, request);
        return ResponseEntity.ok(optionDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete question option")
    public ResponseEntity<Void> deleteOption(@PathVariable UUID id) {
        questionOptionService.deleteOption(id);
        return ResponseEntity.noContent().build();
    }
}
