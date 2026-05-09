package com.booknest.controller;


import com.booknest.dto.response.ClubResponse;
import com.booknest.entity.BookClub;
import com.booknest.entity.User;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.UserRepository;
import com.booknest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final BookClubRepository bookClubRepository;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal User user) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));

        return ResponseEntity.ok(Map.of(
                "id", freshUser.getId(),
                "fullName", freshUser.getFullName(),
                "email", freshUser.getEmail(),
                "phoneNumber", freshUser.getPhoneNumber() != null ? freshUser.getPhoneNumber() : "",
                "city", freshUser.getCity() != null ? freshUser.getCity() : "",
                "state", freshUser.getState() != null ? freshUser.getState() : "",
                "role", freshUser.getRole().name(),
                "createdAt", freshUser.getCreatedAt()
        ));
    }

    @GetMapping("/me/clubs")
    public ResponseEntity<List<ClubResponse>> getMyClubs(@AuthenticationPrincipal User user) {
        List<BookClub> clubs = bookClubRepository.findByCreatedBy(user);

        List<ClubResponse> response = clubs.stream()
                .map(club -> ClubResponse.builder()
                        .id(club.getId())
                        .name(club.getName())
                        .description(club.getDescription())
                        .city(club.getCity())
                        .state(club.getState())
                        .clubType(club.getClubType())
                        .isPrivate(club.isPrivate())
                        .createdByName(user.getFullName())
                        .memberCount(0) // Could populate with actual count
                        .createdAt(club.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, String>> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> updates) {

        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", user.getId()));

        if (updates.containsKey("fullName")) {
            freshUser.setFullName(updates.get("fullName"));
        }
        if (updates.containsKey("phoneNumber")) {
            freshUser.setPhoneNumber(updates.get("phoneNumber"));
        }
        if (updates.containsKey("city")) {
            freshUser.setCity(updates.get("city"));
        }
        if (updates.containsKey("state")) {
            freshUser.setState(updates.get("state"));
        }

        userRepository.save(freshUser);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}