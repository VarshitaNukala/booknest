package com.booknest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingProgressResponse {
    private String id;
    private String userName;
    private String bookTitle;
    private String bookAuthor;
    private String bookClubName;
    private Integer currentPage;
    private Integer totalPages;
    private Double percentageComplete;
    private String notes;
    private String readingStatus;
    private LocalDate startedAt;
    private LocalDate completedAt;
    private boolean isPublic;
    private LocalDateTime updatedAt;
}