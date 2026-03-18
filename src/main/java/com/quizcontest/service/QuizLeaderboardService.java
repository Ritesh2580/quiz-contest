package com.quizcontest.service;

import com.quizcontest.dto.QuizLeaderboardDTO;
import com.quizcontest.entity.QuizLeaderboard;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizLeaderboardRepository;
import com.quizcontest.service.interfaces.IQuizLeaderboardService;
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
import java.util.stream.IntStream;

import static com.quizcontest.config.RedisCacheConfig.CACHE_LEADERBOARD;

/**
 * Service implementation for managing {@link QuizLeaderboard} entities.
 * <p>
 * This service provides comprehensive leaderboard and ranking functionality including:
 * <ul>
 *   <li>Leaderboard entry creation and updates</li>
 *   <li>Rank calculation based on score and completion time</li>
 *   <li>Leaderboard retrieval by quiz</li>
 *   <li>Top N leaderboard entries retrieval</li>
 *   <li>User rank lookup within a quiz</li>
 * </ul>
 * </p>
 *
 * <p><b>Ranking Algorithm:</b></p>
 * <p>
 * Participants are ranked primarily by total score (descending), and secondarily
 * by completion time (ascending) when scores are tied. Uses Java 17 Stream API
 * with {@code IntStream} for efficient rank assignment.
 * </p>
 *
 * <p><b>Caching:</b></p>
 * <p>
 * This service uses Redis caching with the following key patterns:
 * <ul>
 *   <li>{@code leaderboard:{id}} - Individual entry by ID</li>
 *   <li>{@code leaderboard:quiz:{quizId}} - Full leaderboard for a quiz</li>
 *   <li>{@code leaderboard:rank:{quizId}:{userId}} - User's rank</li>
 *   <li>{@code leaderboard:top:{quizId}:{limit}} - Top N entries</li>
 * </ul>
 * Cache TTL is 10 minutes. Report cache is also evicted when leaderboard changes.
 * </p>
 *
 * @author Quiz Contest Team
 * @version 1.0.0
 * @see IQuizLeaderboardService
 * @see QuizLeaderboardRepository
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizLeaderboardService implements IQuizLeaderboardService {

    /** Repository for accessing leaderboard data. */
    private final QuizLeaderboardRepository quizLeaderboardRepository;

    /** Mapper for converting between entities and DTOs. */
    private final ModelMapper modelMapper;

    /**
     * Creates a new leaderboard entry or updates an existing one.
     * <p>
     * If an entry already exists for the specified quiz and user, it is updated
     * with the new score and time. Otherwise, a new entry is created with:
     * <ul>
     *   <li>ID: Auto-generated UUID</li>
     *   <li>Rank: 1 (will be recalculated when leaderboard is retrieved)</li>
     *   <li>version: 1 (or incremented for existing entries)</li>
     * </ul>
     * After creation/update, both leaderboard and report caches are evicted.
     * </p>
     *
     * @param quizId the unique identifier of the quiz
     * @param userId the unique identifier of the user
     * @param score the total score achieved
     * @param timeTakenSeconds the time taken to complete the quiz in seconds
     * @return the created or updated leaderboard entry as a DTO
     */
    @Override
    @CacheEvict(value = {CACHE_LEADERBOARD, "reports"}, allEntries = true)
    public QuizLeaderboardDTO createOrUpdateLeaderboardEntry(UUID quizId, UUID userId, Integer score, Integer timeTakenSeconds) {
        // Check if entry already exists
        QuizLeaderboard existingEntry = quizLeaderboardRepository.findAll().stream()
                .filter(entry -> entry.getQuizId().equals(quizId) && entry.getPlayerId().equals(userId))
                .findFirst()
                .orElse(null);

        QuizLeaderboard leaderboard;
        if (existingEntry != null) {
            // Update existing entry
            existingEntry.setTotalScore(score);
            existingEntry.setCompletionTimeMinutes(timeTakenSeconds);
            existingEntry.setUpdatedAt(LocalDateTime.now());
            existingEntry.setVersion(existingEntry.getVersion() + 1);
            leaderboard = quizLeaderboardRepository.save(existingEntry);
        } else {
            // Create new entry
            leaderboard = new QuizLeaderboard();
            leaderboard.setId(UUID.randomUUID());
            leaderboard.setQuizId(quizId);
            leaderboard.setPlayerId(userId);
            leaderboard.setRank(1); // Will be updated when calculating rankings
            leaderboard.setTotalScore(score);
            leaderboard.setCompletionTimeMinutes(timeTakenSeconds);
            leaderboard.setCreatedAt(LocalDateTime.now());
            leaderboard.setUpdatedAt(LocalDateTime.now());
            leaderboard.setVersion(1);
            leaderboard = quizLeaderboardRepository.save(leaderboard);
        }

        return convertToDTO(leaderboard);
    }

    /**
     * Retrieves the leaderboard for a specific quiz with calculated rankings.
     * <p>
     * Entries are sorted by:
     * <ol>
     *   <li>Total score (descending - higher scores first)</li>
     *   <li>Completion time (ascending - faster times first for ties)</li>
     * </ol>
     * Rank is dynamically assigned using {@code IntStream.range()} for each position.
     * </p>
     * <p>
     * Cached with key pattern: {@code leaderboard:quiz:{quizId}}. TTL: 10 minutes.
     * </p>
     *
     * @param quizId the unique identifier of the quiz
     * @return a list of leaderboard entries sorted by rank
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LEADERBOARD, key = "'quiz:' + #quizId")
    public List<QuizLeaderboardDTO> getLeaderboardByQuizId(UUID quizId) {
        List<QuizLeaderboard> entries = quizLeaderboardRepository.findAll().stream()
                .filter(entry -> entry.getQuizId().equals(quizId))
                .sorted((a, b) -> {
                    // Sort by score descending, then by time ascending
                    int scoreComparison = b.getTotalScore().compareTo(a.getTotalScore());
                    if (scoreComparison != 0) {
                        return scoreComparison;
                    }
                    return a.getCompletionTimeMinutes().compareTo(b.getCompletionTimeMinutes());
                })
                .collect(Collectors.toList());

        // Update ranks using IntStream
        IntStream.range(0, entries.size())
                .forEach(i -> entries.get(i).setRank(i + 1));

        return entries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a leaderboard entry by its unique identifier.
     * <p>
     * Cached with key pattern: {@code leaderboard:{id}}. TTL: 10 minutes.
     * </p>
     *
     * @param id the unique identifier of the leaderboard entry
     * @return the leaderboard entry as a DTO
     * @throws ResourceNotFoundException if no entry is found with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LEADERBOARD, key = "#id")
    public QuizLeaderboardDTO getLeaderboardEntryById(UUID id) {
        QuizLeaderboard entry = quizLeaderboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard entry not found with ID: " + id));
        return convertToDTO(entry);
    }

    /**
     * Retrieves a user's rank within a specific quiz leaderboard.
     * <p>
     * Returns the rank position (1-based) of the user, or {@code null} if the
     * user has not participated in the quiz. Uses Stream API's {@code findFirst()}
     * for efficient lookup.
     * </p>
     * <p>
     * Cached with key pattern: {@code leaderboard:rank:{quizId}:{userId}}.
     * TTL: 10 minutes.
     * </p>
     *
     * @param quizId the unique identifier of the quiz
     * @param userId the unique identifier of the user
     * @return the user's rank position, or {@code null} if not found
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LEADERBOARD, key = "'rank:' + #quizId + ':' + #userId")
    public Integer getUserRank(UUID quizId, UUID userId) {
        List<QuizLeaderboardDTO> leaderboard = getLeaderboardByQuizId(quizId);
        return leaderboard.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .map(QuizLeaderboardDTO::getRank)
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves the top N entries from a quiz leaderboard.
     * <p>
     * Returns the highest-ranked participants up to the specified limit.
     * Uses Stream API's {@code limit()} for efficient truncation.
     * </p>
     * <p>
     * Cached with key pattern: {@code leaderboard:top:{quizId}:{limit}}.
     * TTL: 10 minutes.
     * </p>
     *
     * @param quizId the unique identifier of the quiz
     * @param limit the maximum number of entries to return
     * @return a list of the top N leaderboard entries
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_LEADERBOARD, key = "'top:' + #quizId + ':' + #limit")
    public List<QuizLeaderboardDTO> getTopLeaderboardEntries(UUID quizId, Integer limit) {
        return getLeaderboardByQuizId(quizId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Converts a {@link QuizLeaderboard} entity to a {@link QuizLeaderboardDTO}.
     *
     * @param leaderboard the leaderboard entity to convert
     * @return the converted leaderboard DTO
     */
    private QuizLeaderboardDTO convertToDTO(QuizLeaderboard leaderboard) {
        return modelMapper.map(leaderboard, QuizLeaderboardDTO.class);
    }
}
