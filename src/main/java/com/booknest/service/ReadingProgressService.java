package com.booknest.service;


import com.booknest.dto.request.UpdateReadingProgressRequest;
import com.booknest.dto.response.ReadingProgressResponse;
import com.booknest.entity.Book;
import com.booknest.entity.BookClub;
import com.booknest.entity.ReadingProgress;
import com.booknest.entity.User;
import com.booknest.enums.MembershipStatus;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.exception.UnauthorizedException;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.BookRepository;
import com.booknest.repository.MembershipRepository;
import com.booknest.repository.ReadingProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {

    private final ReadingProgressRepository progressRepository;
    private final BookRepository bookRepository;
    private final BookClubRepository bookClubRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public ReadingProgressResponse updateProgress(UpdateReadingProgressRequest request, User user) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        ReadingProgress progress = progressRepository
                .findByUserAndBook(user, book)
                .orElse(ReadingProgress.builder()
                        .user(user)
                        .book(book)
                        .totalPages(request.getTotalPages() != null ? request.getTotalPages() : 0)
                        .build());

        progress.setCurrentPage(request.getCurrentPage());

        if (request.getTotalPages() != null) {
            progress.setTotalPages(request.getTotalPages());
        }

        if (request.getNotes() != null) {
            progress.setNotes(request.getNotes());
        }

        if (request.getReadingStatus() != null) {
            progress.setReadingStatus(
                    ReadingProgress.ReadingStatus.valueOf(request.getReadingStatus().toUpperCase())
            );
        }

        if (request.getIsPublic() != null) {
            progress.setPublic(request.getIsPublic());
        }

        progress = progressRepository.save(progress);
        return mapToResponse(progress);
    }

    @Transactional(readOnly = true)
    public ReadingProgressResponse getMyProgress(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        ReadingProgress progress = progressRepository
                .findByUserAndBook(user, book)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReadingProgress", "bookId", bookId));

        return mapToResponse(progress);
    }

    @Transactional(readOnly = true)
    public List<ReadingProgressResponse> getMyReadingList(User user) {
        return progressRepository.findByUser(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get public reading progress of all members in a club.
     * Only accessible by approved club members.
     */
    @Transactional(readOnly = true)
    public List<ReadingProgressResponse> getClubReadingProgress(String clubId, User user) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        // Verify user is a club member
        boolean isMember = membershipRepository
                .findByUserAndBookClub(user, club)
                .map(m -> m.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);

        if (!isMember) {
            throw new UnauthorizedException("Only club members can view reading progress");
        }

        return progressRepository.findPublicProgressByClub(club)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reading stats for a book within a club
     */
    @Transactional(readOnly = true)
    public List<ReadingProgressResponse> getBookReaders(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Verify user is a club member
        boolean isMember = membershipRepository
                .findByUserAndBookClub(user, book.getBookClub())
                .map(m -> m.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);

        if (!isMember) {
            throw new UnauthorizedException("Only club members can view readers");
        }

        return progressRepository.findPublicProgressByBook(bookId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgress(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        ReadingProgress progress = progressRepository
                .findByUserAndBook(user, book)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ReadingProgress", "bookId", bookId));

        progressRepository.delete(progress);
    }

    private ReadingProgressResponse mapToResponse(ReadingProgress progress) {
        return ReadingProgressResponse.builder()
                .id(progress.getId())
                .userName(progress.getUser().getFullName())
                .bookTitle(progress.getBook().getTitle())
                .bookAuthor(progress.getBook().getAuthor())
                .bookClubName(progress.getBook().getBookClub().getName())
                .currentPage(progress.getCurrentPage())
                .totalPages(progress.getTotalPages())
                .percentageComplete(progress.getPercentageComplete())
                .notes(progress.getNotes())
                .readingStatus(progress.getReadingStatus().name())
                .startedAt(progress.getStartedAt())
                .completedAt(progress.getCompletedAt())
                .isPublic(progress.isPublic())
                .updatedAt(progress.getUpdatedAt())
                .build();
    }
}