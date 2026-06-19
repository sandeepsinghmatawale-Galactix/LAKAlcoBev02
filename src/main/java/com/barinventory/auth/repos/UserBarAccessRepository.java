package com.barinventory.auth.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barinventory.auth.entities.UserBarAccess;

public interface UserBarAccessRepository extends JpaRepository<UserBarAccess, Long> {

	List<UserBarAccess> findByUserIdAndActiveTrue(Long userId);

	Optional<UserBarAccess> findByUserIdAndBarIdAndActiveTrue(Long userId, Long barId);

	boolean existsByUserIdAndBarId(Long userId, Long barId);

}