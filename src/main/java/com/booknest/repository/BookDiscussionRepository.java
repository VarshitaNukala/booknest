package com.booknest.repository;


import com.booknest.entity.Book;
import com.booknest.entity.BookClub;
import com.booknest.entity.BookDiscussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookDiscussionRepository extends JpaRepository<BookDiscussion, String> {

    Page<BookDiscussion> findByBookClubOrderByCreatedAtDesc(BookClub bookClub, Pageable pageable);

    List<BookDiscussion> findByBookClubAndBook(BookClub bookClub, Book book);

    List<BookDiscussion> findByBookClubAndStatus(BookClub bookClub, BookDiscussion.DiscussionStatus status);
}