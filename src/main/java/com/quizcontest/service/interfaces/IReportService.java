package com.quizcontest.service.interfaces;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * Interface for Report Service
 * Handles generation of Excel reports for quiz results
 */
public interface IReportService {

    /**
     * Generate a user-specific report showing all quizzes attempted by a user
     * with questions solved and scores for each quiz
     *
     * @param userId the user ID
     * @return ByteArrayInputStream containing the Excel file
     */
    ByteArrayInputStream generateUserReport(UUID userId);

    /**
     * Generate a consolidated quiz report showing all users who attempted a quiz
     * with tabs organized by user email, showing questions solved and scores
     *
     * @param quizId the quiz ID
     * @return ByteArrayInputStream containing the Excel file
     */
    ByteArrayInputStream generateQuizConsolidatedReport(UUID quizId);
}
