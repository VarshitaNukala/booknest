package com.booknest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MembershipActionRequest {
    @NotBlank(message = "Membership ID is required")
    private String membershipId;

    @NotNull(message = "Approve must be true or false")
    private Boolean approve;
}