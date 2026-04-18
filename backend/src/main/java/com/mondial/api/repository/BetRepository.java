package com.mondial.api.repository;

import com.mondial.api.model.AppUser;
import com.mondial.api.model.Bet;
import com.mondial.api.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByUser(AppUser user);
    List<Bet> findByGame(Game game);
    Optional<Bet> findByUserAndGame(AppUser user, Game game);
}