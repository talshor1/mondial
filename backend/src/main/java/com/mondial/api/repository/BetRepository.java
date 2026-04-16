package com.mondial.api.repository;

import com.mondial.api.model.Bet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByUserEmailOrderByPlacedAtDesc(String email);
}

