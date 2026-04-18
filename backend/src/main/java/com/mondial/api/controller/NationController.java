package com.mondial.api.controller;

import com.mondial.api.dto.BetResponse;
import com.mondial.api.dto.CreateBetRequest;
import com.mondial.api.dto.GameResponse;
import com.mondial.api.model.Bet;
import com.mondial.api.model.Game;
import com.mondial.api.model.GameStatus;
import com.mondial.api.repository.BetRepository;
import com.mondial.api.repository.GameRepository;
import com.mondial.api.repository.UserRepository;
import com.mondial.api.service.WC2026SyncService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class NationController {

    private final GameRepository gameRepository;
    private final BetRepository betRepository;
    private final UserRepository userRepository;

    private final WC2026SyncService syncService;

    public NationController(GameRepository gameRepository, BetRepository betRepository,
                             UserRepository userRepository, WC2026SyncService syncService) {
        this.gameRepository = gameRepository;
        this.betRepository = betRepository;
        this.userRepository = userRepository;
        this.syncService = syncService;
    }

    private GameResponse toGameResponse(Game g) {
        return new GameResponse(g.getId(), g.getHomeTeam(), g.getAwayTeam(),
                g.getStartsAt(), g.getStatus().name(), g.getHomeScore(), g.getAwayScore());
    }

    // ── List all games ────────────────────────────────────────────────────────
    @GetMapping
    public List<GameResponse> list() {
        return gameRepository.findAllByOrderByStartsAtAsc().stream()
                .map(this::toGameResponse).toList();
    }

    // ── Get my bets (map gameId → bet) ────────────────────────────────────────
    @GetMapping("/my-bets")
    @Transactional
    public List<BetResponse> myBets(Authentication auth) {
        var user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return betRepository.findByUser(user).stream()
                .map(b -> new BetResponse(b.getId(), b.getGame().getId(), b.getHomeGoals(), b.getAwayGoals(), b.getPoints()))
                .toList();
    }

    // ── Place or update a bet ─────────────────────────────────────────────────
    @PostMapping("/{id}/bet")
    @Transactional
    public ResponseEntity<BetResponse> placeBet(@PathVariable Long id,
                                                 @RequestBody CreateBetRequest req,
                                                 Authentication auth) {
        var game = gameRepository.findById(id).orElse(null);
        if (game == null) return ResponseEntity.notFound().build();
        if (game.getStatus() != GameStatus.OPEN) return ResponseEntity.badRequest().build();
        if (game.getStartsAt().isBefore(OffsetDateTime.now())) return ResponseEntity.badRequest().build();

        var user = userRepository.findByEmail(auth.getName()).orElseThrow();
        var bet = betRepository.findByUserAndGame(user, game).orElse(new Bet());
        bet.setUser(user);
        bet.setGame(game);
        bet.setHomeGoals(req.homeGoals());
        bet.setAwayGoals(req.awayGoals());
        bet.setPlacedAt(OffsetDateTime.now());
        betRepository.save(bet);

        return ResponseEntity.ok(new BetResponse(bet.getId(), game.getId(), bet.getHomeGoals(), bet.getAwayGoals(), null));
    }

    // ── Admin: set result and calculate points ────────────────────────────────
    @PutMapping("/{id}/result")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<GameResponse> setResult(@PathVariable Long id,
                                                   @RequestBody Map<String, Integer> body) {
        var game = gameRepository.findById(id).orElse(null);
        if (game == null) return ResponseEntity.notFound().build();

        int home = body.get("homeScore");
        int away = body.get("awayScore");
        game.setHomeScore(home);
        game.setAwayScore(away);
        game.setStatus(GameStatus.FINISHED);
        gameRepository.save(game);

        // Determine actual outcome: H / D / A
        String actual = home > away ? "H" : (home < away ? "A" : "D");

        betRepository.findByGame(game).forEach(bet -> {
            String predicted = bet.getHomeGoals() > bet.getAwayGoals() ? "H"
                    : (bet.getHomeGoals() < bet.getAwayGoals() ? "A" : "D");
            if (bet.getHomeGoals() == home && bet.getAwayGoals() == away) {
                bet.setPoints(3); // exact score
            } else if (predicted.equals(actual)) {
                bet.setPoints(1); // correct outcome
            } else {
                bet.setPoints(0);
            }
            betRepository.save(bet);
        });

        return ResponseEntity.ok(toGameResponse(game));
    }

    // ── Admin: force sync from WC2026 API ─────────────────────────────────────
    @PostMapping("/sync")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> syncGames() {
        syncService.forceSync();
        return ResponseEntity.ok(Map.of("status", "sync complete"));
    }

    // ── Admin: create a game ──────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<GameResponse> create(@RequestBody Map<String, String> body, Authentication auth) {
        var user = userRepository.findByEmail(auth.getName()).orElseThrow();
        var game = new Game();
        game.setHomeTeam(body.get("homeTeam"));
        game.setAwayTeam(body.get("awayTeam"));
        game.setStartsAt(OffsetDateTime.parse(body.get("startsAt")));
        game.setCreatedBy(user);
        gameRepository.save(game);
        return ResponseEntity.ok(toGameResponse(game));
    }
}