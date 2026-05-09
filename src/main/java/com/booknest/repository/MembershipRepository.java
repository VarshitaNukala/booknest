package com.booknest.repository;


import com.booknest.entity.BookClub;
import com.booknest.entity.Membership;
import com.booknest.entity.User;
import com.booknest.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, String> {
    Optional<Membership> findByUserAndBookClub(User user, BookClub bookClub);
    List<Membership> findByBookClubAndStatus(BookClub bookClub, MembershipStatus status);
    List<Membership> findByBookClub(BookClub bookClub);
    boolean existsByUserAndBookClub(User user, BookClub bookClub);
    long countByBookClubAndStatus(BookClub bookClub, MembershipStatus status);
}