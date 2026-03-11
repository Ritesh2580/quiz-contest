package com.quizcontest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quizzes", indexes = {
        @Index(name = "idx_quizzes_creator_id", columnList = "creator_id"),
        @Index(name = "idx_quizzes_start_datetime", columnList = "start_datetime"),
        @Index(name = "idx_quizzes_end_datetime", columnList = "end_datetime"),
        @Index(name = "idx_quizzes_banner_image_id", columnList = "banner_image_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(nullable = false, columnDefinition = "UUID")
    private UUID creatorId;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(length = 4000)
    private String description;
    
    @Column(columnDefinition = "UUID")
    private UUID bannerImageId;
    
    @Column(nullable = false)
    private LocalDateTime startDatetime;
    
    @Column(nullable = false)
    private LocalDateTime endDatetime;
    
    @Column
    private Integer durationMinutes;
    
    @Column(nullable = false)
    private Boolean isPublished;
    
    @Column(nullable = false)
    private Integer totalPoints;
    
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
        if (this.isPublished == null) {
            this.isPublished = false;
        }
        if (this.totalPoints == null) {
            this.totalPoints = 0;
        }
    }
}