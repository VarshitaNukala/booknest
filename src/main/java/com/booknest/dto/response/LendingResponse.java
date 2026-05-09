package com.booknest.dto.response;


import com.booknest.enums.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LendingResponse {
    private String transactionId;
    private String bookTitle;
    private String lenderName;
    private String borrowerName;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal depositPaid;
    private BookStatus transactionStatus;
    private Integer extensionCount;
    private String bookClubId;
}