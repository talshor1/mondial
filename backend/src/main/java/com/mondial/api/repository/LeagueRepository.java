package com.mondial.api.repository;

import com.mondial.api.model.AppUser;
import com.mondial.api.model.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {
    Optional<League> findByLeagueCode(String leagueCode);
    List<League> findByMembersContaining(AppUser user);
}