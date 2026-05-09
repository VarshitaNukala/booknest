package com.booknest.repository;


import com.booknest.entity.Book;
import com.booknest.entity.BookClub;
import com.booknest.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    List<Book> findByBookClubAndStatus(BookClub bookClub, BookStatus status);
    List<Book> findByBookClub(BookClub bookClub);
}