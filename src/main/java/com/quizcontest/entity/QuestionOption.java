package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "question_options", indexes = {
        @Index(name = "idx_question_options_question_id", columnList = "question_id"),
        @Index(name = "idx_question_options_image_id", columnList = "option_image_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_question_options_unique", columnNames = {"question_id", "option_index"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOption {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID questionId;
    
    @Column(nullable = false, length = 500)
    private String optionText;
    
    @Column(columnDefinition = "UUID")
    private UUID optionImageId;
    
    @Column(nullable = false)
    private Short optionIndex;
    
    @Column(nullable = false)
    private Boolean isCorrect;
    
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
        if (this.isCorrect == null) {
            this.isCorrect = false;
        }
    }
}