package com.booknest.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPostResponse {
    private String id;
    private String clubName;
    private String authorName;
    private String authorId;
    private String content;
    private String imageUrl;
    private String postType;
    private String relatedBookTitle;
    private boolean isPinned;
    private int commentCount;
    private List<CommentResponse> recentComments;
    private LocalDateTime createdAt;
}