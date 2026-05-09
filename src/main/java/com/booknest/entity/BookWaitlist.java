package com.booknest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_waitlist",
        uniqueConstraints = @UniqueConstraint(columnNames = {"book_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookWaitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer queuePosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime joinedAt;

    private LocalDateTime notifiedAt;

    public enum WaitlistStatus {
        WAITING,
        NOTIFIED,
        ACCEPTED,
        EXPIRED,
        CANCELLED
    }

}