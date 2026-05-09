package com.booknest.dto.response;


import com.booknest.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponse {
    private String id;
    private String userName;
    private String clubName;
    private MembershipStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
}