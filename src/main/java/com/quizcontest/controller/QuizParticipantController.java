package com.quizcontest.controller;

import com.quizcontest.dto.JoinQuizRequest;
import com.quizcontest.dto.QuizParticipantDTO;
import com.quizcontest.service.interfaces.IQuizParticipantService;
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
 * REST Controller for Quiz Participant management
 * Uses IQuizParticipantService interface for Dependency Inversion Principle
 */
@RestController
@RequestMapping("/api/v1/participants")
@RequiredArgsConstructor
@Tag(name = "Quiz Participants", description = "Quiz participation management endpoints")
public class QuizParticipantController {

    private final IQuizParticipantService quizParticipantService;

    @PostMapping("/join")
    @Operation(summary = "Join a quiz", description = "Register a user to participate in a quiz")
    public ResponseEntity<QuizParticipantDTO> joinQuiz(@Valid @RequestBody JoinQuizRequest request) {
        QuizParticipantDTO participantDTO = quizParticipantService.joinQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(participantDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get participant by ID")
    public ResponseEntity<QuizParticipantDTO> getParticipantById(@PathVariable UUID id) {
        QuizParticipantDTO participantDTO = quizParticipantService.getParticipantById(id);
        return ResponseEntity.ok(participantDTO);
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get participants by quiz ID", description = "Retrieve all participants in a specific quiz")
    public ResponseEntity<List<QuizParticipantDTO>> getParticipantsByQuizId(
            @Parameter(description = "Quiz ID")
            @PathVariable UUID quizId) {
        List<QuizParticipantDTO> participants = quizParticipantService.getParticipantsByQuizId(quizId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get participations by user ID", description = "Retrieve all quizzes a user has participated in")
    public ResponseEntity<List<QuizParticipantDTO>> getParticipationsByUserId(
            @Parameter(description = "User ID")
            @PathVariable UUID userId) {
        List<QuizParticipantDTO> participations = quizParticipantService.getParticipationsByUserId(userId);
        return ResponseEntity.ok(participations);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start quiz", description = "Mark a participant's quiz as started")
    public ResponseEntity<QuizParticipantDTO> startQuiz(@PathVariable UUID id) {
        QuizParticipantDTO participantDTO = quizParticipantService.startQuiz(id);
        return ResponseEntity.ok(participantDTO);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete quiz", description = "Mark a participant's quiz as completed with final score")
    public ResponseEntity<QuizParticipantDTO> completeQuiz(
            @PathVariable UUID id,
            @Parameter(description = "Final score", example = "85")
            @RequestParam Integer finalScore) {
        QuizParticipantDTO participantDTO = quizParticipantService.completeQuiz(id, finalScore);
        return ResponseEntity.ok(participantDTO);
    }
}
