package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_answers", indexes = {
        @Index(name = "idx_player_answers_quiz_participant_id", columnList = "quiz_participant_id"),
        @Index(name = "idx_player_answers_question_id", columnList = "question_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_player_answers_unique", columnNames = {"quiz_participant_id", "question_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerAnswer {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID quizParticipantId;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID questionId;
    
    @Column(length = 1000)
    private String answerValue;
    
    @Column
    private Boolean isCorrect;
    
    @Column(nullable = false)
    private Integer pointsEarned;
    
    @Column(nullable = false)
    private LocalDateTime answeredAt;
    
    @Column
    private Integer timeTakenSeconds;
    
    @Column(nullable = false)
    private Integer version;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.version == null) {
            this.version = 1;
        }
        if (this.pointsEarned == null) {
            this.pointsEarned = 0;
        }
        if (this.answeredAt == null) {
            this.answeredAt = LocalDateTime.now();
        }
    }
}