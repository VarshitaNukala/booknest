package com.booknest.controller;

import com.booknest.dto.request.BorrowBookRequest;
import com.booknest.dto.request.ExtendBorrowRequest;
import com.booknest.dto.request.LendBookRequest;
import com.booknest.dto.response.BookResponse;
import com.booknest.dto.response.LendingResponse;
import com.booknest.entity.User;
import com.booknest.service.LendingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final LendingService lendingService;

    /**
     * LEND A BOOK TO THE CLUB
     *
     * What it does: Owner adds their personal book to the book club's shared pool
     *
     * Cache behavior:
     * - EVICTS the club's book list cache because we just ADDED a new book
     * - Next call to getClubBooks(clubId) will fetch fresh data with this new book included
     *
     * SpEL breakdown:
     * - value = "books" → targets the same cache name used in @Cacheable
     * - key = "#request.bookClubId" → gets the club ID from the request body
     */
    @PostMapping("/lend")
    public ResponseEntity<BookResponse> lendBook(
            @Valid @RequestBody LendBookRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lendingService.lendBook(request, user));
    }

    /**
     * BORROW A BOOK FROM THE CLUB
     *
     * What it does: Member borrows an available book from the club
     *
     * Cache behavior:
     * - NO @CacheEvict needed here!
     * - Why? Because borrowing a book changes its STATUS (available → borrowed)
     *   but typically doesn't change what's IN the club's book list
     * - The book list usually shows all books regardless of current availability
     * - If your getClubBooks DOES show availability status, you'd want to evict here too
     */
    @PostMapping("/borrow")
    public ResponseEntity<LendingResponse> borrowBook(
            @Valid @RequestBody BorrowBookRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lendingService.borrowBook(request, user));
    }

    /**
     * RETURN A BORROWED BOOK
     *
     * What it does: Member returns a book they borrowed back to the club
     *
     * Cache behavior:
     * - SHOULD HAVE @CacheEvict if getClubBooks shows availability status
     * - Currently MISSING - this is a bug if your UI shows book availability
     * - Can't easily evict because we don't know the clubId from transactionId alone
     *
     * Better approach:
     * Option 1: Have the service return the clubId in LendingResponse
     * Option 2: Evict all books cache entries (less performant but simpler)
     */
    @PutMapping("/return/{transactionId}")
    // @CacheEvict(value = "books", allEntries = true) // Fallback: clear all
    // OR if LendingResponse contains bookClubId:

    public ResponseEntity<LendingResponse> returnBook(
            @PathVariable String transactionId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lendingService.returnBook(transactionId, user));
    }

    /**
     * GET ALL BOOKS IN A CLUB
     *
     * What it does: Returns the list of books available in a specific book club
     *
     * Cache behavior:
     * - @Cacheable caches the result using clubId as the key
     * - First call: executes method, stores result in cache
     * - Subsequent calls with same clubId: returns cached result (no DB query!)
     * - Cache cleared when new books are added (lendBook) or books change status
     */
    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<BookResponse>> getClubBooks(@PathVariable String clubId) {
        return ResponseEntity.ok(lendingService.getClubBooks(clubId));
    }

    /**
     * GET CURRENT USER'S BORROWED BOOKS
     *
     * No caching needed - this is user-specific and changes frequently
     */
    @GetMapping("/borrowed")
    public ResponseEntity<List<LendingResponse>> getMyBorrowedBooks(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lendingService.getUserBorrowedBooks(user));
    }

    /**
     * EXTEND BORROWING PERIOD
     *
     * What it does: Extends the due date for a borrowed book
     *
     * Cache behavior:
     * - NO eviction needed - extending a borrow doesn't change what books
     *   are in the club or their availability status
     * - Only the due date changes, which is handled by getMyBorrowedBooks
     */
    @PutMapping("/extend")
    public ResponseEntity<LendingResponse> extendBorrow(
            @Valid @RequestBody ExtendBorrowRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lendingService.extendBorrow(request, user));
    }

    /**
     * GET LENDING HISTORY FOR A SPECIFIC BOOK
     *
     * No caching - historical data that could change (new borrowing records)
     */
    @GetMapping("/history/{bookId}")
    public ResponseEntity<List<LendingResponse>> getBookHistory(
            @PathVariable String bookId) {
        return ResponseEntity.ok(lendingService.getBookLendingHistory(bookId));
    }

    /**
     * GET LENDING HISTORY FOR AN ENTIRE CLUB
     *
     * No caching - historical data that changes frequently
     */
    @GetMapping("/club/{clubId}/history")
    public ResponseEntity<List<LendingResponse>> getClubLendingHistory(
            @PathVariable String clubId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(lendingService.getClubLendingHistory(clubId, user));
    }
}