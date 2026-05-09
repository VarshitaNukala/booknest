package com.booknest.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtendBorrowRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Additional days is required")
    @Min(value = 1, message = "Extension must be at least 1 day")
    private Integer additionalDays;

    private String reason;  // Optional: why borrower needs extension
}