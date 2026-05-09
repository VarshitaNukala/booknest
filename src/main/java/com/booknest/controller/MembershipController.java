package com.booknest.controller;

import com.booknest.dto.request.JoinClubRequest;
import com.booknest.dto.request.MembershipActionRequest;
import com.booknest.dto.response.MembershipResponse;
import com.booknest.entity.User;
import com.booknest.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/join")
    public ResponseEntity<MembershipResponse> joinClub(
            @Valid @RequestBody JoinClubRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(membershipService.requestToJoin(request, user));
    }

    @PutMapping("/approve")
    public ResponseEntity<MembershipResponse> approveMembership(
            @Valid @RequestBody MembershipActionRequest request,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(membershipService.approveOrReject(request, admin));
    }

    @GetMapping("/{clubId}/pending")
    public ResponseEntity<List<MembershipResponse>> getPendingRequests(
            @PathVariable String clubId,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(membershipService.getPendingRequests(clubId, admin));
    }
}