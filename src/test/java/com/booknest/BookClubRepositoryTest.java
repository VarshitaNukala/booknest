package com.booknest;


import com.booknest.entity.BookClub;
import com.booknest.entity.User;
import com.booknest.enums.ClubType;
import com.booknest.enums.Role;
import com.booknest.repository.BookClubRepository;
import com.booknest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class BookClubRepositoryTest {

    @Autowired
    private BookClubRepository bookClubRepository;

    @Autowired
    private UserRepository userRepository;

    private User creator;

    @BeforeEach
    void setUp() {
        creator = userRepository.save(User.builder()
                .fullName("Test Creator")
                .email("creator@test.com")
                .password("password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .accountNonLocked(true)
                .build());

        bookClubRepository.save(BookClub.builder()
                .name("Mumbai Readers")
                .city("Mumbai")
                .state("Maharashtra")
                .clubType(ClubType.FICTION)
                .isPrivate(false)
                .createdBy(creator)
                .build());

        bookClubRepository.save(BookClub.builder()
                .name("Delhi Bookworms")
                .city("Delhi")
                .state("Delhi")
                .clubType(ClubType.NON_FICTION)
                .isPrivate(true)
                .createdBy(creator)
                .build());
    }

    @Test
    void shouldSearchClubsByCity() {
        Page<BookClub> result = bookClubRepository.searchClubs(
                "Mumbai", null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Mumbai Readers", result.getContent().get(0).getName());
    }

    @Test
    void shouldSearchClubsByType() {
        Page<BookClub> result = bookClubRepository.searchClubs(
                null, null, ClubType.NON_FICTION, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Delhi Bookworms", result.getContent().get(0).getName());
    }

    @Test
    void shouldFilterPrivateClubs() {
        Page<BookClub> result = bookClubRepository.searchClubs(
                null, null, null, false, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().stream().allMatch(club -> !club.isPrivate()));
    }
}