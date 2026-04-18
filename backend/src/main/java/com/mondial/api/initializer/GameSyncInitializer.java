package com.mondial.api.initializer;

import com.mondial.api.repository.GameRepository;
import com.mondial.api.service.WC2026SyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * On startup: sync WC2026 matches if last sync was more than 2 hours ago.
 * Runs after NationDataInitializer (order 2).
 */
@Component
@Order(2)
public class GameSyncInitializer implements CommandLineRunner {

    private final WC2026SyncService syncService;
    private final GameRepository gameRepository;

    public GameSyncInitializer(WC2026SyncService syncService, GameRepository gameRepository) {
        this.syncService = syncService;
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // Remove any games seeded manually (no externalId) before syncing from API
        gameRepository.deleteByExternalIdIsNull();
        syncService.syncIfStale();
    }
}