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
 * Request DTO for creating a question option
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for question option creation")
public class CreateQuestionOptionRequest {

    @NotNull(message = "Question ID is required")
    @Schema(description = "Question ID this option belongs to", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID questionId;

    @NotBlank(message = "Option text is required")
    @Schema(description = "Option text", example = "Paris")
    private String optionText;

    @NotNull(message = "Is correct flag is required")
    @Schema(description = "Whether this is the correct answer", example = "true")
    private Boolean isCorrect;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    @Schema(description = "Display order of option", example = "1")
    private Integer displayOrder;

    @Schema(description = "Option image ID (optional)", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID optionImageId;
}
