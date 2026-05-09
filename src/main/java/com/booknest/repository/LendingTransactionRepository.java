package com.booknest.repository;


import com.booknest.entity.LendingTransaction;
import com.booknest.entity.User;
import com.booknest.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LendingTransactionRepository extends JpaRepository<LendingTransaction, String> {
    List<LendingTransaction> findByBorrowerAndTransactionStatus(User borrower, BookStatus status);
    List<LendingTransaction> findByLenderAndTransactionStatus(User lender, BookStatus status);

    @Query("SELECT lt FROM LendingTransaction lt WHERE " +
            "lt.dueDate <= :date AND lt.transactionStatus = 'BORROWED'")
    List<LendingTransaction> findOverdueTransactions(@Param("date") LocalDate date);

    @Query("SELECT lt FROM LendingTransaction lt WHERE " +
            "lt.dueDate = :date AND lt.transactionStatus = 'BORROWED'")
    List<LendingTransaction> findTransactionsDueOn(@Param("date") LocalDate date);


    List<LendingTransaction> findAll();
}