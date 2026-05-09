package com.booknest.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BorrowBookRequest {
    @NotBlank(message = "Book ID is required")
    private String bookId;

    private String messageToLender;
}