package com.ultra.megamod.feature.combat;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.combat.animation.BetterCombatHandler;
import com.ultra.megamod.feature.combat.animation.PlayerComboTracker;
import com.ultra.megamod.feature.combat.items.EquipmentSetManager;
import com.ultra.megamod.feature.combat.spell.SpellCastManager;
import com.ultra.megamod.feature.combat.spell.SpellCastSyncPayload;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellExecutor;
import com.ultra.megamod.feature.combat.spell.SpellEffects;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side event handler for the combat overhaul.
 * Ticks SpellCastManager (charged/channeled spells) and EquipmentSetManager (armor set bonuses)
 * every player tick.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class CombatEventHandler {

    /** Tracks players who were casting last tick so we can send a clear packet when they stop. */
    private static final Set<UUID> wasCasting = ConcurrentHashMap.newKeySet();

    /** Modifier ID for the casting speed reduction applied while casting charged/channeled spells. */
    private static final Identifier CASTING_SLOW_ID = Identifier.fromNamespaceAndPath("megamod", "casting_speed_reduction");

    /** 50% movement speed reduction while casting (matches SpellEngine default). */
    private static final double CASTING_SPEED_PENALTY = -0.5;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        // Tick active spell casts (charged/channeled)
        SpellCastManager.tickCasting(player);

        // Sync spell cast bar to client
        UUID playerId = player.getUUID();
        boolean isCasting = SpellCastManager.isCasting(playerId);

        if (isCasting) {
            SpellCastManager.CastingState state = SpellCastManager.getCastingState(playerId);
            if (state != null) {
                SpellDefinition spell = SpellRegistry.get(state.spellId());
                String spellName = spell != null ? spell.name() : state.spellId();
                int schoolColor = spell != null ? spell.school().color : 0xFFFFFFFF;
                int currentTick = player.level().getServer().getTickCount();
                float progress = SpellCastManager.getCastProgress(playerId, currentTick);

                PacketDistributor.sendToPlayer(player,
                    new SpellCastSyncPayload(true, spellName, progress, schoolColor));

                // Broadcast cast state to all players tracking this player (party members, nearby players)
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new com.ultra.megamod.feature.combat.spell.NearbyPlayerCastPayload(
                        playerId, true, spellName, progress, schoolColor));

                wasCasting.add(playerId);

                // Apply casting speed reduction (50% slower) while casting charged/channeled spells
                if (!AttributeHelper.hasModifier(player, Attributes.MOVEMENT_SPEED, CASTING_SLOW_ID)) {
                    AttributeHelper.addModifier(player, Attributes.MOVEMENT_SPEED, CASTING_SLOW_ID,
                        CASTING_SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                }

                // Spawn wind-up particles around caster every 4 ticks during charged/channeled spells
                if (player.tickCount % 4 == 0 && spell != null && player.level() instanceof ServerLevel castLevel) {
                    var particle = getSchoolCastParticle(spell.school());
                    double px = player.getX(), py = player.getY() + 1.2, pz = player.getZ();
                    // Ring of particles at shoulder height
                    for (int i = 0; i < 3; i++) {
                        double angle = player.getRandom().nextDouble() * Math.PI * 2;
                        double radius = 0.6 + player.getRandom().nextDouble() * 0.3;
                        castLevel.sendParticles(particle,
                                px + Math.cos(angle) * radius, py + player.getRandom().nextDouble() * 0.4,
                                pz + Math.sin(angle) * radius,
                                1, 0, 0.02, 0, 0.01);
                    }
                }
            }
        } else if (wasCasting.remove(playerId)) {
            // Player just stopped casting — send a clear packet
            PacketDistributor.sendToPlayer(player,
                new SpellCastSyncPayload(false, "", 0f, 0xFFFFFFFF));

            // Broadcast cast stop to tracking players
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new com.ultra.megamod.feature.combat.spell.NearbyPlayerCastPayload(
                    playerId, false, "", 0f, 0xFFFFFFFF));

            // Remove casting speed reduction
            AttributeHelper.removeModifier(player, Attributes.MOVEMENT_SPEED, CASTING_SLOW_ID);
        }

        // Tick equipment set bonuses (every 20 ticks = 1 second)
        if (player.tickCount % 20 == 0) {
            try {
                EquipmentSetManager.tickPlayer(player);
            } catch (Exception ignored) {
                // EquipmentSetManager may not be initialized yet
            }

            // Clean up passive trigger cooldowns
            com.ultra.megamod.feature.combat.passive.PassiveTriggerManager.cleanupCooldowns(
                    player.level().getGameTime());
        }

        // Spawn visual particles for active spell effects (every 10 ticks)
        if (player.tickCount % 10 == 0 && player.level() instanceof ServerLevel serverLevel) {
            spawnEffectParticles(player, serverLevel);
        }
    }

    /**
     * Spawns school-themed particles around players with active spell effects.
     * Gives visual feedback that a buff/debuff is active.
     */
    private static net.minecraft.core.particles.ParticleOptions getSchoolCastParticle(com.ultra.megamod.feature.combat.spell.SpellSchool school) {
        if (school == null) return net.minecraft.core.particles.ParticleTypes.ENCHANT;
        return switch (school) {
            case FIRE -> net.minecraft.core.particles.ParticleTypes.FLAME;
            case FROST -> net.minecraft.core.particles.ParticleTypes.SNOWFLAKE;
            case ARCANE -> net.minecraft.core.particles.ParticleTypes.ENCHANT;
            case HEALING -> net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER;
            case LIGHTNING -> net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK;
            case SOUL -> net.minecraft.core.particles.ParticleTypes.SOUL;
            case PHYSICAL_MELEE -> net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK;
            case PHYSICAL_RANGED -> net.minecraft.core.particles.ParticleTypes.CRIT;
        };
    }

    private static void spawnEffectParticles(ServerPlayer player, ServerLevel level) {
        double x = player.getX(), y = player.getY() + 1.0, z = player.getZ();

        if (player.hasEffect(SpellEffects.FROZEN)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                    x, y, z, 4, 0.3, 0.5, 0.3, 0.01);
        }
        if (player.hasEffect(SpellEffects.ARCANE_CHARGE)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    x, y + 0.5, z, 3, 0.2, 0.3, 0.2, 0.5);
        }
        if (player.hasEffect(SpellEffects.FROST_SHIELD)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                    x, y + 0.5, z, 2, 0.4, 0.6, 0.4, 0.0);
        }
        if (player.hasEffect(SpellEffects.DIVINE_PROTECTION)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    x, y, z, 2, 0.3, 0.5, 0.3, 0.02);
        }
        if (player.hasEffect(SpellEffects.BATTLE_BANNER)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    x, y + 0.3, z, 1, 0.3, 0.3, 0.3, 0.0);
        }
        if (player.hasEffect(SpellEffects.SLICE_AND_DICE)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,
                    x, y + 0.5, z, 2, 0.2, 0.3, 0.2, 0.1);
        }
        if (player.hasEffect(SpellEffects.STEALTH)) {
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    x, y, z, 1, 0.3, 0.5, 0.3, 0.0);
        }
    }

    // ─── Player disconnect cleanup ─────────────────────────────────────

    /**
     * Clean up all per-player combat state when a player disconnects.
     * Prevents memory leaks from static maps retaining data for departed players.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();

        // Spell system: cancel active casts, clear cooldowns
        SpellCastManager.cancelCast(id);
        SpellExecutor.clearCooldowns(id);

        // Combo tracker: remove combo state
        PlayerComboTracker.resetCombo(id);

        // Equipment set bonuses: remove tracked tiers
        EquipmentSetManager.onPlayerLogout(player);

        // Casting HUD tracking
        wasCasting.remove(id);

        // Melee swing slow tracking
        BetterCombatHandler.removeSwingSlow(id);

        // Combat attack context (should already be consumed, but clean up just in case)
        BetterCombatHandler.CombatAttackContext.clear(id);
    }

    // ─── Static state cleanup ──────────────────────────────────────────

    /**
     * Clear all combat static state. Called on server shutdown to prevent
     * stale data persisting across singleplayer world reloads.
     */
    public static void clearAll() {
        wasCasting.clear();
    }
}
