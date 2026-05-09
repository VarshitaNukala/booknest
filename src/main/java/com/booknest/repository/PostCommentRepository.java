package com.booknest.repository;


import com.booknest.entity.ClubFeedPost;
import com.booknest.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, String> {

    List<PostComment> findByPostOrderByCreatedAtAsc(ClubFeedPost post);

    long countByPost(ClubFeedPost post);

    List<PostComment> findTop3ByPostOrderByCreatedAtDesc(ClubFeedPost post);
}