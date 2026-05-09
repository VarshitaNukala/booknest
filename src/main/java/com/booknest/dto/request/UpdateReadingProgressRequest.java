package com.booknest.dto.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReadingProgressRequest {

    @NotBlank(message = "Book ID is required")
    private String bookId;

    @NotNull(message = "Current page is required")
    @Min(value = 0, message = "Current page cannot be negative")
    private Integer currentPage;

    private Integer totalPages;

    private String notes;

    private String readingStatus;  // CURRENTLY_READING, COMPLETED, ON_HOLD, DROPPED

    private Boolean isPublic;
}