package com.mondial.api.service;

import com.mondial.api.model.Game;
import com.mondial.api.model.GameStatus;
import com.mondial.api.repository.BetRepository;
import com.mondial.api.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository gameRepository;
    private final BetRepository betRepository;

    public GameService(GameRepository gameRepository, BetRepository betRepository) {
        this.gameRepository = gameRepository;
        this.betRepository = betRepository;
    }

    /**
     * Runs every 60 seconds. Transitions OPEN games to IN_PROGRESS once their start time has passed.
     * This prevents any new bets from being placed on started games.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void lockStartedGames() {
        int count = gameRepository.lockStartedGames();
        if (count > 0) {
            log.info("Locked {} game(s) — betting closed", count);
        }
    }

    /**
     * Marks a game as FINISHED with final scores and bulk-scores all pending bets in one transaction.
     * Scoring rules:
     *   3 pts — exact score
     *   1 pt  — correct outcome (home win / draw / away win)
     *   0 pts — wrong outcome
     */
    @Transactional
    public Game finishGame(Long gameId, int homeScore, int awayScore) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        if (game.getStatus() == GameStatus.FINISHED) {
            log.warn("Game {} is already FINISHED — re-scoring with new scores {}–{}", gameId, homeScore, awayScore);
        }

        game.setHomeScore(homeScore);
        game.setAwayScore(awayScore);
        game.setStatus(GameStatus.FINISHED);
        Game saved = gameRepository.save(game);

        int scored = betRepository.scoreAllForGame(gameId, homeScore, awayScore);
        log.info("Game {} finished {}–{}, scored {} bet(s)", gameId, homeScore, awayScore, scored);

        return saved;
    }
}