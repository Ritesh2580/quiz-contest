package com.quizcontest.service;

import com.quizcontest.dto.JoinQuizRequest;
import com.quizcontest.dto.QuizParticipantDTO;
import com.quizcontest.entity.QuizParticipant;
import com.quizcontest.exception.InvalidOperationException;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizParticipantRepository;
import com.quizcontest.service.interfaces.IQuizParticipantService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.quizcontest.config.RedisCacheConfig.CACHE_QUIZ_PARTICIPANTS;

/**
 * Service implementation for managing {@link QuizParticipant} entities.
 * <p>
 * This service provides comprehensive quiz participation management including:
 * <ul>
 *   <li>Quiz joining with duplicate prevention</li>
 *   <li>Quiz status lifecycle management (joined → in_progress → completed)</li>
 *   <li>Participant retrieval by ID, quiz, or user</li>
 *   <li>Score tracking and completion recording</li>
 * </ul>
 * </p>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching with the following key patterns:
 * <ul>
 *   <li>{@code quizParticipants:{id}} - Individual participant by ID</li>
 *   <li>{@code quizParticipants:quiz:{quizId}} - Participants by specific quiz</li>
 *   <li>{@code quizParticipants:user:{userId}} - Participations by specific user</li>
 * </ul>
 * Cache TTL is 15 minutes. Report cache is also evicted when participation data changes.
 * </p>
 *
 * <p><b>Status Lifecycle:</b></p>
 * <ol>
 *   <li>{@code joined} - User has joined the quiz but not started</li>
 *   <li>{@code in_progress} - User has started answering questions</li>
 *   <li>{@code completed} - User has finished the quiz</li>
 * </ol>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IQuizParticipantService
 * @see QuizParticipantRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizParticipantService implements IQuizParticipantService {

    /** Repository for accessing quiz participant data. */
    private final QuizParticipantRepository quizParticipantRepository;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Allows a user to join a quiz.
     * <p>
     * Prevents duplicate participation by checking if the user is already
     * a participant. Initializes participant with:
     * <ul>
     *   <li>ID: Auto-generated UUID</li>
     *   <li>Status: "joined"</li>
     *   <li>Total Score: 0</li>
     *   <li>version: 1</li>
     * </ul>
     * After joining, both participant and report caches are evicted.
     * </p>
     *
     * @param request the join request containing quiz ID and user ID
     * @return the created participant record as a DTO
     * @throws InvalidOperationException if the user is already a participant
     */
    @Override
    @CacheEvict(value = {CACHE_QUIZ_PARTICIPANTS, "reports"}, allEntries = true)
    public QuizParticipantDTO joinQuiz(JoinQuizRequest request) {
        // Check if user is already a participant
        boolean alreadyJoined = quizParticipantRepository.findAll().stream()
                .anyMatch(p -> p.getQuizId().equals(request.getQuizId()) && 
                             p.getPlayerId().equals(request.getUserId()));
        
        if (alreadyJoined) {
            throw new InvalidOperationException("User is already a participant in this quiz");
        }

        QuizParticipant participant = new QuizParticipant();
        participant.setId(UUID.randomUUID());
        participant.setQuizId(request.getQuizId());
        participant.setPlayerId(request.getUserId());
        participant.setStatus("joined");
        participant.setTotalScore(0);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setCreatedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());
        participant.setVersion(1);

        QuizParticipant savedParticipant = quizParticipantRepository.save(participant);
        return convertToDTO(savedParticipant);
    }

    /**
     * Retrieves a quiz participant by their unique identifier.
     * <p>
     * Cached with key pattern: {@code quizParticipants:{id}}. TTL: 15 minutes.
     * </p>
     *
     * @param id the unique identifier of the participant
     * @return the participant as a DTO
     * @throws ResourceNotFoundException if no participant is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUIZ_PARTICIPANTS, key = "#id")
    public QuizParticipantDTO getParticipantById(UUID id) {
        QuizParticipant participant = quizParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz participant not found with ID: " + id));
        return convertToDTO(participant);
    }

    /**
     * Retrieves all participants for a specific quiz.
     * <p>
     * Cached with key pattern: {@code quizParticipants:quiz:{quizId}}. TTL: 15 minutes.
     * </p>
     *
     * @param quizId the unique identifier of the quiz
     * @return a list of participants for the specified quiz
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUIZ_PARTICIPANTS, key = "'quiz:' + #quizId")
    public List<QuizParticipantDTO> getParticipantsByQuizId(UUID quizId) {
        return quizParticipantRepository.findAll().stream()
                .filter(p -> p.getQuizId().equals(quizId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all quiz participations for a specific user.
     * <p>
     * Cached with key pattern: {@code quizParticipants:user:{userId}}. TTL: 15 minutes.
     * </p>
     *
     * @param userId the unique identifier of the user
     * @return a list of the user's quiz participations
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_QUIZ_PARTICIPANTS, key = "'user:' + #userId")
    public List<QuizParticipantDTO> getParticipationsByUserId(UUID userId) {
        return quizParticipantRepository.findAll().stream()
                .filter(p -> p.getPlayerId().equals(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marks a quiz as started for a participant.
     * <p>
     * Changes the participant status from "joined" to "in_progress" and
     * records the start timestamp. Only allowed if the quiz hasn't been started yet.
     * </p>
     *
     * @param participantId the unique identifier of the participant
     * @return the updated participant record as a DTO
     * @throws ResourceNotFoundException if no participant is found
     * @throws InvalidOperationException if the quiz has already been started or completed
     */
    @Override
    @CachePut(value = CACHE_QUIZ_PARTICIPANTS, key = "#participantId")
    @CacheEvict(value = CACHE_QUIZ_PARTICIPANTS, key = "'all'")
    public QuizParticipantDTO startQuiz(UUID participantId) {
        QuizParticipant participant = quizParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz participant not found with ID: " + participantId));
        
        if (!participant.getStatus().equals("pending")) {
            throw new InvalidOperationException("Quiz has already been started or completed");
        }

        participant.setStatus("in_progress");
        participant.setStartedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());
        participant.setVersion(participant.getVersion() + 1);

        QuizParticipant updatedParticipant = quizParticipantRepository.save(participant);
        return convertToDTO(updatedParticipant);
    }

    /**
     * Marks a quiz as completed for a participant.
     * <p>
     * Changes the participant status to "completed", records the final score,
     * and sets the completion timestamp. Only allowed if the quiz is in progress.
     * Both participant and report caches are evicted to ensure report consistency.
     * </p>
     *
     * @param participantId the unique identifier of the participant
     * @param finalScore the final score achieved by the participant
     * @return the updated participant record as a DTO
     * @throws ResourceNotFoundException if no participant is found
     * @throws InvalidOperationException if the quiz is not currently in progress
     */
    @Override
    @CachePut(value = CACHE_QUIZ_PARTICIPANTS, key = "#participantId")
    @CacheEvict(value = {CACHE_QUIZ_PARTICIPANTS, "reports"}, allEntries = true)
    public QuizParticipantDTO completeQuiz(UUID participantId, Integer finalScore) {
        QuizParticipant participant = quizParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz participant not found with ID: " + participantId));
        
        if (!participant.getStatus().equals("in_progress")) {
            throw new InvalidOperationException("Quiz is not currently in progress");
        }

        participant.setStatus("completed");
        participant.setTotalScore(finalScore);
        participant.setCompletedAt(LocalDateTime.now());
        participant.setUpdatedAt(LocalDateTime.now());
        participant.setVersion(participant.getVersion() + 1);

        QuizParticipant updatedParticipant = quizParticipantRepository.save(participant);
        return convertToDTO(updatedParticipant);
    }

    /**
     * Converts a {@link QuizParticipant} entity to a {@link QuizParticipantDTO}.
     *
     * @param participant the participant entity to convert
     * @return the converted participant DTO
     */
    private QuizParticipantDTO convertToDTO(QuizParticipant participant) {
        return modelMapper.map(participant, QuizParticipantDTO.class);
    }
}
