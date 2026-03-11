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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for PlayerAnswer entity
 * Handles answer submission and scoring logic
 * Implements IPlayerAnswerService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PlayerAnswerService implements IPlayerAnswerService {

    private final PlayerAnswerRepository playerAnswerRepository;
    private final CorrectAnswerRepository correctAnswerRepository;
    private final ModelMapper modelMapper;

    /**
     * Submit an answer
     */
    @Override
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
     * Get answer by ID
     */
    @Override
    @Transactional(readOnly = true)
    public PlayerAnswerDTO getAnswerById(UUID id) {
        PlayerAnswer answer = playerAnswerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player answer not found with ID: " + id));
        return convertToDTO(answer);
    }

    /**
     * Get answers by participant ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlayerAnswerDTO> getAnswersByParticipantId(UUID participantId) {
        return playerAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuizParticipantId().equals(participantId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get answers by question ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlayerAnswerDTO> getAnswersByQuestionId(UUID questionId) {
        return playerAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuestionId().equals(questionId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Calculate total score for a participant
     */
    @Override
    @Transactional(readOnly = true)
    public Integer calculateTotalScore(UUID participantId) {
        return playerAnswerRepository.findAll().stream()
                .filter(answer -> answer.getQuizParticipantId().equals(participantId))
                .mapToInt(PlayerAnswer::getPointsEarned)
                .sum();
    }

    /**
     * Convert PlayerAnswer entity to PlayerAnswerDTO using ModelMapper
     */
    private PlayerAnswerDTO convertToDTO(PlayerAnswer answer) {
        return modelMapper.map(answer, PlayerAnswerDTO.class);
    }
}
