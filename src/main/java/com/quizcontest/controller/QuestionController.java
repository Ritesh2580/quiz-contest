package com.quizcontest.controller;

import com.quizcontest.dto.CreateQuestionRequest;
import com.quizcontest.dto.QuestionDTO;
import com.quizcontest.service.interfaces.IQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Question management
 * Uses IQuestionService interface for Dependency Inversion Principle
 */
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Question management endpoints")
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping
    @Operation(summary = "Create a new question", description = "Create a new question for a quiz")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Question created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuestionDTO> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        QuestionDTO questionDTO = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(questionDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get question by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question found"),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable UUID id) {
        QuestionDTO questionDTO = questionService.getQuestionById(id);
        return ResponseEntity.ok(questionDTO);
    }

    @GetMapping
    @Operation(summary = "Get all questions")
    public ResponseEntity<List<QuestionDTO>> getAllQuestions() {
        List<QuestionDTO> questions = questionService.getAllQuestions();
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get questions by quiz ID", description = "Retrieve all questions for a specific quiz")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByQuizId(
            @Parameter(description = "Quiz ID")
            @PathVariable UUID quizId) {
        List<QuestionDTO> questions = questionService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(questions);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update question")
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuestionRequest request) {
        QuestionDTO questionDTO = questionService.updateQuestion(id, request);
        return ResponseEntity.ok(questionDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
