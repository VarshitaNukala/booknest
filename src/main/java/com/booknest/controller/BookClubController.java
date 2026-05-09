package com.booknest.controller;

import com.booknest.dto.request.CreateClubRequest;
import com.booknest.dto.response.ClubResponse;
import com.booknest.entity.User;
import com.booknest.service.BookClubService;
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
@RequestMapping("/api/v1/clubs")
@RequiredArgsConstructor
public class BookClubController {

    private final BookClubService bookClubService;

    @PostMapping
    public ResponseEntity<ClubResponse> createClub(
            @Valid @RequestBody CreateClubRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookClubService.createClub(request, user));
    }

    @GetMapping
    public ResponseEntity<List<ClubResponse>> searchClubs(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String clubType,
            @RequestParam(required = false) Boolean isPrivate,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(bookClubService.searchClubs(city, state, clubType, isPrivate, pageable));
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<ClubResponse> getClub(@PathVariable String clubId) {
        return ResponseEntity.ok(bookClubService.getClub(clubId));
    }
}