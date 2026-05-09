package com.booknest.dto.request;


import com.booknest.enums.ClubType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClubRequest {
    @NotBlank(message = "Club name is required")
    private String name;

    private String description;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Club type is required")
    private ClubType clubType;

    private boolean isPrivate;
}