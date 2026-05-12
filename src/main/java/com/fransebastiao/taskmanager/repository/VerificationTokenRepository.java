package com.fransebastiao.taskmanager.repository;

import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.domain.user.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByHashedToken(String hashedToken);
    Optional<VerificationToken> findByUser(User user);

    @Modifying
    @Query("""
        DELETE FROM VerificationToken vt 
        WHERE vt.user = :user
    """)
    void deleteByUser(@Param("user") User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM VerificationToken vt 
        WHERE vt.expiresAt < :now
    """)
    int purgeOldTokens(@Param("now") Instant now);
}
