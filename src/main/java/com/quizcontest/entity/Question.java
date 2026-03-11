package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_questions_order_index", columnList = "quiz_id,order_index")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID quizId;
    
    @Column(nullable = false, length = 4000)
    private String questionText;
    
    @Column(nullable = false, length = 50)
    private String questionType;  // yes_no, multiple_choice, number, text
    
    @Column(nullable = false)
    private Integer points;
    
    @Column(nullable = false)
    private Integer orderIndex;
    
    @Column(nullable = false)
    private Boolean isTrivia;
    
    @Column
    private Integer triviaTimeSeconds;
    
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
        if (this.isTrivia == null) {
            this.isTrivia = false;
        }
        if (this.points == null) {
            this.points = 1;
        }
    }
}