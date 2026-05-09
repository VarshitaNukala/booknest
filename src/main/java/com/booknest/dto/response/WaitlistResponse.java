package com.booknest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistResponse {
    private String id;
    private String bookTitle;
    private String userName;
    private int queuePosition;
    private int totalInQueue;
    private String status;
    private LocalDateTime joinedAt;
}