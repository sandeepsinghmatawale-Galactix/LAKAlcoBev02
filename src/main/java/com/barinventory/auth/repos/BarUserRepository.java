package com.barinventory.auth.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.barinventory.auth.entities.BarUser;

@Repository
public interface BarUserRepository extends JpaRepository<BarUser, Long> {

    Optional<BarUser> findByUsername(String username);
    @Query("SELECT u.username FROM BarUser u WHERE u.barId = :barId AND u.role = :role")
    Optional<String> findUsernameByBarIdAndRole(@Param("barId") Long barId, @Param("role") String role);
}