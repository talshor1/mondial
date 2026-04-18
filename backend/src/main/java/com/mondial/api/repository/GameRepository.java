package com.mondial.api.repository;

import com.mondial.api.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findAllByOrderByStartsAtAsc();
    Optional<Game> findByExternalId(Integer externalId);
    void deleteByExternalIdIsNull();
}
