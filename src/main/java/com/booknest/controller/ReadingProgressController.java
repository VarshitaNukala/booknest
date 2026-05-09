package com.booknest.controller;


import com.booknest.dto.request.UpdateReadingProgressRequest;
import com.booknest.dto.response.ReadingProgressResponse;
import com.booknest.entity.User;
import com.booknest.service.ReadingProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reading")
@RequiredArgsConstructor
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    @PostMapping("/progress")
    public ResponseEntity<ReadingProgressResponse> updateProgress(
            @Valid @RequestBody UpdateReadingProgressRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(readingProgressService.updateProgress(request, user));
    }

    @GetMapping("/progress/{bookId}")
    public ResponseEntity<ReadingProgressResponse> getMyProgress(
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(readingProgressService.getMyProgress(bookId, user));
    }

    @GetMapping("/my-list")
    public ResponseEntity<List<ReadingProgressResponse>> getMyReadingList(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(readingProgressService.getMyReadingList(user));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<ReadingProgressResponse>> getClubProgress(
            @PathVariable String clubId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(readingProgressService.getClubReadingProgress(clubId, user));
    }

    @GetMapping("/book/{bookId}/readers")
    public ResponseEntity<List<ReadingProgressResponse>> getBookReaders(
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(readingProgressService.getBookReaders(bookId, user));
    }

    @DeleteMapping("/progress/{bookId}")
    public ResponseEntity<Void> deleteProgress(
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        readingProgressService.deleteProgress(bookId, user);
        return ResponseEntity.noContent().build();
    }
}