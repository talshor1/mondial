package com.mondial.api.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sync_meta")
public class SyncMeta {

    @Id
    @Column(name = "sync_key", length = 64)
    private String syncKey;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    public SyncMeta() {}

    public SyncMeta(String syncKey, OffsetDateTime lastSyncedAt) {
        this.syncKey = syncKey;
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getSyncKey() { return syncKey; }
    public void setSyncKey(String syncKey) { this.syncKey = syncKey; }
    public OffsetDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(OffsetDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}