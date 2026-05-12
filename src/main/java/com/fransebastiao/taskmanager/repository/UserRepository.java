package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.UserDto;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findById(UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.privileges WHERE u.email = :email")
    Optional<User> findByEmailWithRolesAndPrivileges(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.privileges WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPrivileges(@Param("id") UUID id);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    boolean existsByEmail(String email);

    @Query("""
        SELECT new com.fransebastiao.taskmanager.dto.response.UserDto(
            u.id,
            u.name,
            u.email
        )
        FROM User u
    """)
    List<UserDto> findUsers();
}
