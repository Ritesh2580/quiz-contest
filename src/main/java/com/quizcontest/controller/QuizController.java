package com.quizcontest.controller;

import com.quizcontest.dto.CreateQuizRequest;
import com.quizcontest.dto.QuizDTO;
import com.quizcontest.service.interfaces.IQuizService;
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
 * REST Controller for Quiz management
 * Handles quiz creation, retrieval, and updates
 * Uses IQuizService interface for Dependency Inversion Principle
 */
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Quiz management endpoints")
public class QuizController {

    private final IQuizService quizService;

    /**
     * Create a new quiz
     */
    @PostMapping
    @Operation(summary = "Create a new quiz", description = "Create a new quiz with specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuizDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDTO> createQuiz(
            @Parameter(description = "Creator user ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @RequestParam UUID createdBy,
            @Valid @RequestBody CreateQuizRequest request) {
        QuizDTO quizDTO = quizService.createQuiz(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(quizDTO);
    }

    /**
     * Get quiz by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Retrieve quiz details by quiz ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuizDTO.class))),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDTO> getQuizById(
            @Parameter(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id) {
        QuizDTO quizDTO = quizService.getQuizById(id);
        return ResponseEntity.ok(quizDTO);
    }

    /**
     * Get all quizzes
     */
    @GetMapping
    @Operation(summary = "Get all quizzes", description = "Retrieve list of all quizzes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quizzes retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuizDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        List<QuizDTO> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Get quizzes by creator
     */
    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "Get quizzes by creator", description = "Retrieve all quizzes created by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quizzes retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuizDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<QuizDTO>> getQuizzesByCreator(
            @Parameter(description = "Creator user ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID creatorId) {
        List<QuizDTO> quizzes = quizService.getQuizzesByCreator(creatorId);
        return ResponseEntity.ok(quizzes);
    }

    /**
     * Update quiz
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update quiz", description = "Update quiz information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = QuizDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<QuizDTO> updateQuiz(
            @Parameter(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id,
            @Valid @RequestBody CreateQuizRequest request) {
        QuizDTO quizDTO = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(quizDTO);
    }

    /**
     * Delete quiz
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete quiz", description = "Delete a quiz from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quiz deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteQuiz(
            @Parameter(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if quiz is active
     */
    @GetMapping("/{id}/active")
    @Operation(summary = "Check if quiz is active", description = "Check if a quiz is currently active (within start and end time)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz status retrieved"),
            @ApiResponse(responseCode = "404", description = "Quiz not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Boolean> isQuizActive(
            @Parameter(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID id) {
        boolean isActive = quizService.isQuizActive(id);
        return ResponseEntity.ok(isActive);
    }
}
