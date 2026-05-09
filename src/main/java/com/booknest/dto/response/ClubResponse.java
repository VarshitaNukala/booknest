package com.booknest.dto.response;


import com.booknest.enums.ClubType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class ClubResponse {
    private String id;
    private String name;
    private String description;
    private String city;
    private String state;
    private ClubType clubType;
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    private String createdByName;
    private int memberCount;
    private LocalDateTime createdAt;
}