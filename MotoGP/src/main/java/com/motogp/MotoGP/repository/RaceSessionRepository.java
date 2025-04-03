package com.motogp.MotoGP.repository;

import com.motogp.MotoGP.model.RaceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceSessionRepository extends JpaRepository<RaceSession, Long> {
}
