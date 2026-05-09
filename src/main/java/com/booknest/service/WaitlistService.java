package com.booknest.service;

import com.booknest.dto.response.WaitlistResponse;
import com.booknest.entity.Book;
import com.booknest.entity.BookWaitlist;
import com.booknest.entity.User;
import com.booknest.enums.BookStatus;
import com.booknest.exception.BusinessRuleException;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.repository.BookRepository;
import com.booknest.repository.BookWaitlistRepository;
import com.booknest.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final BookWaitlistRepository waitlistRepository;
    private final BookRepository bookRepository;
    private final NotificationService notificationService;

    @Transactional
    public WaitlistResponse joinWaitlist(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Can't waitlist if book is available (just borrow it!)
        if (book.getStatus() == BookStatus.AVAILABLE) {
            throw new BusinessRuleException("This book is currently available. You can borrow it directly!");
        }

        // Can't waitlist your own book
        if (book.getOwner().getId().equals(user.getId())) {
            throw new BusinessRuleException("You cannot waitlist your own book");
        }

        // Check if already in waitlist
        if (waitlistRepository.existsByBookAndUserAndStatus(book, user, BookWaitlist.WaitlistStatus.WAITING)) {
            throw new BusinessRuleException("You are already in the waitlist for this book");
        }

        // Get next queue position
        Integer maxPosition = waitlistRepository.findMaxQueuePosition(book);
        int nextPosition = (maxPosition != null ? maxPosition : 0) + 1;

        BookWaitlist waitlist = BookWaitlist.builder()
                .book(book)
                .user(user)
                .queuePosition(nextPosition)
                .build();

        waitlist = waitlistRepository.save(waitlist);

        log.info("User {} joined waitlist for book '{}' at position {}",
                user.getFullName(), book.getTitle(), nextPosition);

        return mapToResponse(waitlist);
    }

    @Transactional(readOnly = true)
    public List<WaitlistResponse> getBookWaitlist(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        return waitlistRepository
                .findByBookAndStatusOrderByQueuePositionAsc(book, BookWaitlist.WaitlistStatus.WAITING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WaitlistResponse getMyPosition(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        BookWaitlist waitlist = waitlistRepository.findByBookAndUser(book, user)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist", "bookId", bookId));

        return mapToResponse(waitlist);
    }

    @Transactional
    public void leaveWaitlist(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        BookWaitlist waitlist = waitlistRepository.findByBookAndUser(book, user)
                .orElseThrow(() -> new BusinessRuleException("You are not in the waitlist for this book"));

        waitlist.setStatus(BookWaitlist.WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);

        log.info("User {} left waitlist for book '{}'", user.getFullName(), book.getTitle());
    }

    @Transactional
    public void notifyNextPerson(Book book) {
        List<BookWaitlist> nextInQueue = waitlistRepository.findNextInQueue(book);

        if (!nextInQueue.isEmpty()) {
            BookWaitlist next = nextInQueue.get(0);
            next.setStatus(BookWaitlist.WaitlistStatus.NOTIFIED);
            next.setNotifiedAt(java.time.LocalDateTime.now());
            waitlistRepository.save(next);

            log.info("Notified user {} that book '{}' is now available",
                    next.getUser().getFullName(), book.getTitle());

            notificationService.sendBookAvailable(
                    next.getUser().getEmail(),
                    book.getTitle(),
                    book.getOwner().getFullName()
            );
        }
    }

    private WaitlistResponse mapToResponse(BookWaitlist waitlist) {
        long totalWaiting = waitlistRepository.countByBookAndStatus(
                waitlist.getBook(), BookWaitlist.WaitlistStatus.WAITING);

        return WaitlistResponse.builder()
                .id(waitlist.getId())
                .bookTitle(waitlist.getBook().getTitle())
                .userName(waitlist.getUser().getFullName())
                .queuePosition(waitlist.getQueuePosition())
                .totalInQueue((int) totalWaiting)
                .status(waitlist.getStatus().name())
                .joinedAt(waitlist.getJoinedAt())
                .build();
    }
}