package com.ultra.megamod.feature.combat.animation.client;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Record for comparing pose identity to avoid redundant animation changes.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.animation.PoseData).
 */
public record PoseData(UUID uuid, boolean isMirrored) {
    public static PoseData from(String animationId, boolean isMirrored) {
        UUID uuid = null;
        if (animationId != null) {
            uuid = UUID.nameUUIDFromBytes(animationId.getBytes(StandardCharsets.UTF_8));
        }
        return new PoseData(uuid, isMirrored);
    }
}
