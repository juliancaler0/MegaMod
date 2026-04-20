package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.feature.combat.animation.api.fx.ParticlePlacement;
import com.ultra.megamod.feature.combat.animation.api.fx.TrailAppearance;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.List;

/**
 * Data carrier for a scheduled weapon-trail particle spawn. Matches
 * {@code SlashParticleUtil.ScheduledSpawnArgs} in source BetterCombat.
 *
 * <p>Intended usage: the mixin-driven attack path calls
 * {@link PlayerAttackAnimatable#playAttackParticles} on upswing to store this record;
 * {@code updateAnimationsOnTick} later checks {@code time == player.tickCount} and dispatches
 * the particle spawn. The actual spawn implementation (ported SlashParticleEffect +
 * TrailParticles config loader) is a future polish task — this record exists so the
 * network/data path from server to client is wired and ready for the visual piece.
 */
public record ScheduledSlashParticles(
        AbstractClientPlayer player,
        boolean isOffHand,
        float weaponRange,
        List<ParticlePlacement> particles,
        TrailAppearance appearance,
        int time) {
}
