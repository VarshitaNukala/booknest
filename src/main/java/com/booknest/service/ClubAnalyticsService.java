package com.booknest.service;

import com.booknest.dto.response.ClubAnalyticsResponse;
import com.booknest.entity.BookClub;
import com.booknest.enums.MembershipStatus;
import com.booknest.enums.BookStatus;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.BookRepository;
import com.booknest.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubAnalyticsService {

    private final BookClubRepository bookClubRepository;
    private final MembershipRepository membershipRepository;
    private final BookRepository bookRepository;

    @Cacheable(value = "analytics", key = "#clubId")
    public ClubAnalyticsResponse getClubAnalytics(String clubId) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        // Total approved members
        long totalMembers = membershipRepository
                .countByBookClubAndStatus(club, MembershipStatus.APPROVED);

        // Total books in club
        var allBooks = bookRepository.findByBookClub(club);
        int totalBooks = allBooks.size();

        // Books by status
        int booksAvailable = (int) allBooks.stream()
                .filter(b -> b.getStatus() == BookStatus.AVAILABLE).count();
        int booksBorrowed = (int) allBooks.stream()
                .filter(b -> b.getStatus() == BookStatus.BORROWED).count();
        int booksOverdue = (int) allBooks.stream()
                .filter(b -> b.getStatus() == BookStatus.OVERDUE).count();

        // Average reading percentage
        Double avgReading = bookClubRepository.findAvgReadingPercentageNative(clubId);
        if (avgReading == null) avgReading = 0.0;

        // Most borrowed books
        List<ClubAnalyticsResponse.PopularBook> popularBooks =
                bookClubRepository.findMostBorrowedBooksNative(clubId)
                        .stream()
                        .map(row -> new ClubAnalyticsResponse.PopularBook(
                                (String) row[0],
                                (String) row[1],
                                ((Number) row[2]).longValue()))
                        .collect(Collectors.toList());

        // Most active members
        List<ClubAnalyticsResponse.ActiveMember> activeMembers =
                bookClubRepository.findMostActiveMembersNative(clubId)
                        .stream()
                        .map(row -> new ClubAnalyticsResponse.ActiveMember(
                                (String) row[0],
                                row[1] != null ? ((Number) row[1]).longValue() : 0,
                                row[2] != null ? ((Number) row[2]).longValue() : 0,
                                row[3] != null ? ((Number) row[3]).intValue() : 0))
                        .collect(Collectors.toList());

        // Books by status breakdown
        List<ClubAnalyticsResponse.CategoryBreakdown> statusBreakdown =
                bookClubRepository.findBookCountByStatusNative(clubId)
                        .stream()
                        .map(row -> new ClubAnalyticsResponse.CategoryBreakdown(
                                row[0].toString(),
                                ((Number) row[1]).longValue()))
                        .collect(Collectors.toList());

        return ClubAnalyticsResponse.builder()
                .clubName(club.getName())
                .totalMembers((int) totalMembers)
                .totalBooks(totalBooks)
                .booksAvailable(booksAvailable)
                .booksBorrowed(booksBorrowed)
                .booksOverdue(booksOverdue)
                .avgReadingPercentage(Math.round(avgReading * 100.0) / 100.0)
                .mostBorrowedBooks(popularBooks)
                .mostActiveMembers(activeMembers)
                .booksByStatus(statusBreakdown)
                .build();
    }
}