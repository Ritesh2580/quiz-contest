package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for PlayerAnswer entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Player answer submission information")
public class PlayerAnswerDTO {

    @Schema(description = "Unique answer identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Question ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID questionId;

    @Schema(description = "Quiz participant ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID participantId;

    @Schema(description = "Answer text/value", example = "Paris")
    private String answerText;

    @Schema(description = "Time taken to answer in seconds", example = "15")
    private Integer timeTakenSeconds;

    @Schema(description = "Whether the answer is correct", example = "true")
    private Boolean isCorrect;

    @Schema(description = "Score awarded for this answer", example = "10")
    private Integer score;

    @Schema(description = "Answer submission timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Answer last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Optimistic locking version", example = "1")
    private Integer version;
}
