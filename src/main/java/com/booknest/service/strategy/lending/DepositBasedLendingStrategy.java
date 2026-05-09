package com.booknest.service.strategy.lending;

import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.dto.request.CreatePaymentRequest;
import com.booknest.dto.response.PaymentResponse;
import com.booknest.entity.Book;
import com.booknest.entity.LendingTransaction;
import com.booknest.entity.User;
import com.booknest.enums.BookStatus;
import com.booknest.enums.LendingPolicyType;
import com.booknest.exception.BusinessRuleException;
import com.booknest.repository.LendingTransactionRepository;
import com.booknest.service.StripePaymentService;
import com.booknest.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component("depositBasedLendingStrategy")
@RequiredArgsConstructor
@Slf4j
public class DepositBasedLendingStrategy implements LendingStrategy {

    private final LendingTransactionRepository transactionRepository;
    private final StripePaymentService stripePaymentService;
    private final WaitlistService waitlistService;

    @Override
    public LendingTransaction processBorrow(Book book, User borrower, BorrowBookRequest request) {
        if (book.getOwner().getId().equals(borrower.getId())) {
            throw new BusinessRuleException("You cannot borrow your own book");
        }

        if (book.getDepositAmount() == null || book.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("This book requires a deposit amount to be set");
        }

        // 1. Create and save the transaction FIRST
        LocalDate now = LocalDate.now();

        LendingTransaction transaction = LendingTransaction.builder()
                .book(book)
                .lender(book.getOwner())
                .borrower(borrower)
                .borrowDate(now)
                .dueDate(now.plusDays(book.getMaxBorrowDays()))
                .originalDueDate(now.plusDays(book.getMaxBorrowDays()))
                .depositPaid(book.getDepositAmount())
                .transactionStatus(BookStatus.AWAITING_PAYMENT)
                .build();

        transaction = transactionRepository.save(transaction);
        book.setStatus(BookStatus.AWAITING_PAYMENT);

        // 2. NOW create Stripe PaymentIntent with the REAL transaction ID
        log.info("Creating Stripe payment for deposit: ₹{} for book: {}",
                book.getDepositAmount(), book.getTitle());

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setAmount(book.getDepositAmount());
        paymentRequest.setPaymentType("DEPOSIT");
        paymentRequest.setTransactionId(transaction.getId());  // ← REAL transaction ID

        PaymentResponse payment = stripePaymentService.createPaymentIntent(paymentRequest);

        if ("failed".equals(payment.getStatus())) {
            // Rollback: revert book status and delete transaction
            book.setStatus(BookStatus.AVAILABLE);
            transactionRepository.delete(transaction);
            throw new BusinessRuleException("Payment failed: " + payment.getMessage());
        }

        log.info("Stripe payment intent created: {} for transaction: {}",
                payment.getPaymentIntentId(), transaction.getId());

        return transaction;
    }

    @Override
    public void processReturn(LendingTransaction transaction) {
        transaction.setReturnDate(LocalDate.now());
        transaction.getBook().setStatus(BookStatus.AVAILABLE);

        // Calculate refund
        if (transaction.getDueDate().isBefore(LocalDate.now())) {
            // Book returned late — calculate late fee
            long daysLate = LocalDate.now().toEpochDay() - transaction.getDueDate().toEpochDay();
            BigDecimal lateFee = BigDecimal.valueOf(daysLate * 2); // ₹2 per day

            // Cap late fee at deposit amount
            if (lateFee.compareTo(transaction.getDepositPaid()) > 0) {
                lateFee = transaction.getDepositPaid();
            }

            transaction.setLateFee(lateFee);
            transaction.setTransactionStatus(BookStatus.OVERDUE);

            // Calculate refund amount
            BigDecimal refundAmount = transaction.getDepositPaid().subtract(lateFee);

            log.info("Book returned {} days late. Late fee: ₹{}. Refunding: ₹{}",
                    daysLate, lateFee, refundAmount);

            // Process refund through Stripe
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                stripePaymentService.refundPayment(
                        transaction.getBook().getId(), refundAmount);
            }
            waitlistService.notifyNextPerson(transaction.getBook());
        } else {
            // Book returned on time — full refund
            transaction.setLateFee(BigDecimal.ZERO);
            transaction.setTransactionStatus(BookStatus.AVAILABLE);

            log.info("Book returned on time. Full refund: ₹{}", transaction.getDepositPaid());

            // Full refund through Stripe
            stripePaymentService.refundPayment(
                    transaction.getBook().getId(), transaction.getDepositPaid());
        }

        transactionRepository.save(transaction);
    }

    @Override
    public void processExtension(LendingTransaction transaction, int additionalDays) {
        // Max 2 extensions for deposit-based books
        if (transaction.getExtensionCount() >= 2) {
            throw new BusinessRuleException(
                    "Maximum extension limit reached (2) for deposit-based lending. " +
                            "Please return the book or contact the lender.");
        }

        // Max 7 days per extension
        if (additionalDays > 7) {
            throw new BusinessRuleException("Extensions are limited to 7 days at a time for deposit-based lending");
        }

        // Total borrow period cannot exceed 28 days
        long currentBorrowDays = transaction.getDueDate().toEpochDay() - transaction.getBorrowDate().toEpochDay();
        long newTotal = currentBorrowDays + additionalDays;
        if (newTotal > 28) {
            throw new BusinessRuleException(
                    "Total borrowing period cannot exceed 28 days for deposit-based lending");
        }

        transaction.setDueDate(transaction.getDueDate().plusDays(additionalDays));
        transaction.setExtensionCount(transaction.getExtensionCount() + 1);

        log.info("Extension granted: {} days. New due date: {}. Extension count: {}",
                additionalDays, transaction.getDueDate(), transaction.getExtensionCount());

        transactionRepository.save(transaction);
    }

    @Override
    public boolean canBorrow(Book book, User user) {
        // Book must be available
        if (book.getStatus() != BookStatus.AVAILABLE) {
            return false;
        }

        // Cannot borrow your own book
        if (book.getOwner().getId().equals(user.getId())) {
            return false;
        }

        // For deposit-based: user can borrow multiple books
        // (since they're paying deposits, there's accountability)

        return true;
    }

    @Override
    public LendingPolicyType getStrategyType() {
        return LendingPolicyType.DEPOSIT_BASED;
    }
}