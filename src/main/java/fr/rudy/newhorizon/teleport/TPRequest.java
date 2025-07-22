/*
 * Decompiled with CFR 0.152.
 */
package fr.rudy.newhorizon.teleport;

import java.util.UUID;

class TPRequest {
    private final UUID senderId;
    private final UUID targetId;

    public TPRequest(UUID senderId, UUID targetId) {
        this.senderId = senderId;
        this.targetId = targetId;
    }

    public UUID getSenderId() {
        return this.senderId;
    }

    public UUID getTargetId() {
        return this.targetId;
    }
}

