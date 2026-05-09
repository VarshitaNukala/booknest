package com.booknest;


import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.entity.Book;
import com.booknest.entity.LendingTransaction;
import com.booknest.entity.User;
import com.booknest.enums.BookStatus;
import com.booknest.enums.LendingPolicyType;
import com.booknest.enums.Role;
import com.booknest.exception.BusinessRuleException;
import com.booknest.repository.LendingTransactionRepository;
import com.booknest.service.WaitlistService;
import com.booknest.service.strategy.lending.FreeLendingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreeLendingStrategyTest {

    @Mock
    private LendingTransactionRepository transactionRepository;
    private WaitlistService waitlistService;

    private FreeLendingStrategy freeLendingStrategy;

    private User owner;
    private User borrower;
    private Book book;
    private BorrowBookRequest request;

    @BeforeEach
    void setUp() {
        freeLendingStrategy = new FreeLendingStrategy(transactionRepository,waitlistService);

        owner = User.builder()
                .id("owner-1")
                .fullName("Book Owner")
                .email("owner@test.com")
                .role(Role.ROLE_USER)
                .build();

        borrower = User.builder()
                .id("borrower-1")
                .fullName("Book Borrower")
                .email("borrower@test.com")
                .role(Role.ROLE_USER)
                .build();

        book = Book.builder()
                .id("book-1")
                .title("Test Book")
                .author("Test Author")
                .owner(owner)
                .status(BookStatus.AVAILABLE)
                .lendingPolicy(LendingPolicyType.FREE)
                .maxBorrowDays(14)
                .build();

        request = new BorrowBookRequest();
        request.setBookId("book-1");
        request.setMessageToLender("I'd love to read this!");
    }

    @Test
    void shouldProcessBorrowSuccessfully() {
        when(transactionRepository.save(any(LendingTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LendingTransaction transaction = freeLendingStrategy.processBorrow(book, borrower, request);

        assertNotNull(transaction);
        assertEquals(borrower, transaction.getBorrower());
        assertEquals(owner, transaction.getLender());
        assertEquals(BookStatus.BORROWED, transaction.getTransactionStatus());
        assertEquals(BigDecimal.ZERO, transaction.getDepositPaid());
        assertEquals(BookStatus.BORROWED, book.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenBorrowingOwnBook() {
        assertThrows(BusinessRuleException.class, () -> {
            freeLendingStrategy.processBorrow(book, owner, request);
        });
    }

    @Test
    void shouldProcessReturnSuccessfully() {
        LendingTransaction transaction = LendingTransaction.builder()
                .book(book)
                .lender(owner)
                .borrower(borrower)
                .transactionStatus(BookStatus.BORROWED)
                .borrowDate(java.time.LocalDate.now().minusDays(7))
                .dueDate(java.time.LocalDate.now().plusDays(7))
                .build();

        freeLendingStrategy.processReturn(transaction);

        assertEquals(BookStatus.AVAILABLE, transaction.getBook().getStatus());
        assertNotNull(transaction.getReturnDate());
        assertEquals(BigDecimal.ZERO, transaction.getLateFee());
    }

    @Test
    void shouldProcessExtensionSuccessfully() {
        LendingTransaction transaction = LendingTransaction.builder()
                .book(book)
                .lender(owner)
                .borrower(borrower)
                .transactionStatus(BookStatus.BORROWED)
                .borrowDate(java.time.LocalDate.now().minusDays(7))
                .originalDueDate(java.time.LocalDate.now().plusDays(7))
                .dueDate(java.time.LocalDate.now().plusDays(7))
                .extensionCount(0)
                .build();

        freeLendingStrategy.processExtension(transaction, 5);

        assertEquals(1, transaction.getExtensionCount());
    }

    @Test
    void shouldThrowExceptionWhenMaxExtensionsReached() {
        LendingTransaction transaction = LendingTransaction.builder()
                .book(book)
                .lender(owner)
                .borrower(borrower)
                .transactionStatus(BookStatus.BORROWED)
                .borrowDate(java.time.LocalDate.now().minusDays(7))
                .originalDueDate(java.time.LocalDate.now().plusDays(7))
                .dueDate(java.time.LocalDate.now().plusDays(7))
                .extensionCount(3)
                .build();

        assertThrows(BusinessRuleException.class, () -> {
            freeLendingStrategy.processExtension(transaction, 5);
        });
    }

    @Test
    void shouldReturnTrueWhenCanBorrow() {
        when(transactionRepository.findByBorrowerAndTransactionStatus(borrower, BookStatus.BORROWED))
                .thenReturn(Collections.emptyList());

        boolean result = freeLendingStrategy.canBorrow(book, borrower);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenBookNotAvailable() {
        book.setStatus(BookStatus.BORROWED);

        boolean result = freeLendingStrategy.canBorrow(book, borrower);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenUserIsOwner() {
        boolean result = freeLendingStrategy.canBorrow(book, owner);

        assertFalse(result);
    }
}