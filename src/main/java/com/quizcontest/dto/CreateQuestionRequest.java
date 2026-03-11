package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a new question
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for question creation")
public class CreateQuestionRequest {

    @NotNull(message = "Quiz ID is required")
    @Schema(description = "Quiz ID this question belongs to", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID quizId;

    @NotBlank(message = "Question text is required")
    @Schema(description = "Question text", example = "What is the capital of France?")
    private String questionText;

    @NotBlank(message = "Question type is required")
    @Schema(description = "Question type (yes_no, multiple_choice, number, text)", example = "multiple_choice")
    private String questionType;

    @NotNull(message = "Time limit is required")
    @Min(value = 1, message = "Time limit must be at least 1 second")
    @Schema(description = "Time limit in seconds for this question", example = "30")
    private Integer timeLimitSeconds;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    @Schema(description = "Display order of question in quiz", example = "1")
    private Integer displayOrder;
}
