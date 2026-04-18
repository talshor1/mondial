package com.mondial.api.repository;

import com.mondial.api.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findAllByOrderByStartsAtAsc();
    Optional<Game> findByExternalId(Integer externalId);
    void deleteByExternalIdIsNull();

    @Modifying
    @Query(value = "UPDATE games SET status = 'IN_PROGRESS' WHERE status = 'OPEN' AND starts_at <= CURRENT_TIMESTAMP", nativeQuery = true)
    int lockStartedGames();
}
