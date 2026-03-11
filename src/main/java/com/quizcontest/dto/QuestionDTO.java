package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Question entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Question information")
public class QuestionDTO {

    @Schema(description = "Unique question identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Quiz ID this question belongs to", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID quizId;

    @Schema(description = "Question text", example = "What is the capital of France?")
    private String questionText;

    @Schema(description = "Question type (yes_no, multiple_choice, number, text)", example = "multiple_choice")
    private String questionType;

    @Schema(description = "Time limit in seconds for this question", example = "30")
    private Integer timeLimitSeconds;

    @Schema(description = "Display order of question in quiz", example = "1")
    private Integer displayOrder;

    @Schema(description = "Question creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Question last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Optimistic locking version", example = "1")
    private Integer version;
}
