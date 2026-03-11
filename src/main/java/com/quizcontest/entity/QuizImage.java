package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_images", indexes = {
        @Index(name = "idx_quiz_images_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_quiz_images_question_id", columnList = "linked_question_id"),
        @Index(name = "idx_quiz_images_option_id", columnList = "linked_option_id"),
        @Index(name = "idx_quiz_images_type", columnList = "image_type"),
        @Index(name = "idx_quiz_images_index", columnList = "linked_question_id,image_index")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizImage {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID quizId;
    
    @Column(nullable = false, columnDefinition = "BLOB")
    @Lob
    private byte[] imageData;
    
    @Column(nullable = false, length = 50)
    private String imageMimeType;
    
    @Column(length = 255)
    private String imageName;
    
    @Column(length = 500)
    private String imageAltText;
    
    @Column
    private Integer imageSizeBytes;
    
    @Column
    private Integer imageWidth;
    
    @Column
    private Integer imageHeight;
    
    @Column
    private Short imageIndex;
    
    @Column(nullable = false, length = 50)
    private String imageType;  // quiz_banner, question_image, option_image
    
    @Column(columnDefinition = "UUID")
    private UUID linkedQuestionId;
    
    @Column(columnDefinition = "UUID")
    private UUID linkedOptionId;
    
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
        if (this.imageIndex == null) {
            this.imageIndex = 0;
        }
    }
}