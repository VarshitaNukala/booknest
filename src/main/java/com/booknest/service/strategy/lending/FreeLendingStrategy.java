package com.booknest.service.strategy.lending;



import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.entity.Book;
import com.booknest.entity.LendingTransaction;
import com.booknest.entity.User;
import com.booknest.enums.BookStatus;
import com.booknest.enums.LendingPolicyType;
import com.booknest.exception.BusinessRuleException;
import com.booknest.repository.LendingTransactionRepository;
import com.booknest.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component("freeLendingStrategy")
@RequiredArgsConstructor
public class FreeLendingStrategy implements LendingStrategy {

    private final LendingTransactionRepository transactionRepository;
    private final WaitlistService waitlistService;

    @Override
    public LendingTransaction processBorrow(Book book, User borrower, BorrowBookRequest request) {
        if (book.getOwner().getId().equals(borrower.getId())) {
            throw new BusinessRuleException("You cannot borrow your own book");
        }

        LocalDate now = LocalDate.now();

        LendingTransaction transaction = LendingTransaction.builder()
                .book(book)
                .lender(book.getOwner())
                .borrower(borrower)
                .borrowDate(now)
                .dueDate(now.plusDays(book.getMaxBorrowDays()))
                .originalDueDate(now.plusDays(book.getMaxBorrowDays()))
                .depositPaid(BigDecimal.ZERO)
                .transactionStatus(BookStatus.BORROWED)
                .build();

        book.setStatus(BookStatus.BORROWED);
        return transactionRepository.save(transaction);
    }

    @Override
    public void processReturn(LendingTransaction transaction) {
        transaction.setReturnDate(LocalDate.now());
        transaction.getBook().setStatus(BookStatus.AVAILABLE);

        if (transaction.getDueDate().isBefore(LocalDate.now())) {
            transaction.setTransactionStatus(BookStatus.OVERDUE);
        } else {
            transaction.setTransactionStatus(BookStatus.AVAILABLE);
        }

        waitlistService.notifyNextPerson(transaction.getBook());
        transactionRepository.save(transaction);
    }

    @Override
    public void processExtension(LendingTransaction transaction, int additionalDays) {
        if (transaction.getExtensionCount() >= 3) {
            throw new BusinessRuleException("Maximum extension limit reached (3)");
        }

        transaction.setDueDate(transaction.getDueDate().plusDays(additionalDays));
        transaction.setExtensionCount(transaction.getExtensionCount() + 1);
        transactionRepository.save(transaction);
    }

    @Override
    public boolean canBorrow(Book book, User user) {
        return book.getStatus() == BookStatus.AVAILABLE
                && !book.getOwner().getId().equals(user.getId());
    }

    @Override
    public LendingPolicyType getStrategyType() {
        return LendingPolicyType.FREE;
    }
}