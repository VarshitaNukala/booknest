package com.booknest.entity;


import com.booknest.enums.BookStatus;
import com.booknest.enums.LendingPolicyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String isbn;

    @Column(length = 2000)
    private String description;

    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_club_id", nullable = false)
    private BookClub bookClub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LendingPolicyType lendingPolicy;

    private BigDecimal depositAmount;

    private Integer maxBorrowDays;

    private String condition;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = BookStatus.AVAILABLE;
        if (lendingPolicy == null) lendingPolicy = LendingPolicyType.FREE;
        if (maxBorrowDays == null) maxBorrowDays = 14;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}