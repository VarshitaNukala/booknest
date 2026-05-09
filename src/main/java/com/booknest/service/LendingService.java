package com.booknest.service;


import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.dto.request.LendBookRequest;
import com.booknest.dto.request.ExtendBorrowRequest;
import com.booknest.dto.response.BookResponse;
import com.booknest.dto.response.LendingResponse;
import com.booknest.entity.*;
import com.booknest.enums.BookStatus;
import com.booknest.enums.MembershipStatus;
import com.booknest.exception.BusinessRuleException;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.exception.UnauthorizedException;
import com.booknest.repository.BookRepository;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.LendingTransactionRepository;
import com.booknest.repository.MembershipRepository;
import com.booknest.service.strategy.lending.LendingStrategy;
import com.booknest.service.strategy.lending.LendingStrategyFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LendingService {

    private final BookRepository bookRepository;
    private final BookClubRepository bookClubRepository;
    private final LendingTransactionRepository transactionRepository;
    private final MembershipRepository membershipRepository;
    private final LendingStrategyFactory strategyFactory;
    private final EntityManager entityManager;

    @Transactional
    @CacheEvict(value = "books", key = "#request.bookClubId")
    public BookResponse lendBook(LendBookRequest request, User owner) {
        BookClub club = bookClubRepository.findById(request.getBookClubId())
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", request.getBookClubId()));

        // Verify user is an approved member of the club
        membershipRepository.findByUserAndBookClub(owner, club)
                .filter(m -> m.getStatus() == MembershipStatus.APPROVED)
                .orElseThrow(() -> new BusinessRuleException("You must be an approved member to lend books"));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .owner(owner)
                .bookClub(club)
                .lendingPolicy(request.getLendingPolicy())
                .depositAmount(request.getDepositAmount())
                .maxBorrowDays(request.getMaxBorrowDays())
                .condition(request.getCondition())
                .build();

        book = bookRepository.save(book);
        return mapToBookResponse(book);
    }

    @Transactional
    public LendingResponse borrowBook(BorrowBookRequest request, User borrower) {
        try {
            Book book = bookRepository.findById(request.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

            LendingStrategy strategy = strategyFactory.getStrategy(book.getLendingPolicy());

            if (!strategy.canBorrow(book, borrower)) {
                throw new BusinessRuleException("This book cannot be borrowed at this time");
            }

            LendingTransaction transaction = strategy.processBorrow(book, borrower, request);
            return mapToLendingResponse(transaction);
        } catch (Exception e) {
            e.printStackTrace();  // ← THIS PRINTS THE REAL ERROR
            throw e;
        }
    }

    @Transactional
    public LendingResponse returnBook(String transactionId, User user) {
        try {
            LendingTransaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("LendingTransaction", "id", transactionId));

            if (!transaction.getBorrower().getId().equals(user.getId()) &&
                    !transaction.getLender().getId().equals(user.getId())) {
                throw new BusinessRuleException("Only the borrower or lender can mark this book as returned");
            }

            var strategy = strategyFactory.getStrategy(transaction.getBook().getLendingPolicy());
            strategy.processReturn(transaction);

            return mapToLendingResponse(transaction);
        } catch (Exception e) {
            e.printStackTrace();  // ← THIS WILL SHOW THE REAL ERROR
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getClubBooks(String clubId) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        List<Book> books = bookRepository.findByBookClub(club);
        System.out.println("BOOKS FOUND: " + books.size());

        return books.stream()
                .map(book -> {
                    System.out.println("MAPPING: " + book.getTitle());
                    return mapToBookResponse(book);
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<LendingResponse> getUserBorrowedBooks(User user) {
        return transactionRepository.findByBorrowerAndTransactionStatus(user, BookStatus.BORROWED)
                .stream()
                .map(this::mapToLendingResponse)
                .collect(Collectors.toList());
    }

    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .ownerName(book.getOwner().getFullName())
                .bookClubName(book.getBookClub().getName())
                .status(book.getStatus())
                .lendingPolicy(book.getLendingPolicy())
                .depositAmount(book.getDepositAmount())
                .maxBorrowDays(book.getMaxBorrowDays())
                .condition(book.getCondition())
                .createdAt(book.getCreatedAt())
                .build();
    }

    private LendingResponse mapToLendingResponse(LendingTransaction transaction) {
        return LendingResponse.builder()
                .transactionId(transaction.getId())
                .bookTitle(transaction.getBook().getTitle())
                .lenderName(transaction.getLender().getFullName())
                .borrowerName(transaction.getBorrower().getFullName())
                .borrowDate(transaction.getBorrowDate())
                .dueDate(transaction.getDueDate())
                .returnDate(transaction.getReturnDate())
                .depositPaid(transaction.getDepositPaid())
                .transactionStatus(transaction.getTransactionStatus())
                .extensionCount(transaction.getExtensionCount())
                //.bookClubId(transaction.getBook().getBookClub().getId())
                .build();
    }
    @Transactional
    public LendingResponse extendBorrow(ExtendBorrowRequest request, User borrower) {
        LendingTransaction transaction = transactionRepository
                .findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LendingTransaction", "id", request.getTransactionId()));

        // Only the borrower can request an extension
        if (!transaction.getBorrower().getId().equals(borrower.getId())) {
            throw new BusinessRuleException("Only the borrower can request an extension");
        }

        // Can only extend active borrows
        if (transaction.getTransactionStatus() != BookStatus.BORROWED) {
            throw new BusinessRuleException(
                    "Can only extend active borrows. Current status: " + transaction.getTransactionStatus()
            );
        }

        // Use Strategy Pattern to handle extension (each policy has its own rules)
        var strategy = strategyFactory.getStrategy(transaction.getBook().getLendingPolicy());
        strategy.processExtension(transaction, request.getAdditionalDays());

        return mapToLendingResponse(transaction);
    }

    /**
     * Get lending history for a specific book
     */
    @Transactional(readOnly = true)
    public List<LendingResponse> getBookLendingHistory(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        return transactionRepository.findAll()
                .stream()
                .filter(t -> t.getBook().getId().equals(bookId))
                .map(this::mapToLendingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all lending transactions for a club (admin only)
     */
    @Transactional(readOnly = true)
    public List<LendingResponse> getClubLendingHistory(String clubId, User admin) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        if (!club.getCreatedBy().getId().equals(admin.getId())) {
            throw new UnauthorizedException("Only the club admin can view lending history");
        }

        return bookRepository.findByBookClub(club)
                .stream()
                .flatMap(book -> transactionRepository.findAll()
                        .stream()
                        .filter(t -> t.getBook().getId().equals(book.getId())))
                .map(this::mapToLendingResponse)
                .collect(Collectors.toList());
    }
}