package com.booknest.service;


import com.booknest.dto.request.MembershipActionRequest;
import com.booknest.dto.request.JoinClubRequest;
import com.booknest.dto.response.MembershipResponse;
import com.booknest.entity.BookClub;
import com.booknest.entity.Membership;
import com.booknest.entity.User;
import com.booknest.enums.MembershipStatus;
import com.booknest.enums.Role;
import com.booknest.exception.BusinessRuleException;
import com.booknest.exception.ResourceNotFoundException;
import com.booknest.exception.UnauthorizedException;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final BookClubRepository bookClubRepository;

    @Transactional
    public MembershipResponse requestToJoin(JoinClubRequest request, User user) {
        BookClub club = bookClubRepository.findById(request.getClubId())
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", request.getClubId()));

        if (membershipRepository.existsByUserAndBookClub(user, club)) {
            throw new BusinessRuleException("You already have a membership request for this club");
        }

        // Auto-approve for public clubs
        Membership membership = Membership.builder()
                .user(user)
                .bookClub(club)
                .status(club.isPrivate() ? MembershipStatus.PENDING : MembershipStatus.APPROVED)
                .build();

        if (!club.isPrivate()) {
            membership.setRespondedAt(LocalDateTime.now());
        }

        membership = membershipRepository.save(membership);

        return mapToResponse(membership);
    }

    

    @Transactional
    public MembershipResponse approveOrReject(MembershipActionRequest request, User admin) {
        Membership membership = membershipRepository.findById(request.getMembershipId())
                .orElseThrow(() -> new ResourceNotFoundException("Membership", "id", request.getMembershipId()));

        // Check if admin is the club creator or has platform admin role
        boolean isClubOwner = membership.getBookClub().getCreatedBy().getId().equals(admin.getId());
        boolean isPlatformAdmin = admin.getRole() == Role.ROLE_PLATFORM_ADMIN;

        if (!isClubOwner && !isPlatformAdmin) {
            throw new UnauthorizedException("Only the club admin can approve or reject membership requests");
        }

        // Cannot approve/reject someone who is already approved or rejected
        if (membership.getStatus() == MembershipStatus.APPROVED ||
                membership.getStatus() == MembershipStatus.REJECTED) {
            throw new BusinessRuleException("This membership request has already been processed");
        }

        membership.setStatus(request.getApprove() ? MembershipStatus.APPROVED : MembershipStatus.REJECTED);
        membership.setRespondedAt(LocalDateTime.now());
        membership.setRespondedBy(admin);

        membership = membershipRepository.save(membership);
        return mapToResponse(membership);
    }

    @Transactional
    public List<MembershipResponse> getPendingRequests(String clubId, User admin) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        boolean isClubOwner = club.getCreatedBy().getId().equals(admin.getId());
        boolean isPlatformAdmin = admin.getRole() == Role.ROLE_PLATFORM_ADMIN;

        if (!isClubOwner && !isPlatformAdmin) {
            throw new UnauthorizedException("Only the club admin can view pending requests");
        }

        return membershipRepository.findByBookClubAndStatus(club, MembershipStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getClubMembers(String clubId) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        return membershipRepository.findByBookClubAndStatus(club, MembershipStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void leaveClub(String clubId, User user) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        Membership membership = membershipRepository.findByUserAndBookClub(user, club)
                .orElseThrow(() -> new BusinessRuleException("You are not a member of this club"));

        // Club creator cannot leave — they must delete the club instead
        if (club.getCreatedBy().getId().equals(user.getId())) {
            throw new BusinessRuleException(
                    "Club creator cannot leave. You can delete the club or transfer ownership."
            );
        }

        membershipRepository.delete(membership);
    }

    private MembershipResponse mapToResponse(Membership membership) {
        String respondedByName = membership.getRespondedBy() != null
                ? membership.getRespondedBy().getFullName()
                : null;

        return MembershipResponse.builder()
                .id(membership.getId())
                .userName(membership.getUser().getFullName())
                .clubName(membership.getBookClub().getName())
                .status(membership.getStatus())
                .requestedAt(membership.getRequestedAt())
                .respondedAt(membership.getRespondedAt())
                .build();
    }
}