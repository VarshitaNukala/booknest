package com.booknest.controller;

import com.booknest.dto.response.ClubAnalyticsResponse;
import com.booknest.entity.User;
import com.booknest.service.ClubAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class ClubAnalyticsController {

    private final ClubAnalyticsService analyticsService;

    @GetMapping("/club/{clubId}")
    public ResponseEntity<ClubAnalyticsResponse> getClubAnalytics(
            @PathVariable String clubId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(analyticsService.getClubAnalytics(clubId));
    }
}