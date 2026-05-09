package com.booknest.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Post ID is required")
    private String postId;

    @NotBlank(message = "Comment content is required")
    private String content;

    private String parentCommentId;  // Optional, for nested replies
}