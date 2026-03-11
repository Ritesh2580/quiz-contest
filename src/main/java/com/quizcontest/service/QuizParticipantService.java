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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for QuizParticipant entity
 * Handles quiz participation and tracking
 * Implements IQuizParticipantService interface for Dependency Inversion Principle
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QuizParticipantService implements IQuizParticipantService {

    private final QuizParticipantRepository quizParticipantRepository;
    private final ModelMapper modelMapper;

    /**
     * Join a quiz (create participant record)
     */
    @Override
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
     * Get participant by ID
     */
    @Override
    @Transactional(readOnly = true)
    public QuizParticipantDTO getParticipantById(UUID id) {
        QuizParticipant participant = quizParticipantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz participant not found with ID: " + id));
        return convertToDTO(participant);
    }

    /**
     * Get participants by quiz ID
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizParticipantDTO> getParticipantsByQuizId(UUID quizId) {
        return quizParticipantRepository.findAll().stream()
                .filter(p -> p.getQuizId().equals(quizId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get quizzes participated by a user
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizParticipantDTO> getParticipationsByUserId(UUID userId) {
        return quizParticipantRepository.findAll().stream()
                .filter(p -> p.getPlayerId().equals(userId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Start quiz (change status to in_progress)
     */
    @Override
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
     * Complete quiz (change status to completed)
     */
    @Override
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
     * Convert QuizParticipant entity to QuizParticipantDTO using ModelMapper
     */
    private QuizParticipantDTO convertToDTO(QuizParticipant participant) {
        return modelMapper.map(participant, QuizParticipantDTO.class);
    }
}
