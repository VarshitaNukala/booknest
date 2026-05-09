package com.booknest.repository;

import com.booknest.entity.BookClub;
import com.booknest.entity.User;
import com.booknest.enums.ClubType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookClubRepository extends JpaRepository<BookClub, String> {

    @Query("SELECT bc FROM BookClub bc WHERE " +
            "(:city IS NULL OR bc.city = :city) AND " +
            "(:state IS NULL OR bc.state = :state) AND " +
            "(:clubType IS NULL OR bc.clubType = :clubType) AND " +
            "(:isPrivate IS NULL OR bc.isPrivate = :isPrivate)")
    Page<BookClub> searchClubs(@Param("city") String city,
                               @Param("state") String state,
                               @Param("clubType") ClubType clubType,
                               @Param("isPrivate") Boolean isPrivate,
                               Pageable pageable);

    List<BookClub> findByCreatedBy(User user);



    @Query(value = """
    SELECT b.title, b.author, COUNT(lt.id) as borrow_count
    FROM books b
    JOIN lending_transactions lt ON b.id = lt.book_id
    WHERE b.book_club_id = :clubId
    GROUP BY b.id, b.title, b.author
    ORDER BY borrow_count DESC
    LIMIT 5
""", nativeQuery = true)
    List<Object[]> findMostBorrowedBooksNative(@Param("clubId") String clubId);

    @Query(value = """
    SELECT u.full_name,
           COUNT(DISTINCT CASE WHEN b.owner_id = u.id THEN b.id END) as books_lent,
           COUNT(DISTINCT CASE WHEN lt.borrower_id = u.id THEN lt.id END) as books_borrowed,
           COUNT(DISTINCT CASE WHEN rp.reading_status = 'CURRENTLY_READING' THEN rp.id END) as currently_reading
    FROM users u
    JOIN memberships m ON u.id = m.user_id
    LEFT JOIN books b ON u.id = b.owner_id AND b.book_club_id = :clubId
    LEFT JOIN lending_transactions lt ON u.id = lt.borrower_id
    LEFT JOIN reading_progress rp ON u.id = rp.user_id
    WHERE m.book_club_id = :clubId AND m.status = 'APPROVED'
    GROUP BY u.id, u.full_name
    ORDER BY books_lent DESC, books_borrowed DESC
    LIMIT 10
""", nativeQuery = true)
    List<Object[]> findMostActiveMembersNative(@Param("clubId") String clubId);

    @Query(value = """
    SELECT b.status, COUNT(*) as count
    FROM books b
    WHERE b.book_club_id = :clubId
    GROUP BY b.status
""", nativeQuery = true)
    List<Object[]> findBookCountByStatusNative(@Param("clubId") String clubId);

    @Query(value = """
    SELECT COALESCE(AVG(rp.percentage_complete), 0)
    FROM reading_progress rp
    JOIN books b ON rp.book_id = b.id
    WHERE b.book_club_id = :clubId
""", nativeQuery = true)
    Double findAvgReadingPercentageNative(@Param("clubId") String clubId);
}