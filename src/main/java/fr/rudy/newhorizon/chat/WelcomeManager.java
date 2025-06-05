package fr.rudy.newhorizon.chat;

import java.util.UUID;

public class WelcomeManager {
    private UUID lastNewPlayer;
    private long joinTime;

    public void setNewPlayer(UUID uuid) {
        this.lastNewPlayer = uuid;
        this.joinTime = System.currentTimeMillis(); // en millisecondes
    }

    public UUID getLastNewPlayer() {
        return lastNewPlayer;
    }

    public boolean hasNewPlayer() {
        return lastNewPlayer != null;
    }

    public boolean isWithinWindow(long millis) {
        return System.currentTimeMillis() - joinTime <= millis;
    }

    public void clear() {
        this.lastNewPlayer = null;
        this.joinTime = 0;
    }
}
