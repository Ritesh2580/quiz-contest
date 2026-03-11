package com.quizcontest.service;

import com.quizcontest.dto.QuizLeaderboardDTO;
import com.quizcontest.entity.QuizLeaderboard;
import com.quizcontest.exception.ResourceNotFoundException;
import com.quizcontest.repository.QuizLeaderboardRepository;
import com.quizcontest.service.interfaces.IQuizLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for QuizLeaderboard entity
 * Handles leaderboard and ranking functionality
 * Implements IQuizLeaderboardService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizLeaderboardService implements IQuizLeaderboardService {

    private final QuizLeaderboardRepository quizLeaderboardRepository;
    private final ModelMapper modelMapper;

    /**
     * Create or update leaderboard entry
     */
    @Override
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
     * Get leaderboard for a quiz (sorted by score and time)
     */
    @Override
    @Transactional(readOnly = true)
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

        // Update ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get leaderboard entry by ID
     */
    @Override
    @Transactional(readOnly = true)
    public QuizLeaderboardDTO getLeaderboardEntryById(UUID id) {
        QuizLeaderboard entry = quizLeaderboardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leaderboard entry not found with ID: " + id));
        return convertToDTO(entry);
    }

    /**
     * Get user's rank in a quiz leaderboard
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getUserRank(UUID quizId, UUID userId) {
        List<QuizLeaderboardDTO> leaderboard = getLeaderboardByQuizId(quizId);
        return leaderboard.stream()
                .filter(entry -> entry.getUserId().equals(userId))
                .map(QuizLeaderboardDTO::getRank)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get top N entries from leaderboard
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizLeaderboardDTO> getTopLeaderboardEntries(UUID quizId, Integer limit) {
        return getLeaderboardByQuizId(quizId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Convert QuizLeaderboard entity to QuizLeaderboardDTO using ModelMapper
     */
    private QuizLeaderboardDTO convertToDTO(QuizLeaderboard leaderboard) {
        return modelMapper.map(leaderboard, QuizLeaderboardDTO.class);
    }
}
