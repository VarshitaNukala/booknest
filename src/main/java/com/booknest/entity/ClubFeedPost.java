package com.booknest.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "club_feed_posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubFeedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_club_id", nullable = false)
    private BookClub bookClub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 2000)
    private String content;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostType postType = PostType.GENERAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_book_id")
    private Book relatedBook;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPinned = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PostComment> comments = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PostType {
        GENERAL,
        BOOK_RECOMMENDATION,
        BOOK_REVIEW,
        DISCUSSION_TOPIC,
        MEETUP_ANNOUNCEMENT,
        BOOK_SWAP
    }

}