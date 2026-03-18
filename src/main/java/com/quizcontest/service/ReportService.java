package com.quizcontest.service;

import com.quizcontest.entity.*;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.*;
import com.quizcontest.service.interfaces.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.quizcontest.config.RedisCacheConfig.CACHE_REPORTS;

/**
 * Service implementation for generating Excel reports for quiz statistics.
 * <p>
 * This service provides comprehensive reporting functionality including:
 * <ul>
 *   <li>User-specific reports showing all quiz attempts and performance</li>
 *   <li>Consolidated quiz reports with individual sheets per participant</li>
 * </ul>
 * </p>
 *
 * <p><b>Report Types:</b></p>
 * <ul>
 *   <li><b>User Report:</b> Single sheet showing all quizzes attempted by a user with
 *       questions solved, scores, and status for each quiz</li>
 *   <li><b>Consolidated Quiz Report:</b> Multi-sheet workbook with a summary sheet
 *       and individual detail sheets for each participant organized by email</li>
 * </ul>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * Reports are cached in Redis with a 2-hour TTL to improve performance for
 * repeated requests. Cache keys follow the pattern:
 * <ul>
 *   <li>{@code reports:user:{userId}} - User-specific reports</li>
 *   <li>{@code reports:quiz:{quizId}} - Quiz consolidated reports</li>
 * </ul>
 * Cache is automatically evicted when related quiz data changes.
 * </p>
 *
 * <p><b>Excel Styling:</b></p>
 * <p>
 * Reports use professional styling with:
 * <ul>
 *   <li>Dark blue headers with white text</li>
 *   <li>Green highlighting for correct answers</li>
 *   <li>Red highlighting for incorrect/unanswered questions</li>
 *   <li>Auto-sized columns for optimal readability</li>
 * </ul>
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IReportService
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService implements IReportService {

    /** Repository for accessing user data. */
    private final UserRepository userRepository;

    /** Repository for accessing quiz data. */
    private final QuizRepository quizRepository;

    /** Repository for accessing quiz participant data. */
    private final QuizParticipantRepository quizParticipantRepository;

    /** Repository for accessing player answer data. */
    private final PlayerAnswerRepository playerAnswerRepository;

    /** Repository for accessing question data. */
    private final QuestionRepository questionRepository;

    /**
     * Generates a user-specific report showing all quiz attempts.
     * <p>
     * The report includes:
     * <ul>
     *   <li>User information (name, email)</li>
     *   <li>List of all attempted quizzes</li>
     *   <li>Total questions per quiz</li>
     *   <li>Questions solved correctly</li>
     *   <li>Total score achieved</li>
     *   <li>Participation status</li>
     *   <li>Join and completion timestamps</li>
     * </ul>
     * </p>
     *
     * <p>Cached with key: {@code reports:user:{userId}}. TTL: 2 hours.</p>
     *
     * @param userId the unique identifier of the user
     * @return a {@link ByteArrayInputStream} containing the Excel report
     * @throws ResourceNotFoundException if the user is not found
     * @throws RuntimeException if an error occurs during report generation
     */
    @Override
    @Cacheable(value = CACHE_REPORTS, key = "'user:' + #userId")
    public ByteArrayInputStream generateUserReport(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Create main sheet
            Sheet sheet = workbook.createSheet("User Quiz Report");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // Add report title
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("User Quiz Performance Report");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

            // Add user info
            Row userInfoRow = sheet.createRow(2);
            userInfoRow.createCell(0).setCellValue("User:");
            userInfoRow.createCell(1).setCellValue(user.getFullName() != null ? user.getFullName() : user.getUsername());
            userInfoRow.createCell(2).setCellValue("Email:");
            userInfoRow.createCell(3).setCellValue(user.getEmail());

            // Add headers for quiz data
            Row headerRow = sheet.createRow(4);
            String[] headers = {"Quiz Title", "Quiz Description", "Total Questions", "Questions Solved", "Total Score", "Status", "Joined At", "Completed At"};

            IntStream.range(0, headers.length)
                    .forEach(i -> {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                    });

            AtomicInteger rowNum = new AtomicInteger(5);

            quizParticipantRepository.findByPlayerId(userId).stream()
                    .map(participant -> quizRepository.findById(participant.getQuizId())
                            .map(quiz -> new QuizParticipantData(quiz, participant))
                            .orElse(null))
                    .filter(data -> data != null)
                    .forEach(data -> {
                        Quiz quiz = data.quiz();
                        QuizParticipant participant = data.participant();

                        long totalQuestions = questionRepository.countByQuizId(quiz.getId());
                        long questionsSolved = playerAnswerRepository.findByQuizParticipantId(participant.getId())
                                .stream()
                                .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                                .count();

                        Row row = sheet.createRow(rowNum.getAndIncrement());
                        row.createCell(0).setCellValue(quiz.getTitle());
                        row.createCell(1).setCellValue(quiz.getDescription() != null ? quiz.getDescription() : "N/A");
                        row.createCell(2).setCellValue((int) totalQuestions);
                        row.createCell(3).setCellValue((int) questionsSolved);
                        row.createCell(4).setCellValue(participant.getTotalScore());
                        row.createCell(5).setCellValue(participant.getStatus());
                        row.createCell(6).setCellValue(participant.getJoinedAt() != null ? participant.getJoinedAt().toString() : "N/A");
                        row.createCell(7).setCellValue(participant.getCompletedAt() != null ? participant.getCompletedAt().toString() : "N/A");

                        IntStream.range(0, headers.length)
                                .forEach(i -> row.getCell(i).setCellStyle(dataStyle));
                    });

            // Auto-size columns
            IntStream.range(0, headers.length)
                    .forEach(sheet::autoSizeColumn);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error generating user report for userId: {}", userId, e);
            throw new RuntimeException("Failed to generate user report", e);
        }
    }

    /**
     * Record holding quiz and participant data for stream processing.
     *
     * @param quiz the quiz entity
     * @param participant the quiz participant entity
     */
    private record QuizParticipantData(Quiz quiz, QuizParticipant participant) {}

    /**
     * Generates a consolidated quiz report with detailed participant information.
     * <p>
     * The report includes:
     * <ul>
     *   <li><b>Summary Sheet:</b> Overview of all participants with email, name,
     *       questions solved, total score, and status</li>
     *   <li><b>Individual Sheets:</b> One sheet per participant (named by email)
     *       containing detailed question-by-question breakdown with answers</li>
     * </ul>
     * </p>
     *
     * <p>Cached with key: {@code reports:quiz:{quizId}}. TTL: 2 hours.</p>
     *
     * @param quizId the unique identifier of the quiz
     * @return a {@link ByteArrayInputStream} containing the Excel report
     * @throws ResourceNotFoundException if the quiz is not found
     * @throws RuntimeException if an error occurs during report generation
     */
    @Override
    @Cacheable(value = CACHE_REPORTS, key = "'quiz:' + #quizId")
    public ByteArrayInputStream generateQuizConsolidatedReport(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            List<Question> questions = questionRepository.findByQuizIdOrderByOrderIndex(quizId);
            List<QuizParticipant> participants = quizParticipantRepository.findByQuizId(quizId);

            createSummarySheet(workbook, quiz, participants, questions);

            participants.stream()
                    .map(participant -> userRepository.findById(participant.getPlayerId())
                            .map(user -> new ParticipantUserData(participant, user))
                            .orElse(null))
                    .filter(data -> data != null)
                    .forEach(data -> {
                        String sheetName = generateUniqueSheetName(workbook, sanitizeSheetName(data.user().getEmail()));
                        createUserDetailSheet(workbook, sheetName, quiz, data.participant(), data.user(), questions);
                    });

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Error generating quiz consolidated report for quizId: {}", quizId, e);
            throw new RuntimeException("Failed to generate quiz consolidated report", e);
        }
    }

    /**
     * Record holding participant and user data for stream processing.
     *
     * @param participant the quiz participant entity
     * @param user the user entity
     */
    private record ParticipantUserData(QuizParticipant participant, User user) {}

    /**
     * Generates a unique sheet name for the Excel workbook.
     * <p>
     * If the base name already exists, appends a numeric suffix (_1, _2, etc.)
     * until a unique name is found.
     * </p>
     *
     * @param workbook the Excel workbook to check for existing sheet names
     * @param baseName the desired base name for the sheet
     * @return a unique sheet name
     */
    private String generateUniqueSheetName(Workbook workbook, String baseName) {
        return Stream.iterate(1, n -> n + 1)
                .map(n -> n == 1 ? baseName : baseName + "_" + (n - 1))
                .filter(name -> workbook.getSheet(name) == null)
                .findFirst()
                .orElse(baseName);
    }

    /**
     * Creates the summary sheet for the consolidated quiz report.
     * <p>
     * The summary sheet contains:
     * <ul>
     *   <li>Quiz title and description</li>
     *   <li>Total questions count</li>
     *   <li>Total participants count</li>
     *   <li>Participant details (email, name, questions solved, score, status)</li>
     * </ul>
     * </p>
     *
     * @param workbook the Excel workbook to add the sheet to
     * @param quiz the quiz entity
     * @param participants list of quiz participants
     * @param questions list of quiz questions
     */
    private void createSummarySheet(Workbook workbook, Quiz quiz, List<QuizParticipant> participants, List<Question> questions) {
        Sheet sheet = workbook.createSheet("Summary");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Quiz Consolidated Report");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

        Row quizInfoRow = sheet.createRow(2);
        quizInfoRow.createCell(0).setCellValue("Quiz Title:");
        quizInfoRow.createCell(1).setCellValue(quiz.getTitle());

        Row quizDescRow = sheet.createRow(3);
        quizDescRow.createCell(0).setCellValue("Description:");
        quizDescRow.createCell(1).setCellValue(quiz.getDescription() != null ? quiz.getDescription() : "N/A");

        Row totalQuestionsRow = sheet.createRow(4);
        totalQuestionsRow.createCell(0).setCellValue("Total Questions:");
        totalQuestionsRow.createCell(1).setCellValue(questions.size());

        Row totalParticipantsRow = sheet.createRow(5);
        totalParticipantsRow.createCell(0).setCellValue("Total Participants:");
        totalParticipantsRow.createCell(1).setCellValue(participants.size());

        String[] headers = {"User Email", "Full Name", "Questions Solved", "Total Score", "Status", "Joined At", "Completed At"};
        Row headerRow = sheet.createRow(7);
        IntStream.range(0, headers.length)
                .forEach(i -> {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                });

        AtomicInteger rowNum = new AtomicInteger(8);

        participants.stream()
                .map(participant -> userRepository.findById(participant.getPlayerId())
                        .map(user -> new ParticipantUserData(participant, user))
                        .orElse(null))
                .filter(data -> data != null)
                .forEach(data -> {
                    QuizParticipant participant = data.participant();
                    User user = data.user();

                    long questionsSolved = playerAnswerRepository.findByQuizParticipantId(participant.getId())
                            .stream()
                            .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()))
                            .count();

                    Row row = sheet.createRow(rowNum.getAndIncrement());
                    row.createCell(0).setCellValue(user.getEmail());
                    row.createCell(1).setCellValue(user.getFullName() != null ? user.getFullName() : user.getUsername());
                    row.createCell(2).setCellValue((int) questionsSolved);
                    row.createCell(3).setCellValue(participant.getTotalScore());
                    row.createCell(4).setCellValue(participant.getStatus());
                    row.createCell(5).setCellValue(participant.getJoinedAt() != null ? participant.getJoinedAt().toString() : "N/A");
                    row.createCell(6).setCellValue(participant.getCompletedAt() != null ? participant.getCompletedAt().toString() : "N/A");

                    IntStream.range(0, headers.length)
                            .forEach(i -> row.getCell(i).setCellStyle(dataStyle));
                });

        IntStream.range(0, headers.length)
                .forEach(sheet::autoSizeColumn);
    }

    /**
     * Creates a detailed sheet for a specific participant in the consolidated report.
     * <p>
     * The detail sheet contains:
     * <ul>
     *   <li>User information (name, email)</li>
     *   <li>Total score and participation status</li>
     *   <li>Question-by-question breakdown with:
     *     <ul>
     *       <li>Question text and type</li>
     *       <li>Points available</li>
     *       <li>User's answer</li>
     *       <li>Correctness indicator (Yes/No with color coding)</li>
     *       <li>Points earned</li>
     *     </ul>
     *   </li>
     * </ul>
     * </p>
     *
     * @param workbook the Excel workbook to add the sheet to
     * @param sheetName the name for the new sheet (typically user's email)
     * @param quiz the quiz entity
     * @param participant the participant entity
     * @param user the user entity
     * @param questions list of quiz questions
     */
    private void createUserDetailSheet(Workbook workbook, String sheetName, Quiz quiz,
                                       QuizParticipant participant, User user, List<Question> questions) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle correctStyle = createCorrectAnswerStyle(workbook);
        CellStyle incorrectStyle = createIncorrectAnswerStyle(workbook);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Quiz Results: " + quiz.getTitle());
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 5));

        Row userInfoRow = sheet.createRow(2);
        userInfoRow.createCell(0).setCellValue("User:");
        userInfoRow.createCell(1).setCellValue(user.getFullName() != null ? user.getFullName() : user.getUsername());
        userInfoRow.createCell(2).setCellValue("Email:");
        userInfoRow.createCell(3).setCellValue(user.getEmail());

        Row scoreRow = sheet.createRow(3);
        scoreRow.createCell(0).setCellValue("Total Score:");
        scoreRow.createCell(1).setCellValue(participant.getTotalScore());
        scoreRow.createCell(2).setCellValue("Status:");
        scoreRow.createCell(3).setCellValue(participant.getStatus());

        String[] headers = {"Question #", "Question Text", "Question Type", "Points", "Your Answer", "Correct Answer", "Is Correct", "Points Earned"};
        Row headerRow = sheet.createRow(5);
        IntStream.range(0, headers.length)
                .forEach(i -> {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                });

        List<PlayerAnswer> answers = playerAnswerRepository.findByQuizParticipantId(participant.getId());
        AtomicInteger rowNum = new AtomicInteger(6);

        IntStream.range(0, questions.size())
                .mapToObj(i -> new QuestionIndexData(i, questions.get(i)))
                .forEach(data -> {
                    Question question = data.question();
                    int index = data.index();

                    PlayerAnswer answer = answers.stream()
                            .filter(a -> a.getQuestionId().equals(question.getId()))
                            .findFirst()
                            .orElse(null);

                    Row row = sheet.createRow(rowNum.getAndIncrement());
                    row.createCell(0).setCellValue(index + 1);
                    row.createCell(1).setCellValue(question.getQuestionText());
                    row.createCell(2).setCellValue(question.getQuestionType());
                    row.createCell(3).setCellValue(question.getPoints());

                    Optional.ofNullable(answer)
                            .ifPresentOrElse(
                                    ans -> {
                                        row.createCell(4).setCellValue(ans.getAnswerValue() != null ? ans.getAnswerValue() : "N/A");
                                        row.createCell(5).setCellValue("See Correct Answer Table");
                                        row.createCell(6).setCellValue(Boolean.TRUE.equals(ans.getIsCorrect()) ? "Yes" : "No");
                                        row.createCell(7).setCellValue(ans.getPointsEarned());

                                        CellStyle answerStyle = Boolean.TRUE.equals(ans.getIsCorrect()) ? correctStyle : incorrectStyle;
                                        row.getCell(6).setCellStyle(answerStyle);
                                    },
                                    () -> {
                                        row.createCell(4).setCellValue("Not Answered");
                                        row.createCell(5).setCellValue("N/A");
                                        row.createCell(6).setCellValue("No");
                                        row.createCell(7).setCellValue(0);
                                        row.getCell(6).setCellStyle(incorrectStyle);
                                    }
                            );

                    IntStream.range(0, 6)
                            .filter(j -> j != 6)
                            .forEach(j -> row.getCell(j).setCellStyle(dataStyle));
                    row.getCell(7).setCellStyle(dataStyle);
                });

        IntStream.range(0, headers.length)
                .forEach(sheet::autoSizeColumn);
    }

    /**
     * Record holding question index and entity for stream processing.
     *
     * @param index the zero-based index of the question
     * @param question the question entity
     */
    private record QuestionIndexData(int index, Question question) {}

    /**
     * Sanitizes a string to be used as an Excel sheet name.
     * <p>
     * Excel sheet names cannot contain the following characters: \ / ? * [ ]
     * Additionally, the maximum length is 31 characters.
     * </p>
     *
     * @param name the original name to sanitize
     * @return a sanitized name safe for use as an Excel sheet name
     */
    private String sanitizeSheetName(String name) {
        // Excel sheet names cannot contain: \ / ? * [ ]
        // Max length is 31 characters
        String sanitized = name.replaceAll("[\\\\/\\?\\*\\[\\]]", "_");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 31);
        }
        return sanitized;
    }

    /**
     * Creates a styled cell style for header rows.
     * <p>
     * The header style features:
     * <ul>
     *   <li>Bold white text</li>
     *   <li>Dark blue background</li>
     *   <li>Thin borders on all sides</li>
     *   <li>Center alignment</li>
     * </ul>
     * </p>
     *
     * @param workbook the Excel workbook
     * @return the configured header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Creates a basic cell style for data rows.
     * <p>
     * Features thin borders on all sides.
     * </p>
     *
     * @param workbook the Excel workbook
     * @return the configured data cell style
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    /**
     * Creates a styled cell style for report titles.
     * <p>
     * The title style features:
     * <ul>
     *   <li>Bold text</li>
     *   <li>16-point font size</li>
     *   <li>Center alignment</li>
     * </ul>
     * </p>
     *
     * @param workbook the Excel workbook
     * @return the configured title cell style
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Creates a styled cell style for correct answers.
     * <p>
     * The correct answer style features:
     * <ul>
     *   <li>Bold green text</li>
     *   <li>Light green background</li>
     *   <li>Thin borders on all sides</li>
     * </ul>
     * </p>
     *
     * @param workbook the Excel workbook
     * @return the configured correct answer cell style
     */
    private CellStyle createCorrectAnswerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.GREEN.getIndex());
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Creates a styled cell style for incorrect/unanswered questions.
     * <p>
     * The incorrect answer style features:
     * <ul>
     *   <li>Red text</li>
     *   <li>Rose (light red) background</li>
     *   <li>Thin borders on all sides</li>
     * </ul>
     * </p>
     *
     * @param workbook the Excel workbook
     * @return the configured incorrect answer cell style
     */
    private CellStyle createIncorrectAnswerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
