package com.booknest.service;


import com.booknest.dto.request.CreateCommentRequest;
import com.booknest.dto.request.CreatePostRequest;
import com.booknest.dto.response.CommentResponse;
import com.booknest.dto.response.FeedPostResponse;
import com.booknest.entity.*;
import com.booknest.enums.MembershipStatus;
import com.booknest.exception.BusinessRuleException;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.exception.UnauthorizedException;
import com.booknest.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubFeedService {

    private final ClubFeedPostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final BookClubRepository bookClubRepository;
    private final BookRepository bookRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public FeedPostResponse createPost(CreatePostRequest request, User author) {
        BookClub club = bookClubRepository.findById(request.getBookClubId())
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", request.getBookClubId()));

        validateMembership(club, author);

        ClubFeedPost.ClubFeedPostBuilder builder = ClubFeedPost.builder()
                .bookClub(club)
                .author(author)
                .content(request.getContent())
                .imageUrl(request.getImageUrl());

        if (request.getPostType() != null) {
            builder.postType(ClubFeedPost.PostType.valueOf(request.getPostType().toUpperCase()));
        }

        if (request.getRelatedBookId() != null) {
            Book book = bookRepository.findById(request.getRelatedBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getRelatedBookId()));
            builder.relatedBook(book);
        }

        ClubFeedPost post = postRepository.save(builder.build());
        return mapToPostResponse(post);
    }

    @Transactional(readOnly = true)
    public Page<FeedPostResponse> getClubFeed(String clubId, Pageable pageable, User user) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        validateMembership(club, user);

        return postRepository.findByBookClubOrderByCreatedAtDesc(club, pageable)
                .map(this::mapToPostResponse);
    }

    @Transactional(readOnly = true)
    public List<FeedPostResponse> getPinnedPosts(String clubId, User user) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        validateMembership(club, user);

        return postRepository.findByBookClubAndIsPinnedTrueOrderByCreatedAtDesc(club)
                .stream()
                .map(this::mapToPostResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void pinPost(String postId, User admin) {
        ClubFeedPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("ClubFeedPost", "id", postId));

        validateClubAdmin(post.getBookClub(), admin);

        post.setPinned(!post.isPinned());
        postRepository.save(post);
    }

    @Transactional
    public void deletePost(String postId, User user) {
        ClubFeedPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("ClubFeedPost", "id", postId));

        boolean isAuthor = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = post.getBookClub().getCreatedBy().getId().equals(user.getId());

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("Only the author or club admin can delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public CommentResponse addComment(CreateCommentRequest request, User author) {
        ClubFeedPost post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("ClubFeedPost", "id", request.getPostId()));

        validateMembership(post.getBookClub(), author);

        PostComment.PostCommentBuilder builder = PostComment.builder()
                .post(post)
                .author(author)
                .content(request.getContent());

        if (request.getParentCommentId() != null) {
            PostComment parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", request.getParentCommentId()));
            builder.parentComment(parent);
        }

        PostComment comment = commentRepository.save(builder.build());
        return mapToCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(String postId, User user) {
        ClubFeedPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("ClubFeedPost", "id", postId));

        validateMembership(post.getBookClub(), user);

        return commentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(String commentId, User user) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", commentId));

        boolean isAuthor = comment.getAuthor().getId().equals(user.getId());
        boolean isAdmin = comment.getPost().getBookClub().getCreatedBy().getId().equals(user.getId());

        if (!isAuthor && !isAdmin) {
            throw new UnauthorizedException("Only the author or club admin can delete this comment");
        }

        commentRepository.delete(comment);
    }

    private void validateMembership(BookClub club, User user) {
        boolean isMember = membershipRepository
                .findByUserAndBookClub(user, club)
                .map(m -> m.getStatus() == MembershipStatus.APPROVED)
                .orElse(false);

        if (!isMember) {
            throw new UnauthorizedException("You must be an approved club member to perform this action");
        }
    }

    private void validateClubAdmin(BookClub club, User user) {
        if (!club.getCreatedBy().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the club admin can perform this action");
        }
    }

    private FeedPostResponse mapToPostResponse(ClubFeedPost post) {
        List<CommentResponse> recentComments = commentRepository.findTop3ByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());

        return FeedPostResponse.builder()
                .id(post.getId())
                .clubName(post.getBookClub().getName())
                .authorName(post.getAuthor().getFullName())
                .authorId(post.getAuthor().getId())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .postType(post.getPostType().name())
                .relatedBookTitle(post.getRelatedBook() != null ? post.getRelatedBook().getTitle() : null)
                .isPinned(post.isPinned())
                .commentCount((int) commentRepository.countByPost(post))
                .recentComments(recentComments)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private CommentResponse mapToCommentResponse(PostComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorName(comment.getAuthor().getFullName())
                .authorId(comment.getAuthor().getId())
                .content(comment.getContent())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}