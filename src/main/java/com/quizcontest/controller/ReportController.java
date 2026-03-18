package com.quizcontest.controller;

import com.quizcontest.service.interfaces.IReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.UUID;

/**
 * REST Controller for generating Excel reports for quiz statistics.
 * <p>
 * This controller provides endpoints for downloading Excel reports that contain
 * detailed quiz participation and performance data. Reports are generated on-demand
 * and cached for improved performance on subsequent requests.
 * </p>
 *
 * <p><b>Available Reports:</b></p>
 * <ul>
 *   <li><b>User Report:</b> Shows all quizzes attempted by a specific user with
 *       performance metrics for each quiz</li>
 *   <li><b>Consolidated Quiz Report:</b> Shows all participants for a specific quiz
 *       with detailed question-by-question breakdown per participant</li>
 * </ul>
 *
 * <p><b>Response Format:</b></p>
 * <p>
 * All endpoints return Excel files (.xlsx) as downloadable attachments with
 * appropriate Content-Disposition headers.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IReportService
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report generation endpoints for quiz statistics")
public class ReportController {

    /** Service for generating Excel reports. */
    private final IReportService reportService;

    /**
     * Generates and downloads a user-specific report.
     * <p>
     * The report contains a single worksheet with the following columns:
     * <ul>
     *   <li>Quiz Title</li>
     *   <li>Quiz Description</li>
     *   <li>Total Questions</li>
     *   <li>Questions Solved</li>
     *   <li>Total Score</li>
     *   <li>Status</li>
     *   <li>Joined At</li>
     *   <li>Completed At</li>
     * </ul>
     * </p>
     *
     * <p>The report is cached for 2 hours to improve performance on repeated requests.</p>
     *
     * @param userId the unique identifier of the user (UUID format)
     * @return a ResponseEntity containing the Excel report as a downloadable resource
     */
    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Generate user-specific report",
            description = "Generate an Excel report showing all quizzes attempted by a user, " +
                    "including questions solved and scores for each quiz"
    )
    public ResponseEntity<InputStreamResource> generateUserReport(
            @Parameter(description = "User ID", required = true)
            @PathVariable UUID userId) {

        ByteArrayInputStream reportStream = reportService.generateUserReport(userId);

        InputStreamResource resource = new InputStreamResource(reportStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=user-report-" + userId + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(reportStream.available())
                .body(resource);
    }

    /**
     * Generates and downloads a consolidated quiz report.
     * <p>
     * The report is a multi-sheet Excel workbook containing:
     * <ul>
     *   <li><b>Summary Sheet:</b> Overview of all participants with:
     *     <ul>
     *       <li>User Email</li>
     *       <li>Full Name</li>
     *       <li>Questions Solved</li>
     *       <li>Total Score</li>
     *       <li>Status</li>
     *       <li>Joined At / Completed At timestamps</li>
     *     </ul>
     *   </li>
     *   <li><b>Individual Sheets:</b> One sheet per participant (named by email) containing:
     *     <ul>
     *       <li>Question-by-question breakdown</li>
     *       <li>User's answers</li>
     *       <li>Correctness indicators (color-coded)</li>
     *       <li>Points earned per question</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     *
     * <p>The report is cached for 2 hours to improve performance on repeated requests.</p>
     *
     * @param quizId the unique identifier of the quiz (UUID format)
     * @return a ResponseEntity containing the Excel report as a downloadable resource
     */
    @GetMapping("/quiz/{quizId}")
    @Operation(
            summary = "Generate consolidated quiz report",
            description = "Generate an Excel report showing all users who attempted a quiz, " +
                    "with separate tabs for each user's email containing their answers and scores"
    )
    public ResponseEntity<InputStreamResource> generateQuizConsolidatedReport(
            @Parameter(description = "Quiz ID", required = true)
            @PathVariable UUID quizId) {

        ByteArrayInputStream reportStream = reportService.generateQuizConsolidatedReport(quizId);

        InputStreamResource resource = new InputStreamResource(reportStream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=quiz-consolidated-report-" + quizId + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(reportStream.available())
                .body(resource);
    }
}
