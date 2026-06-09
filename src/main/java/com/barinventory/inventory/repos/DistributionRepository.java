package com.barinventory.inventory.repos;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.barinventory.inventory.entities.Distribution;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, Long> {

    Optional<Distribution> findBySessionSessionId(Long sessionId);

    Optional<Distribution> findTopByOrderByDistributionIdDesc();
}