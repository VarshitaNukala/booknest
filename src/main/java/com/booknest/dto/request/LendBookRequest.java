package com.booknest.dto.request;

import com.booknest.enums.LendingPolicyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class LendBookRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;

    private String description;

    @NotBlank(message = "Book club ID is required")
    private String bookClubId;

    @NotNull(message = "Lending policy is required")
    private LendingPolicyType lendingPolicy;

    private BigDecimal depositAmount;

    private Integer maxBorrowDays;

    private String condition;
}