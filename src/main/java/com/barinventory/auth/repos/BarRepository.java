package com.barinventory.auth.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barinventory.auth.entities.Bar;

public interface BarRepository extends JpaRepository<Bar, Long> {

}
