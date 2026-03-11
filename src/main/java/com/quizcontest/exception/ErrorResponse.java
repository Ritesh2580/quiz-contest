package com.quizcontest.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response format for API errors
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private Integer status;

    @Schema(description = "Error message", example = "Quiz not found")
    private String message;

    @Schema(description = "Error details", example = "Quiz with ID 550e8400-e29b-41d4-a716-446655440000 not found")
    private String details;

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "API endpoint that caused the error", example = "/api/v1/quizzes/550e8400-e29b-41d4-a716-446655440000")
    private String path;
}
