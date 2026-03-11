package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "correct_answers", indexes = {
        @Index(name = "idx_correct_answers_question_id", columnList = "question_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorrectAnswer {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID questionId;
    
    @Column(nullable = false, length = 50)
    private String answerType;  // yes_no, multiple_choice, number, text
    
    @Column(length = 500)
    private String answerValue;
    
    @Column(nullable = false)
    private Boolean isCaseSensitive;
    
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
        if (this.isCaseSensitive == null) {
            this.isCaseSensitive = false;
        }
    }
}