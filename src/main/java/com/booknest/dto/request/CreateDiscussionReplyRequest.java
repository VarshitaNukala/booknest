package com.booknest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDiscussionReplyRequest {

    @NotBlank(message = "Discussion ID is required")
    private String discussionId;

    @NotBlank(message = "Reply content is required")
    private String content;
}