package com.booknest.service;

import com.booknest.dto.request.CreateClubRequest;
import com.booknest.dto.response.ClubResponse;
import com.booknest.entity.BookClub;
import com.booknest.entity.Membership;
import com.booknest.entity.User;
import com.booknest.enums.ClubType;
import com.booknest.enums.MembershipStatus;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookClubService {

    private final BookClubRepository bookClubRepository;
    private final MembershipRepository membershipRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ClubResponse createClub(CreateClubRequest request, User creator) {

        // Create Book Club
        BookClub club = BookClub.builder()
                .name(request.getName())
                .description(request.getDescription())
                .city(request.getCity())
                .state(request.getState())
                .clubType(request.getClubType())
                .isPrivate(request.isPrivate())
                .createdBy(creator)
                .build();

        club = bookClubRepository.save(club);

        // Auto-add creator as approved member
        Membership membership = Membership.builder()
                .user(creator)
                .bookClub(club)
                .status(MembershipStatus.APPROVED)
                .build();

        membershipRepository.save(membership);

        log.info("Club created successfully: {} (id={})",
                club.getName(), club.getId());

        return mapToResponse(club);
    }

//    @Cacheable(cacheNames = "clubSearchResults",
//            key = "#city + '_' + #state + '_' + #clubType + '_' + #isPrivate + '_' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public List<ClubResponse> searchClubs(String city, String state,
                                          String clubType, Boolean isPrivate,
                                          Pageable pageable) {
        log.info("Cache MISS — searching clubs");
        return bookClubRepository.searchClubs(city, state,
                        clubType != null ? ClubType.valueOf(clubType) : null,
                        isPrivate, pageable)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());  // ← Return List, not Page
    }

    @Cacheable(
            cacheNames = "clubDetails",
            key = "#clubId"
    )
    @Transactional(readOnly = true)
    public ClubResponse getClub(String clubId) {

        log.info("Cache MISS — fetching club details for id={}", clubId);

        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "BookClub",
                                "id",
                                clubId
                        )
                );

        return mapToResponse(club);
    }

    private ClubResponse mapToResponse(BookClub club) {

        // ModelMapper handles simple fields
        ClubResponse response =
                modelMapper.map(club, ClubResponse.class);

        // Manual mapping for computed / nested fields
        response.setCreatedByName(
                club.getCreatedBy().getFullName()
        );

        long memberCount =
                membershipRepository.countByBookClubAndStatus(
                        club,
                        MembershipStatus.APPROVED
                );

        response.setMemberCount((int) memberCount);

        return response;
    }
}