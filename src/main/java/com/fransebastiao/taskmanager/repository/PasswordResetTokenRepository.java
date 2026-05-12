package com.fransebastiao.taskmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.user.PasswordResetToken;
import com.fransebastiao.taskmanager.domain.user.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>{
    Optional<PasswordResetToken> findByHashedToken(String hashedToken);
    Optional<PasswordResetToken> findByUser(User user);

    @Modifying
    @Query("""
        DELETE FROM PasswordResetToken pt 
        WHERE pt.user = :user
    """)
    void deleteByUser(@Param("user") User user);
}