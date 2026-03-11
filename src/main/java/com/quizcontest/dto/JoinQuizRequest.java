package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for joining a quiz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for joining a quiz")
public class JoinQuizRequest {

    @NotNull(message = "Quiz ID is required")
    @Schema(description = "Quiz ID to join", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID quizId;

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID of the participant", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID userId;
}
