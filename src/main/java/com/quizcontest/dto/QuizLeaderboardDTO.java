package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for QuizLeaderboard entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Quiz leaderboard entry")
public class QuizLeaderboardDTO {

    @Schema(description = "Unique leaderboard entry identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID quizId;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID userId;

    @Schema(description = "User rank in leaderboard", example = "1")
    private Integer rank;

    @Schema(description = "Final score", example = "100")
    private Integer score;

    @Schema(description = "Total time taken in seconds", example = "120")
    private Integer timeTakenSeconds;

    @Schema(description = "Leaderboard entry creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Leaderboard entry last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Optimistic locking version", example = "1")
    private Integer version;
}
