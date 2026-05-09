package com.booknest.controller;

import com.booknest.dto.response.WaitlistResponse;
import com.booknest.entity.User;
import com.booknest.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping("/join/{bookId}")
    public ResponseEntity<WaitlistResponse> joinWaitlist(
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(waitlistService.joinWaitlist(bookId, user));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<WaitlistResponse>> getBookWaitlist(
            @PathVariable String bookId) {
        return ResponseEntity.ok(waitlistService.getBookWaitlist(bookId));
    }

    @GetMapping("/position/{bookId}")
    public ResponseEntity<WaitlistResponse> getMyPosition(
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(waitlistService.getMyPosition(bookId, user));
    }

    @DeleteMapping("/leave/{bookId}")
    public ResponseEntity<Void> leaveWaitlist(
            @PathVariable String bookId,
            @AuthenticationPrincipal User user) {
        waitlistService.leaveWaitlist(bookId, user);
        return ResponseEntity.noContent().build();
    }
}