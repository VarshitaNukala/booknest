package com.booknest.service;


import com.booknest.dto.request.CreateDiscussionReplyRequest;
import com.booknest.dto.request.CreateDiscussionRequest;
import com.booknest.dto.response.DiscussionReplyResponse;
import com.booknest.dto.response.DiscussionResponse;
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
public class BookDiscussionService {

    private final BookDiscussionRepository discussionRepository;
    private final DiscussionReplyRepository replyRepository;
    private final BookClubRepository bookClubRepository;
    private final BookRepository bookRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public DiscussionResponse createDiscussion(CreateDiscussionRequest request, User creator) {
        BookClub club = bookClubRepository.findById(request.getBookClubId())
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", request.getBookClubId()));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        validateMembership(club, creator);

        BookDiscussion discussion = BookDiscussion.builder()
                .bookClub(club)
                .book(book)
                .topic(request.getTopic())
                .description(request.getDescription())
                .createdBy(creator)
                .build();

        discussion = discussionRepository.save(discussion);
        return mapToDiscussionResponse(discussion);
    }

    @Transactional(readOnly = true)
    public Page<DiscussionResponse> getClubDiscussions(String clubId, Pageable pageable, User user) {
        BookClub club = bookClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("BookClub", "id", clubId));

        validateMembership(club, user);

        return discussionRepository.findByBookClubOrderByCreatedAtDesc(club, pageable)
                .map(this::mapToDiscussionResponse);
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> getBookDiscussions(String bookId, User user) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        validateMembership(book.getBookClub(), user);

        return discussionRepository.findByBookClubAndBook(book.getBookClub(), book)
                .stream()
                .map(this::mapToDiscussionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DiscussionResponse getDiscussion(String discussionId, User user) {
        BookDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookDiscussion", "id", discussionId));

        validateMembership(discussion.getBookClub(), user);

        return mapToDiscussionResponse(discussion);
    }

    @Transactional
    public DiscussionResponse closeDiscussion(String discussionId, User user) {
        BookDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookDiscussion", "id", discussionId));

        boolean isCreator = discussion.getCreatedBy().getId().equals(user.getId());
        boolean isClubAdmin = discussion.getBookClub().getCreatedBy().getId().equals(user.getId());

        if (!isCreator && !isClubAdmin) {
            throw new UnauthorizedException("Only the discussion creator or club admin can close this discussion");
        }

        discussion.setStatus(BookDiscussion.DiscussionStatus.CLOSED);
        discussion = discussionRepository.save(discussion);
        return mapToDiscussionResponse(discussion);
    }

    @Transactional
    public DiscussionReplyResponse addReply(CreateDiscussionReplyRequest request, User author) {
        BookDiscussion discussion = discussionRepository.findById(request.getDiscussionId())
                .orElseThrow(() -> new ResourceNotFoundException("BookDiscussion", "id", request.getDiscussionId()));

        validateMembership(discussion.getBookClub(), author);

        if (discussion.getStatus() == BookDiscussion.DiscussionStatus.CLOSED) {
            throw new BusinessRuleException("Cannot reply to a closed discussion");
        }

        DiscussionReply reply = DiscussionReply.builder()
                .discussion(discussion)
                .author(author)
                .content(request.getContent())
                .build();

        reply = replyRepository.save(reply);
        return mapToReplyResponse(reply);
    }

    @Transactional(readOnly = true)
    public List<DiscussionReplyResponse> getReplies(String discussionId, User user) {
        BookDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookDiscussion", "id", discussionId));

        validateMembership(discussion.getBookClub(), user);

        return replyRepository.findByDiscussionOrderByCreatedAtAsc(discussion)
                .stream()
                .map(this::mapToReplyResponse)
                .collect(Collectors.toList());
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

    private DiscussionResponse mapToDiscussionResponse(BookDiscussion discussion) {
        List<DiscussionReplyResponse> recentReplies = replyRepository
                .findTop3ByDiscussionOrderByCreatedAtDesc(discussion)
                .stream()
                .map(this::mapToReplyResponse)
                .collect(Collectors.toList());

        return DiscussionResponse.builder()
                .id(discussion.getId())
                .bookTitle(discussion.getBook().getTitle())
                .bookAuthor(discussion.getBook().getAuthor())
                .clubName(discussion.getBookClub().getName())
                .topic(discussion.getTopic())
                .description(discussion.getDescription())
                .createdByName(discussion.getCreatedBy().getFullName())
                .status(discussion.getStatus().name())
                .replyCount((int) replyRepository.countByDiscussion(discussion))
                .recentReplies(recentReplies)
                .createdAt(discussion.getCreatedAt())
                .build();
    }

    private DiscussionReplyResponse mapToReplyResponse(DiscussionReply reply) {
        return DiscussionReplyResponse.builder()
                .id(reply.getId())
                .authorName(reply.getAuthor().getFullName())
                .authorId(reply.getAuthor().getId())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}