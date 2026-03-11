package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for QuizParticipant entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Quiz participant information")
public class QuizParticipantDTO {

    @Schema(description = "Unique participant identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Quiz ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID quizId;

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID userId;

    @Schema(description = "Participation status (pending, in_progress, completed, abandoned)", example = "in_progress")
    private String status;

    @Schema(description = "Current score", example = "75")
    private Integer score;

    @Schema(description = "Quiz start timestamp")
    private LocalDateTime startedAt;

    @Schema(description = "Quiz completion timestamp")
    private LocalDateTime completedAt;

    @Schema(description = "Participant record creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Participant record last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Optimistic locking version", example = "1")
    private Integer version;
}
