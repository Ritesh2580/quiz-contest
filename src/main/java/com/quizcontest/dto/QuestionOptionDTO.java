package com.quizcontest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for QuestionOption entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Question option/choice information")
public class QuestionOptionDTO {

    @Schema(description = "Unique option identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Question ID this option belongs to", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID questionId;

    @Schema(description = "Option text", example = "Paris")
    private String optionText;

    @Schema(description = "Option image ID (optional)", example = "550e8400-e29b-41d4-a716-446655440002")
    private UUID optionImageId;

    @Schema(description = "Whether this is the correct answer", example = "true")
    private Boolean isCorrect;

    @Schema(description = "Display order of option", example = "1")
    private Integer displayOrder;

    @Schema(description = "Option creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Option last update timestamp")
    private LocalDateTime updatedAt;

    @Schema(description = "Optimistic locking version", example = "1")
    private Integer version;
}
