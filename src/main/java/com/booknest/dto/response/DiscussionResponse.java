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
public class DiscussionResponse {
    private String id;
    private String bookTitle;
    private String bookAuthor;
    private String clubName;
    private String topic;
    private String description;
    private String createdByName;
    private String status;
    private int replyCount;
    private List<DiscussionReplyResponse> recentReplies;
    private LocalDateTime createdAt;
}