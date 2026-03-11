package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_leaderboard", indexes = {
        @Index(name = "idx_quiz_leaderboard_quiz_id", columnList = "quiz_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_quiz_leaderboard_unique", columnNames = {"quiz_id", "player_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizLeaderboard {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID quizId;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID playerId;
    
    @Column
    private Integer rank;
    
    @Column
    private Integer totalScore;
    
    @Column
    private Integer totalQuestionsCorrect;
    
    @Column
    private Integer totalQuestionsAttempted;
    
    @Column
    private Integer completionTimeMinutes;
    
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
    }
}