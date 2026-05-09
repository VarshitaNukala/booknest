package com.booknest.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Club ID is required")
    private String bookClubId;

    @NotBlank(message = "Content is required")
    private String content;

    private String imageUrl;

    private String postType;  // GENERAL, BOOK_RECOMMENDATION, BOOK_REVIEW, etc.

    private String relatedBookId;
}