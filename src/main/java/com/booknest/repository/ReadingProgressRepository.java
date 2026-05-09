package com.booknest.repository;


import com.booknest.entity.BookClub;
import com.booknest.entity.ReadingProgress;
import com.booknest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, String> {

    Optional<ReadingProgress> findByUserAndBook(User user, com.booknest.entity.Book book);

    List<ReadingProgress> findByUser(User user);

    List<ReadingProgress> findByUserAndIsPublicTrue(User user);

    @Query("SELECT rp FROM ReadingProgress rp WHERE rp.book.bookClub = :club AND rp.isPublic = true")
    List<ReadingProgress> findPublicProgressByClub(@Param("club") BookClub club);

    @Query("SELECT rp FROM ReadingProgress rp WHERE rp.book.id = :bookId AND rp.isPublic = true")
    List<ReadingProgress> findPublicProgressByBook(@Param("bookId") String bookId);

    long countByBookBookClubAndReadingStatus(BookClub club, ReadingProgress.ReadingStatus status);
}