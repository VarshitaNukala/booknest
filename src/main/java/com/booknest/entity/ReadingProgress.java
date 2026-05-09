package com.booknest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reading_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer currentPage;

    @Column(nullable = false)
    private Integer totalPages;

    @Column(nullable = false)
    @Builder.Default
    private Double percentageComplete = 0.0;

    @Column(length = 500)
    private String notes;  // Reader's personal notes

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReadingStatus readingStatus = ReadingStatus.CURRENTLY_READING;

    @CreationTimestamp
    private LocalDate startedAt;

    @UpdateTimestamp
    private LocalDate completedAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = true;  // Visible to club members or not

    // Enum defined inline or in enums package
    public enum ReadingStatus {
        WANT_TO_READ,
        CURRENTLY_READING,
        COMPLETED,
        ON_HOLD,
        DROPPED
    }

    @PrePersist
    protected void onCreate() {
        if (startedAt == null && readingStatus == ReadingStatus.CURRENTLY_READING) {
            startedAt = LocalDate.now();
        }
        calculatePercentage();
    }

    @PreUpdate
    protected void onUpdate() {
        if (readingStatus == ReadingStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDate.now();
            currentPage = totalPages;
        }
        calculatePercentage();
    }

    private void calculatePercentage() {
        if (totalPages != null && totalPages > 0 && currentPage != null) {
            double raw = (currentPage.doubleValue() / totalPages.doubleValue()) * 100.0;
            this.percentageComplete = Math.round(raw * 100.0) / 100.0;
        }
    }
}