package com.booknest.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDiscussionRequest {

    @NotBlank(message = "Club ID is required")
    private String bookClubId;

    @NotBlank(message = "Book ID is required")
    private String bookId;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Description is required")
    private String description;
}