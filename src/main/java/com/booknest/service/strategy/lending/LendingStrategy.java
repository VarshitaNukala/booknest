package com.booknest.service.strategy.lending;


import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.entity.Book;
import com.booknest.entity.LendingTransaction;
import com.booknest.entity.User;
import com.booknest.enums.LendingPolicyType;

public interface LendingStrategy {
    LendingTransaction processBorrow(Book book, User borrower, BorrowBookRequest request);
    void processReturn(LendingTransaction transaction);
    void processExtension(LendingTransaction transaction, int additionalDays);
    boolean canBorrow(Book book, User user);
    LendingPolicyType getStrategyType();
}