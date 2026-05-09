package com.booknest.controller;


import com.booknest.dto.request.CreateCommentRequest;
import com.booknest.dto.request.CreatePostRequest;
import com.booknest.dto.response.CommentResponse;
import com.booknest.dto.response.FeedPostResponse;
import com.booknest.entity.User;
import com.booknest.service.ClubFeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class ClubFeedController {

    private final ClubFeedService clubFeedService;

    @PostMapping("/posts")
    public ResponseEntity<FeedPostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clubFeedService.createPost(request, user));
    }

    @GetMapping("/clubs/{clubId}/posts")
    public ResponseEntity<Page<FeedPostResponse>> getClubFeed(
            @PathVariable String clubId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(clubFeedService.getClubFeed(clubId, pageable, user));
    }

    @GetMapping("/clubs/{clubId}/posts/pinned")
    public ResponseEntity<List<FeedPostResponse>> getPinnedPosts(
            @PathVariable String clubId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(clubFeedService.getPinnedPosts(clubId, user));
    }

    @PutMapping("/posts/{postId}/pin")
    public ResponseEntity<Void> togglePin(
            @PathVariable String postId,
            @AuthenticationPrincipal User user) {
        clubFeedService.pinPost(postId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String postId,
            @AuthenticationPrincipal User user) {
        clubFeedService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> addComment(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clubFeedService.addComment(request, user));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable String postId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(clubFeedService.getComments(postId, user));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal User user) {
        clubFeedService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }
}