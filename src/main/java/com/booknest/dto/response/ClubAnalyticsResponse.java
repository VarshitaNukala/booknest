package com.booknest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubAnalyticsResponse {
    private String clubName;
    private int totalMembers;
    private int totalBooks;
    private int booksAvailable;
    private int booksBorrowed;
    private int booksOverdue;
    private double avgReadingPercentage;
    private List<PopularBook> mostBorrowedBooks;
    private List<ActiveMember> mostActiveMembers;
    private List<CategoryBreakdown> booksByStatus;

    @Data
    @AllArgsConstructor
    public static class PopularBook {
        private String title;
        private String author;
        private long borrowCount;
    }

    @Data
    @AllArgsConstructor
    public static class ActiveMember {
        private String name;
        private long booksLent;
        private long booksBorrowed;
        private int booksCurrentlyReading;
    }

    @Data
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String status;
        private long count;
    }
}