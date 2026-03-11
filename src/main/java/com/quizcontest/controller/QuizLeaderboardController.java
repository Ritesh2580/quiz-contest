package com.quizcontest.controller;

import com.quizcontest.dto.QuizLeaderboardDTO;
import com.quizcontest.service.interfaces.IQuizLeaderboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Quiz Leaderboard management
 * Uses IQuizLeaderboardService interface for Dependency Inversion Principle
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Quiz leaderboard and ranking endpoints")
public class QuizLeaderboardController {

    private final IQuizLeaderboardService quizLeaderboardService;

    @PostMapping
    @Operation(summary = "Create or update leaderboard entry", description = "Create or update a leaderboard entry for a user in a quiz")
    public ResponseEntity<QuizLeaderboardDTO> createOrUpdateLeaderboardEntry(
            @Parameter(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
            @RequestParam UUID quizId,
            @Parameter(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440002")
            @RequestParam UUID userId,
            @Parameter(description = "Score", example = "85")
            @RequestParam Integer score,
            @Parameter(description = "Time taken in seconds", example = "600")
            @RequestParam Integer timeTakenSeconds) {
        QuizLeaderboardDTO leaderboardDTO = quizLeaderboardService.createOrUpdateLeaderboardEntry(quizId, userId, score, timeTakenSeconds);
        return ResponseEntity.status(HttpStatus.CREATED).body(leaderboardDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leaderboard entry by ID")
    public ResponseEntity<QuizLeaderboardDTO> getLeaderboardEntryById(@PathVariable UUID id) {
        QuizLeaderboardDTO leaderboardDTO = quizLeaderboardService.getLeaderboardEntryById(id);
        return ResponseEntity.ok(leaderboardDTO);
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get leaderboard by quiz ID", description = "Retrieve the complete leaderboard for a specific quiz")
    public ResponseEntity<List<QuizLeaderboardDTO>> getLeaderboardByQuizId(
            @Parameter(description = "Quiz ID")
            @PathVariable UUID quizId) {
        List<QuizLeaderboardDTO> leaderboard = quizLeaderboardService.getLeaderboardByQuizId(quizId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/quiz/{quizId}/rank")
    @Operation(summary = "Get user's rank", description = "Get a specific user's rank in a quiz leaderboard")
    public ResponseEntity<Integer> getUserRank(
            @Parameter(description = "Quiz ID")
            @PathVariable UUID quizId,
            @Parameter(description = "User ID")
            @RequestParam UUID userId) {
        Integer rank = quizLeaderboardService.getUserRank(quizId, userId);
        return ResponseEntity.ok(rank);
    }

    @GetMapping("/quiz/{quizId}/top")
    @Operation(summary = "Get top leaderboard entries", description = "Retrieve top N entries from a quiz leaderboard")
    public ResponseEntity<List<QuizLeaderboardDTO>> getTopLeaderboardEntries(
            @Parameter(description = "Quiz ID")
            @PathVariable UUID quizId,
            @Parameter(description = "Number of top entries to retrieve", example = "10")
            @RequestParam(defaultValue = "10") Integer limit) {
        List<QuizLeaderboardDTO> topEntries = quizLeaderboardService.getTopLeaderboardEntries(quizId, limit);
        return ResponseEntity.ok(topEntries);
    }
}
