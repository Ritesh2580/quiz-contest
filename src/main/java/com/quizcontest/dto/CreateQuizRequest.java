package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a new quiz
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for quiz creation")
public class CreateQuizRequest {

    @NotBlank(message = "Quiz title is required")
    @Schema(description = "Quiz title", example = "General Knowledge Quiz")
    private String title;

    @Schema(description = "Quiz description", example = "Test your knowledge on various topics")
    private String description;

    @NotNull(message = "Start date and time is required")
    @Future(message = "Start date must be in the future")
    @Schema(description = "Quiz start date and time", example = "2024-02-23T10:00:00")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time is required")
    @Future(message = "End date must be in the future")
    @Schema(description = "Quiz end date and time", example = "2024-02-23T12:00:00")
    private LocalDateTime endDateTime;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Schema(description = "Quiz duration in minutes", example = "120")
    private Integer durationMinutes;

    @Schema(description = "Quiz banner image ID (optional)", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID bannerImageId;
}
