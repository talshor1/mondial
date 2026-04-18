package com.mondial.api.service;

import com.mondial.api.model.*;
import com.mondial.api.repository.GameRepository;
import com.mondial.api.repository.SyncMetaRepository;
import com.mondial.api.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.Duration;

@Service
public class WC2026SyncService {

    private static final Logger log = LoggerFactory.getLogger(WC2026SyncService.class);
    private static final String SYNC_KEY = "wc2026_matches";
    private static final Duration SYNC_INTERVAL = Duration.ofHours(2);

    @Value("${wc2026.api.base-url}")
    private String baseUrl;

    @Value("${wc2026.api.key}")
    private String apiKey;

    private final GameRepository gameRepository;
    private final SyncMetaRepository syncMetaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WC2026SyncService(GameRepository gameRepository,
                              SyncMetaRepository syncMetaRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.gameRepository = gameRepository;
        this.syncMetaRepository = syncMetaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Syncs matches from WC2026 API if last sync was more than 2 hours ago.
     * @return true if sync was performed, false if skipped (too recent)
     */
    @Transactional
    public boolean syncIfStale() {
        var meta = syncMetaRepository.findById(SYNC_KEY).orElse(null);
        if (meta != null && meta.getLastSyncedAt() != null) {
            Duration sinceLastSync = Duration.between(meta.getLastSyncedAt(), OffsetDateTime.now());
            if (sinceLastSync.compareTo(SYNC_INTERVAL) < 0) {
                long minutesLeft = SYNC_INTERVAL.minus(sinceLastSync).toMinutes();
                log.info("Skipping WC2026 sync — next sync in ~{} minutes", minutesLeft);
                return false;
            }
        }
        forceSync();
        return true;
    }

    /**
     * Forces a sync regardless of last sync time.
     */
    @Transactional
    public void forceSync() {
        log.info("Syncing matches from WC2026 API...");

        AppUser admin = userRepository.findByEmail("admin@mondial.com").orElseGet(() -> {
            AppUser u = new AppUser();
            u.setEmail("admin@mondial.com");
            u.setPassword(passwordEncoder.encode("Admin1234!"));
            u.setRole(Role.ADMIN);
            return userRepository.save(u);
        });

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/matches"))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("WC2026 API returned status {}: {}", response.statusCode(), response.body());
                return;
            }

            JsonNode matches = objectMapper.readTree(response.body());
            int created = 0, updated = 0, skipped = 0;

            for (JsonNode m : matches) {
                String homeTeam = m.path("home_team").asText(null);
                String awayTeam = m.path("away_team").asText(null);
                String kickoffStr = m.path("kickoff_utc").asText(null);
                int externalId = m.path("id").asInt();

                // Skip knockout matches where teams are not yet known
                if (homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()) {
                    skipped++;
                    continue;
                }

                OffsetDateTime kickoff = OffsetDateTime.parse(kickoffStr);
                String statusStr = m.path("status").asText("scheduled");
                GameStatus gameStatus = "finished".equalsIgnoreCase(statusStr) ? GameStatus.FINISHED
                        : GameStatus.OPEN;

                Integer homeScore = m.path("home_score").isNull() ? null : m.path("home_score").asInt();
                Integer awayScore = m.path("away_score").isNull() ? null : m.path("away_score").asInt();

                // Try to find existing game by externalId
                var existing = gameRepository.findByExternalId(externalId);
                if (existing.isPresent()) {
                    Game g = existing.get();
                    g.setHomeTeam(homeTeam);
                    g.setAwayTeam(awayTeam);
                    g.setStartsAt(kickoff);
                    g.setStatus(gameStatus);
                    g.setHomeScore(homeScore);
                    g.setAwayScore(awayScore);
                    gameRepository.save(g);
                    updated++;
                } else {
                    Game g = new Game();
                    g.setExternalId(externalId);
                    g.setHomeTeam(homeTeam);
                    g.setAwayTeam(awayTeam);
                    g.setStartsAt(kickoff);
                    g.setStatus(gameStatus);
                    g.setHomeScore(homeScore);
                    g.setAwayScore(awayScore);
                    g.setCreatedBy(admin);
                    gameRepository.save(g);
                    created++;
                }
            }

            log.info("WC2026 sync done: {} created, {} updated, {} skipped (no teams yet)", created, updated, skipped);

            // Update sync timestamp
            var meta = syncMetaRepository.findById(SYNC_KEY).orElse(new SyncMeta(SYNC_KEY, null));
            meta.setLastSyncedAt(OffsetDateTime.now());
            syncMetaRepository.save(meta);

        } catch (Exception e) {
            log.error("WC2026 sync failed", e);
        }
    }
}