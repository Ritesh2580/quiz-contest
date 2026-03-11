package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Quiz entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Quiz information and metadata")
public class QuizDTO {

    @Schema(description = "Unique quiz identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Quiz title", example = "General Knowledge Quiz")
    private String title;

    @Schema(description = "Quiz description", example = "Test your knowledge on various topics")
    private String description;

    @Schema(description = "Quiz banner image ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID bannerImageId;

    @Schema(description = "Quiz start date and time", example = "2024-02-23T10:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "Quiz end date and time", example = "2024-02-23T12:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "Quiz duration in minutes", example = "120")
    private Integer durationMinutes;

    @Schema(description = "User ID of quiz creator", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID createdBy;

    @Schema(description = "Quiz creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Quiz last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Optimistic locking version", example = "1")
    private Integer version;
}
