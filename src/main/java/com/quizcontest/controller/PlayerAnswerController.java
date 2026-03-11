package com.quizcontest.controller;

import com.quizcontest.dto.PlayerAnswerDTO;
import com.quizcontest.dto.SubmitAnswerRequest;
import com.quizcontest.service.interfaces.IPlayerAnswerService;
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
 * REST Controller for Player Answer management
 * Uses IPlayerAnswerService interface for Dependency Inversion Principle
 */
@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
@Tag(name = "Player Answers", description = "Player answer submission and retrieval endpoints")
public class PlayerAnswerController {

    private final IPlayerAnswerService playerAnswerService;

    @PostMapping("/submit")
    @Operation(summary = "Submit an answer", description = "Submit an answer for a question")
    public ResponseEntity<PlayerAnswerDTO> submitAnswer(@Valid @RequestBody SubmitAnswerRequest request) {
        PlayerAnswerDTO answerDTO = playerAnswerService.submitAnswer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(answerDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get answer by ID")
    public ResponseEntity<PlayerAnswerDTO> getAnswerById(@PathVariable UUID id) {
        PlayerAnswerDTO answerDTO = playerAnswerService.getAnswerById(id);
        return ResponseEntity.ok(answerDTO);
    }

    @GetMapping("/participant/{participantId}")
    @Operation(summary = "Get answers by participant ID", description = "Retrieve all answers submitted by a participant")
    public ResponseEntity<List<PlayerAnswerDTO>> getAnswersByParticipantId(
            @Parameter(description = "Participant ID")
            @PathVariable UUID participantId) {
        List<PlayerAnswerDTO> answers = playerAnswerService.getAnswersByParticipantId(participantId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/question/{questionId}")
    @Operation(summary = "Get answers by question ID", description = "Retrieve all answers submitted for a specific question")
    public ResponseEntity<List<PlayerAnswerDTO>> getAnswersByQuestionId(
            @Parameter(description = "Question ID")
            @PathVariable UUID questionId) {
        List<PlayerAnswerDTO> answers = playerAnswerService.getAnswersByQuestionId(questionId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/participant/{participantId}/score")
    @Operation(summary = "Calculate total score", description = "Calculate total score for a participant")
    public ResponseEntity<Integer> calculateTotalScore(
            @Parameter(description = "Participant ID")
            @PathVariable UUID participantId) {
        Integer totalScore = playerAnswerService.calculateTotalScore(participantId);
        return ResponseEntity.ok(totalScore);
    }
}
