package com.motogp.MotoGP.repository;

import com.motogp.MotoGP.model.PitStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitStopRepository extends JpaRepository<PitStop, Long> {
}
