package com.quizcontest.service;

import com.quizcontest.dto.PlayerAnswerDTO;
import com.quizcontest.dto.SubmitAnswerRequest;
import com.quizcontest.entity.CorrectAnswer;
import com.quizcontest.entity.PlayerAnswer;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.CorrectAnswerRepository;
import com.quizcontest.repository.PlayerAnswerRepository;
import com.quizcontest.service.interfaces.IPlayerAnswerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.quizcontest.config.RedisCacheConfig.CACHE_PLAYER_ANSWERS;

/**
 * Service implementation for managing {@link PlayerAnswer} entities.
 * <p>
 * This service provides comprehensive answer submission and scoring functionality including:
 * <ul>
 *   <li>Answer submission with automatic correctness checking</li>
 *   <li>Score calculation (10 points per correct answer)</li>
 *   <li>Answer retrieval by ID, participant, or question</li>
 *   <li>Total score calculation for participants</li>
 * </ul>
 * </p>
 *
 * <p><b>Scoring Logic:</b></p>
 * <ul>
 *   <li>Correct answer: 10 points</li>
 *   <li>Incorrect answer: 0 points</li>
 * </ul>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching with the following key patterns:
 * <ul>
 *   <li>{@code playerAnswers:{id}} - Individual answer by ID</li>
 *   <li>{@code playerAnswers:participant:{participantId}} - Answers by participant</li>
 *   <li>{@code playerAnswers:question:{questionId}} - Answers by question</li>
 *   <li>{@code playerAnswers:score:{participantId}} - Calculated total score</li>
 * </ul>
 * Cache TTL is 15 minutes. Report cache is also evicted when answers are submitted.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IPlayerAnswerService
 * @see PlayerAnswerRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerAnswerService implements IPlayerAnswerService {

    /** Repository for accessing player answer data. */
    private final PlayerAnswerRepository playerAnswerRepository;

    /** Repository for accessing correct answer data. */
    private final CorrectAnswerRepository correctAnswerRepository;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Submits an answer for a question and calculates the score.
     * <p>
     * The answer is validated against the correct answers stored in the database.
     * If the answer matches any correct answer (case-insensitive), the player
     * receives 10 points. Otherwise, 0 points are awarded.
     * </p>
     * <p>
     * After submission, both player answer and report caches are evicted.
     * </p>
     *
     * @param request the answer submission request containing participant ID,
     *                question ID, answer text, and time taken
     * @return the submitted answer as a DTO with correctness and score information
     */
    @Override
    @CacheEvict(value = {CACHE_PLAYER_ANSWERS, "reports"}, allEntries = true)
    public PlayerAnswerDTO submitAnswer(SubmitAnswerRequest request) {
        // Get the correct answer for this question
        List<CorrectAnswer> correctAnswers = correctAnswerRepository.findAll().stream()
                .filter(ca -> ca.getQuestionId().equals(request.getQuestionId()))
                .collect(Collectors.toList());

        // Check if answer is correct
        boolean isCorrect = correctAnswers.stream()
                .anyMatch(ca -> ca.getAnswerValue().equalsIgnoreCase(request.getAnswerText()));

        // Calculate score (simple scoring: 10 points for correct, 0 for incorrect)
        int pointsEarned = isCorrect ? 10 : 0;

        PlayerAnswer answer = new PlayerAnswer();
        answer.setId(UUID.randomUUID());
        answer.setQuizParticipantId(request.getParticipantId());
        answer.setQuestionId(request.getQuestionId());
        answer.setAnswerValue(request.getAnswerText());
        answer.setIsCorrect(isCorrect);
        answer.setPointsEarned(pointsEarned);
        answer.setTimeTakenSeconds(request.getTimeTakenSeconds());
        answer.setAnsweredAt(LocalDateTime.now());
        answer.setCreatedAt(LocalDateTime.now());
        answer.setUpdatedAt(LocalDateTime.now());
        answer.setVersion(1);

        PlayerAnswer savedAnswer = playerAnswerRepository.save(answer);
        return convertToDTO(savedAnswer);
    }

    /**
     * Retrieves a player answer by its unique identifier.
     * <p>
     * Cached with key pattern: {@code playerAnswers:{id}}. TTL: 15 minutes.
     * </p>
     *
     * @param id the unique identifier of the answer
     * @return the answer as a DTO
     * @throws ResourceNotFoundException if no answer is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PLAYER_ANSWERS, key = "#id")
    public PlayerAnswerDTO getAnswerById(UUID id) {
        PlayerAnswer answer = playerAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player answer not found with ID: " + id));
        return convertToDTO(answer);
    }

    /**
     * Retrieves all answers submitted by a specific participant.
     * <p>
     * Cached with key pattern: {@code playerAnswers:participant:{participantId}}.
     * TTL: 15 minutes.
     * </p>
     *
     * @param participantId the unique identifier of the participant
     * @return a list of answers submitted by the participant
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PLAYER_ANSWERS, key = "'participant:' + #participantId")
    public List<PlayerAnswerDTO> getAnswersByParticipantId(UUID participantId) {
        return playerAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuizParticipantId().equals(participantId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all answers for a specific question.
     * <p>
     * Cached with key pattern: {@code playerAnswers:question:{questionId}}.
     * TTL: 15 minutes.
     * </p>
     *
     * @param questionId the unique identifier of the question
     * @return a list of answers for the specified question
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PLAYER_ANSWERS, key = "'question:' + #questionId")
    public List<PlayerAnswerDTO> getAnswersByQuestionId(UUID questionId) {
        return playerAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuestionId().equals(questionId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total score for a participant.
     * <p>
     * Sums up all points earned across all answers submitted by the participant.
     * Uses Java Stream API's {@code mapToInt} and {@code sum} for efficient calculation.
     * </p>
     * <p>
     * Cached with key pattern: {@code playerAnswers:score:{participantId}}.
     * TTL: 15 minutes.
     * </p>
     *
     * @param participantId the unique identifier of the participant
     * @return the total score achieved by the participant
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_PLAYER_ANSWERS, key = "'score:' + #participantId")
    public Integer calculateTotalScore(UUID participantId) {
        return playerAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuizParticipantId().equals(participantId))
                .mapToInt(PlayerAnswer::getPointsEarned)
                .sum();
    }

    /**
     * Converts a {@link PlayerAnswer} entity to a {@link PlayerAnswerDTO}.
     *
     * @param answer the answer entity to convert
     * @return the converted answer DTO
     */
    private PlayerAnswerDTO convertToDTO(PlayerAnswer answer) {
        return modelMapper.map(answer, PlayerAnswerDTO.class);
    }
}
