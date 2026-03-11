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
 * Request DTO for submitting an answer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for answer submission")
public class SubmitAnswerRequest {

    @NotNull(message = "Question ID is required")
    @Schema(description = "Question ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID questionId;

    @NotNull(message = "Participant ID is required")
    @Schema(description = "Quiz participant ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID participantId;

    @NotBlank(message = "Answer text is required")
    @Schema(description = "Answer text/value", example = "Paris")
    private String answerText;

    @NotNull(message = "Time taken is required")
    @Min(value = 0, message = "Time taken cannot be negative")
    @Schema(description = "Time taken to answer in seconds", example = "15")
    private Integer timeTakenSeconds;
}
