package com.mondial.api.controller;

import com.mondial.api.dto.LeagueResponse;
import com.mondial.api.model.League;
import com.mondial.api.repository.BetRepository;
import com.mondial.api.repository.LeagueRepository;
import com.mondial.api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueRepository leagueRepository;
    private final UserRepository userRepository;
    private final BetRepository betRepository;

    public LeagueController(LeagueRepository leagueRepository, UserRepository userRepository, BetRepository betRepository) {
        this.leagueRepository = leagueRepository;
        this.userRepository = userRepository;
        this.betRepository = betRepository;
    }

    private LeagueResponse toResponse(League league) {
        var members = league.getMembers().stream()
                .map(u -> {
                    int points = betRepository.findByUser(u).stream()
                            .mapToInt(b -> b.getPoints() != null ? b.getPoints() : 0)
                            .sum();
                    return new LeagueResponse.MemberInfo(u.getId(), u.getEmail(), u.getTeamName(), points);
                })
                .sorted((a, b) -> Integer.compare(b.points(), a.points()))
                .toList();
        return new LeagueResponse(league.getId(), league.getName(), league.getLeagueCode(), members);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<LeagueResponse> create(Authentication auth, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) return ResponseEntity.badRequest().build();

        var user = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        var league = new League();
        league.setName(name.trim());
        league.setLeagueCode(code);
        league.setCreatedBy(user);
        league.getMembers().add(user);

        leagueRepository.save(league);
        return ResponseEntity.ok(toResponse(league));
    }

    @PostMapping("/join")
    @Transactional
    public ResponseEntity<LeagueResponse> join(Authentication auth, @RequestBody Map<String, String> body) {
        String code = body.get("leagueCode");
        if (code == null || code.isBlank()) return ResponseEntity.badRequest().build();

        var user = userRepository.findByEmail(auth.getName()).orElseThrow();
        var league = leagueRepository.findByLeagueCode(code.toUpperCase())
                .orElse(null);
        if (league == null) return ResponseEntity.notFound().build();

        boolean alreadyMember = league.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
        if (!alreadyMember) {
            league.getMembers().add(user);
            leagueRepository.save(league);
        }
        return ResponseEntity.ok(toResponse(league));
    }

    @GetMapping("/mine")
    @Transactional
    public ResponseEntity<List<LeagueResponse>> mine(Authentication auth) {
        var user = userRepository.findByEmail(auth.getName()).orElseThrow();
        var leagues = leagueRepository.findByMembersContaining(user)
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(leagues);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<LeagueResponse> get(@PathVariable Long id) {
        return leagueRepository.findById(id)
                .map(l -> ResponseEntity.ok(toResponse(l)))
                .orElse(ResponseEntity.notFound().build());
    }
}