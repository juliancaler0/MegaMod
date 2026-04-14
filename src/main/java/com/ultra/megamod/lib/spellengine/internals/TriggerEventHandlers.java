package com.ultra.megamod.lib.spellengine.internals;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.api.event.CombatEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * NeoForge-native bridge for SpellEngine passive-spell triggers (Phase A.4).
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>{@link CombatEvents#PLAYER_DAMAGE_INCOMING} fan-out via {@link LivingIncomingDamageEvent}
 *         (pre-reduction, fires {@code SpellTriggers.onDamageIncoming} / DAMAGE_TAKEN PRE).</li>
 *     <li>{@link CombatEvents#PLAYER_DAMAGE_TAKEN} fan-out via {@link LivingDamageEvent.Post}
 *         (post-reduction, fires {@code SpellTriggers.onDamageTaken} / DAMAGE_TAKEN POST).</li>
 *     <li>{@link CombatEvents#PLAYER_SHIELD_BLOCK} fan-out via {@link LivingShieldBlockEvent}
 *         (fires {@code SpellTriggers.onShieldBlock} / SHIELD_BLOCK) when a shield actually
 *         blocks the incoming attack.</li>
 *     <li>{@link PlayerTickEvent.Post} hook reserved for any tick-based bookkeeping needed by
 *         passive resolution (currently a no-op — per-spell cooldowns, batching, and cooldown
 *         ticking already live in dedicated mixins/managers).</li>
 * </ul>
 *
 * <p>MELEE_IMPACT remains wired by the {@code PlayerEntityEvents} mixin (Phase A.1, hooks
 * {@link net.minecraft.world.entity.player.Player#attack(net.minecraft.world.entity.Entity)}).
 * ARROW_SHOT / ARROW_IMPACT remain wired through {@code RangedWeaponItemMixin} /
 * {@code PersistentProjectileEntityMixin} because arrow context (ArrowExtension, firedBySpell)
 * is only available at those mixin sites. SPELL_CAST is fired from
 * {@code SpellHelper.performSpell} (owned by Phase A.3) via
 * {@link com.ultra.megamod.lib.spellengine.api.spell.event.SpellEvents#SPELL_CAST}; if A.3
 * changes that, this class also exposes {@link #onSpellCast} as a secondary entry point that
 * forwards into the same {@link SpellTriggers} pipeline. EFFECT_TICK, EVASION, and ROLL remain
 * wired by their dedicated sites (status effect, evasion logic, and CombatRoll compat).
 *
 * <p>Fan-out design: instead of calling {@link SpellTriggers} directly, this handler fires
 * the matching {@link CombatEvents} so that every listener on the internal event bus
 * (including {@link SpellTriggers} itself, registered in
 * {@link SpellTriggers#init()}) observes the same events. This replaces the old mixin-level
 * invocations of {@code PLAYER_DAMAGE_*} and {@code PLAYER_SHIELD_BLOCK}, which have been
 * removed from their mixins to avoid double-firing. {@code ENTITY_*} invocations in mixins
 * continue to fire for non-player entities.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public final class TriggerEventHandlers {
    private TriggerEventHandlers() {}

    // --- DAMAGE_TAKEN (PRE / incoming) ---------------------------------

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (!CombatEvents.PLAYER_DAMAGE_INCOMING.isListened()) return;

        var args = new CombatEvents.PlayerDamageTaken.Args(player, event.getSource(), event.getAmount());
        CombatEvents.PLAYER_DAMAGE_INCOMING.invoke(listener -> listener.onPlayerDamageTaken(args));
    }

    // --- DAMAGE_TAKEN (POST / after reduction) -------------------------

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (!CombatEvents.PLAYER_DAMAGE_TAKEN.isListened()) return;

        var args = new CombatEvents.PlayerDamageTaken.Args(player, event.getSource(), event.getNewDamage());
        CombatEvents.PLAYER_DAMAGE_TAKEN.invoke(listener -> listener.onPlayerDamageTaken(args));
    }

    // --- SHIELD_BLOCK --------------------------------------------------

    @SubscribeEvent
    public static void onLivingShieldBlock(LivingShieldBlockEvent event) {
        if (!event.getBlocked()) return; // only when a block actually happens
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (!CombatEvents.PLAYER_SHIELD_BLOCK.isListened()) return;

        var args = new CombatEvents.PlayerShieldBlock.Args(player, event.getDamageSource(), event.getBlockedDamage());
        CombatEvents.PLAYER_SHIELD_BLOCK.invoke(listener -> listener.onShieldBlock(args));
    }

    // --- Tick hook (reserved) -----------------------------------------

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        // Reserved. The Spell.Trigger.Type enum does not currently define an ON_TICK
        // trigger (reference enum: ARROW_SHOT, ARROW_IMPACT, MELEE_IMPACT, SPELL_CAST,
        // SPELL_IMPACT_ANY, SPELL_IMPACT_SPECIFIC, SPELL_AREA_IMPACT, EFFECT_TICK,
        // EVASION, DAMAGE_TAKEN, SHIELD_BLOCK, ROLL). Per-tick passive effects go
        // through EFFECT_TICK (StatusEffect.onApplied/onTick in TickingStatusEffect)
        // or through cooldown/batch ticking in existing mixins
        // (PlayerEntitySpellBatching, SpellCooldownManager). If a future phase adds
        // an ON_TICK trigger type, iterate SpellContainerSource.passiveSpellsOf(player)
        // here, read the per-spell tick_interval config, and call
        // SpellTriggers.onTick(player, spellEntry) (to be added).
    }

    // --- SPELL_CAST passthrough (coordination with Phase A.2) ---------

    /**
     * Secondary entry point for the spell cast controller to notify the trigger
     * pipeline when an active spell is released. A.3's {@code SpellHelper.performSpell}
     * already fires {@link com.ultra.megamod.lib.spellengine.api.spell.event.SpellEvents#SPELL_CAST},
     * which in turn reaches {@link SpellTriggers#onSpellCast}. This helper exists so
     * A.2's cast controller (if it releases outside of {@code performSpell}) can
     * route through the same pipeline without reaching into {@link SpellTriggers}
     * internals.
     *
     * @param player the caster
     * @param spell  the spell that was cast
     * @param targets the targets affected by the cast (may be empty for self-cast /
     *                projectile launch)
     */
    public static void onSpellCast(Player player,
                                   net.minecraft.core.Holder<com.ultra.megamod.lib.spellengine.api.spell.Spell> spell,
                                   java.util.List<net.minecraft.world.entity.Entity> targets) {
        if (player.level().isClientSide()) return;
        SpellTriggers.onSpellCast(player, spell, targets);
    }
}
