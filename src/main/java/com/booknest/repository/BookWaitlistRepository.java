package com.booknest.repository;

import com.booknest.entity.Book;
import com.booknest.entity.BookWaitlist;
import com.booknest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookWaitlistRepository extends JpaRepository<BookWaitlist, String> {

    Optional<BookWaitlist> findByBookAndUser(Book book, User user);

    List<BookWaitlist> findByBookAndStatusOrderByQueuePositionAsc(Book book, BookWaitlist.WaitlistStatus status);

    @Query("SELECT COALESCE(MAX(w.queuePosition), 0) FROM BookWaitlist w WHERE w.book = :book")
    Integer findMaxQueuePosition(@Param("book") Book book);

    @Query("SELECT w FROM BookWaitlist w WHERE w.book = :book AND w.status = 'WAITING' ORDER BY w.queuePosition ASC")
    List<BookWaitlist> findNextInQueue(@Param("book") Book book);

    long countByBookAndStatus(Book book, BookWaitlist.WaitlistStatus status);

    boolean existsByBookAndUserAndStatus(Book book, User user, BookWaitlist.WaitlistStatus status);
}