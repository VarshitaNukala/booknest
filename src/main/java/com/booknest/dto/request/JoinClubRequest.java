package com.booknest.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinClubRequest {
    @NotBlank(message = "Club ID is required")
    private String clubId;
}