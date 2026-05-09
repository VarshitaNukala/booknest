package com.booknest.repository;

import com.booknest.entity.BookClub;
import com.booknest.entity.ClubFeedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubFeedPostRepository extends JpaRepository<ClubFeedPost, String> {

    Page<ClubFeedPost> findByBookClubOrderByCreatedAtDesc(BookClub bookClub, Pageable pageable);

    List<ClubFeedPost> findByBookClubAndIsPinnedTrueOrderByCreatedAtDesc(BookClub bookClub);

    @Query("SELECT p FROM ClubFeedPost p WHERE p.bookClub = :club " +
            "AND (p.postType = :type1 OR p.postType = :type2) " +
            "ORDER BY p.createdAt DESC")
    Page<ClubFeedPost> findBookRelatedPosts(@Param("club") BookClub club,
                                            @Param("type1") ClubFeedPost.PostType type1,
                                            @Param("type2") ClubFeedPost.PostType type2,
                                            Pageable pageable);
}