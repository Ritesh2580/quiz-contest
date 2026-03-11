package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_participants", indexes = {
        @Index(name = "idx_quiz_participants_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_quiz_participants_player_id", columnList = "player_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_quiz_participants_unique", columnNames = {"quiz_id", "player_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizParticipant {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID quizId;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID playerId;
    
    @Column(nullable = false)
    private LocalDateTime joinedAt;
    
    @Column
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column(nullable = false)
    private Integer totalScore;
    
    @Column(nullable = false, length = 50)
    private String status;  // joined, in_progress, completed, abandoned
    
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
        if (this.totalScore == null) {
            this.totalScore = 0;
        }
        if (this.status == null) {
            this.status = "joined";
        }
        if (this.joinedAt == null) {
            this.joinedAt = LocalDateTime.now();
        }
    }
}