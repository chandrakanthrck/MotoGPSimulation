package com.motogp.MotoGP.repository;

import com.motogp.MotoGP.model.Lap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LapRepository extends JpaRepository<Lap, Long> {
}
