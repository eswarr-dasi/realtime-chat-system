package com.eswarr.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.Instant;

/**
 * Tracks a user's live presence state.
 * Stored in Redis (not the DB) since presence is ephemeral/high-frequency.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresence implements Serializable {

    private String userId;
    private String userName;
    private String roomId;
    private Status status;
    private Instant lastSeenAt;
    private String serverInstanceId; // which server instance holds this connection

    public enum Status {
        ONLINE, OFFLINE, TYPING, AWAY
    }

    public boolean isStale(long timeoutSeconds) {
        return lastSeenAt == null ||
            Instant.now().isAfter(lastSeenAt.plusSeconds(timeoutSeconds));
    }
}
