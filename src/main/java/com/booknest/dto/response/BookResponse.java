package com.booknest.dto.response;


import com.booknest.enums.BookStatus;
import com.booknest.enums.LendingPolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private String coverImageUrl;
    private String ownerName;
    private String bookClubName;
    private BookStatus status;
    private LendingPolicyType lendingPolicy;
    private BigDecimal depositAmount;
    private Integer maxBorrowDays;
    private String condition;
    private LocalDateTime createdAt;
}