package com.mondial.api.repository;

import com.mondial.api.model.AppUser;
import com.mondial.api.model.Bet;
import com.mondial.api.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByUser(AppUser user);
    List<Bet> findByGame(Game game);
    Optional<Bet> findByUserAndGame(AppUser user, Game game);

    @Modifying
    @Query(value = """
        UPDATE bets SET
          points = CASE
            WHEN home_goals = :homeScore AND away_goals = :awayScore THEN 3
            WHEN SIGN(home_goals - away_goals) = SIGN(:homeScore - :awayScore) THEN 1
            ELSE 0
          END,
          status = 'SCORED'
        WHERE game_id = :gameId AND status = 'PENDING'
        """, nativeQuery = true)
    int scoreAllForGame(@Param("gameId") Long gameId,
                        @Param("homeScore") int homeScore,
                        @Param("awayScore") int awayScore);
}
