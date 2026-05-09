package com.booknest.repository;


import com.booknest.entity.BookDiscussion;
import com.booknest.entity.DiscussionReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, String> {

    List<DiscussionReply> findByDiscussionOrderByCreatedAtAsc(BookDiscussion discussion);

    long countByDiscussion(BookDiscussion discussion);

    List<DiscussionReply> findTop3ByDiscussionOrderByCreatedAtDesc(BookDiscussion discussion);
}