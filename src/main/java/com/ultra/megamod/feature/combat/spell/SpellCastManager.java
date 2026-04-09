package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.combat.animation.SpellAnimationPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active spell casting state for players.
 * Charged spells require holding a key for castDuration before release.
 * Channeled spells deliver effects repeatedly while held.
 */
public class SpellCastManager {

    private static final Map<UUID, CastingState> activeCasts = new ConcurrentHashMap<>();

    public record CastingState(String spellId, int startTick, int durationTicks, boolean channeled) {}

    /**
     * Begin casting a spell. For INSTANT spells, executes immediately.
     * For CHARGED/CHANNELED, begins the cast timer.
     */
    public static boolean startCast(ServerPlayer player, String spellId) {
        SpellDefinition spell = SpellRegistry.get(spellId);
        if (spell == null) return false;

        // Check cooldown
        if (SpellExecutor.isOnCooldown(player.getUUID(), spellId)) return false;

        if (spell.castMode() == SpellDefinition.CastMode.INSTANT) {
            // Execute immediately
            return SpellExecutor.cast(player, spell);
        }

        // Calculate cast duration with haste
        float haste = (float) AttributeHelper.getSpellHaste(player);
        int durationTicks = Math.max(1, (int)(spell.castDuration() * 20 / haste));

        activeCasts.put(player.getUUID(), new CastingState(
            spellId,
            player.level().getServer().getTickCount(),
            durationTicks,
            spell.castMode() == SpellDefinition.CastMode.CHANNELED
        ));

        // Broadcast casting animation to nearby clients
        String castAnimId = getCastAnimationId(spell);
        float animSpeed = (float) AttributeHelper.getSpellHaste(player);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SpellAnimationPayload(player.getId(), 0, castAnimId, animSpeed, false));

        // For channeled spells, apply food exhaustion once at channel start.
        // SpellExecutor.cast() skips exhaust for CHANNELED mode to prevent per-tick application.
        if (spell.castMode() == SpellDefinition.CastMode.CHANNELED && spell.exhaust() > 0) {
            player.causeFoodExhaustion(spell.exhaust() * 40f);
        }

        return true;
    }

    /**
     * Called every server tick for each player that is actively casting.
     */
    public static void tickCasting(ServerPlayer player) {
        CastingState state = activeCasts.get(player.getUUID());
        if (state == null) return;

        int currentTick = player.level().getServer().getTickCount();
        int elapsed = currentTick - state.startTick();

        if (state.channeled()) {
            // Channeled: deliver effects periodically
            SpellDefinition spell = SpellRegistry.get(state.spellId());
            if (spell != null && elapsed % 4 == 0) { // deliver every 4 ticks (5x per second)
                SpellExecutor.cast(player, spell);
            }
            // End channel when duration expires
            if (elapsed >= state.durationTicks()) {
                activeCasts.remove(player.getUUID());
                // Stop cast animation
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                        new SpellAnimationPayload(player.getId(), 4, "", 1.0f, false));
            }
        } else {
            // Charged: wait for release or auto-fire when complete
            if (elapsed >= state.durationTicks()) {
                SpellDefinition spell = SpellRegistry.get(state.spellId());
                if (spell != null) {
                    broadcastRelease(player, spell);
                    SpellExecutor.cast(player, spell);
                }
                activeCasts.remove(player.getUUID());
            }
        }
    }

    /**
     * Player released the cast key. For charged spells, fires if enough time passed.
     */
    public static void releaseCast(ServerPlayer player) {
        CastingState state = activeCasts.remove(player.getUUID());
        if (state == null) return;

        if (!state.channeled()) {
            int elapsed = player.level().getServer().getTickCount() - state.startTick();
            // Fire if at least 50% of cast time completed
            if (elapsed >= state.durationTicks() * 0.5) {
                SpellDefinition spell = SpellRegistry.get(state.spellId());
                if (spell != null) {
                    broadcastRelease(player, spell);
                    SpellExecutor.cast(player, spell);
                }
            } else {
                // Didn't cast long enough — just stop the cast animation
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                        new SpellAnimationPayload(player.getId(), 4, "", 1.0f, false));
            }
        }
        // Channeled: stopping early just ends the channel (cooldown already applied)
    }

    /**
     * Cancel any active cast (e.g., player took damage, switched items).
     */
    public static void cancelCast(ServerPlayer player) {
        if (activeCasts.remove(player.getUUID()) != null) {
            // Broadcast stop animation
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new SpellAnimationPayload(player.getId(), 4, "", 1.0f, false));
        }
    }

    /** @deprecated Use cancelCast(ServerPlayer) instead */
    @Deprecated
    public static void cancelCast(UUID playerId) {
        activeCasts.remove(playerId);
    }

    public static boolean isCasting(UUID playerId) {
        return activeCasts.containsKey(playerId);
    }

    public static CastingState getCastingState(UUID playerId) {
        return activeCasts.get(playerId);
    }

    /**
     * Clear all active casts. Called on server shutdown.
     */
    public static void clearAll() {
        activeCasts.clear();
    }

    /**
     * Get cast progress as 0.0-1.0 for HUD rendering.
     */
    public static float getCastProgress(UUID playerId, int currentTick) {
        CastingState state = activeCasts.get(playerId);
        if (state == null) return 0;
        int elapsed = currentTick - state.startTick();
        return Math.min(1.0f, (float) elapsed / state.durationTicks());
    }

    /**
     * Broadcast a release animation and stop the casting animation for a player.
     */
    public static void broadcastRelease(ServerPlayer player, SpellDefinition spell) {
        // Stop casting animation
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SpellAnimationPayload(player.getId(), 3, "", 1.0f, false));

        // Play release animation
        String releaseAnimId = getReleaseAnimationId(spell);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SpellAnimationPayload(player.getId(), 1, releaseAnimId, 1.0f, false));
    }

    /**
     * Determine the casting animation ID — uses SpellVisuals if present, falls back to delivery-based defaults.
     */
    private static String getCastAnimationId(SpellDefinition spell) {
        // Use visuals config first
        if (spell.visuals() != null && spell.visuals().castAnimation() != null) {
            return spell.visuals().castAnimation();
        }
        // Legacy fallback
        if (spell.animationId() != null && !spell.animationId().isEmpty()) {
            return spell.animationId();
        }
        return switch (spell.delivery()) {
            case PROJECTILE -> "one_handed_projectile_charge";
            case BEAM -> "two_handed_channeling";
            case AREA, CLOUD, SPAWN -> "one_handed_area_charge";
            case ARROW -> "archery_pull";
            case MELEE -> "weapon_thrust_charge";
            case TELEPORT -> "one_handed_sky_charge";
            case DIRECT -> switch (spell.school()) {
                case HEALING -> "one_handed_healing_charge";
                case SOUL -> "two_handed_channeling";
                default -> "one_handed_projectile_charge";
            };
        };
    }

    /**
     * Determine the release animation ID — uses SpellVisuals if present.
     */
    private static String getReleaseAnimationId(SpellDefinition spell) {
        if (spell.visuals() != null && spell.visuals().releaseAnimation() != null) {
            return spell.visuals().releaseAnimation();
        }
        return switch (spell.delivery()) {
            case PROJECTILE -> "one_handed_projectile_release";
            case BEAM -> "one_handed_shout_release";
            case AREA, CLOUD, SPAWN -> "one_handed_area_release";
            case ARROW -> "archery_release";
            case MELEE -> "weapon_thrust_full";
            case TELEPORT -> "one_handed_throw_release_instant";
            case DIRECT -> switch (spell.school()) {
                case HEALING -> "one_handed_healing_release";
                default -> "one_handed_projectile_release";
            };
        };
    }

    /**
     * Get the casting sound event path — uses SpellVisuals if present.
     */
    public static String getCastSound(SpellDefinition spell) {
        if (spell.visuals() != null && spell.visuals().castSound() != null) {
            return spell.visuals().castSound();
        }
        return null;
    }

    /**
     * Get the release sound event path — uses SpellVisuals if present.
     */
    public static String getReleaseSound(SpellDefinition spell) {
        if (spell.visuals() != null && spell.visuals().releaseSound() != null) {
            return spell.visuals().releaseSound();
        }
        return null;
    }

    /**
     * Get the impact sound event path — uses SpellVisuals if present.
     */
    public static String getImpactSound(SpellDefinition spell) {
        if (spell.visuals() != null && spell.visuals().impactSound() != null) {
            return spell.visuals().impactSound();
        }
        return null;
    }
}
