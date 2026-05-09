package com.booknest.service.strategy.lending;


import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.entity.Book;
import com.booknest.entity.LendingTransaction;
import com.booknest.entity.User;
import com.booknest.enums.BookStatus;
import com.booknest.enums.LendingPolicyType;
import com.booknest.enums.MembershipStatus;
import com.booknest.exception.BusinessRuleException;
import com.booknest.repository.LendingTransactionRepository;
import com.booknest.repository.MembershipRepository;
import com.booknest.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component("subscriptionBasedLendingStrategy")
@RequiredArgsConstructor
public class SubscriptionBasedLendingStrategy implements LendingStrategy {

    private final LendingTransactionRepository transactionRepository;
    private final MembershipRepository membershipRepository;
    private final WaitlistService waitlistService;

    @Override
    public LendingTransaction processBorrow(Book book, User borrower, BorrowBookRequest request) {
        if (book.getOwner().getId().equals(borrower.getId())) {
            throw new BusinessRuleException("You cannot borrow your own book");
        }

        // Verify subscriber has an active membership in this club
        boolean isActiveMember = membershipRepository
                .findByUserAndBookClub(borrower, book.getBookClub())
                .map(m -> m.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);

        if (!isActiveMember) {
            throw new BusinessRuleException("Active subscription required to borrow this book");
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

        if (transaction.getDueDate().isBefore(LocalDate.now())) {
            transaction.setTransactionStatus(BookStatus.OVERDUE);
            long daysLate = LocalDate.now().toEpochDay() - transaction.getDueDate().toEpochDay();
            BigDecimal lateFee = BigDecimal.valueOf(daysLate * 1.5); // $1.50 per day for subscribers
            transaction.setLateFee(lateFee);
        }

        transaction.getBook().setStatus(BookStatus.AVAILABLE);

        waitlistService.notifyNextPerson(transaction.getBook());
        transactionRepository.save(transaction);
    }

    @Override
    public void processExtension(LendingTransaction transaction, int additionalDays) {
        if (transaction.getExtensionCount() >= 5) {
            throw new BusinessRuleException("Maximum extension limit reached (5) for subscribers");
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
        return LendingPolicyType.SUBSCRIPTION_BASED;
    }
}