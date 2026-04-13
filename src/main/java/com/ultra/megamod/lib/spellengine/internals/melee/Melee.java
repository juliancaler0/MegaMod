package com.ultra.megamod.lib.spellengine.internals.melee;

import com.google.common.base.Suppliers;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.fx.PlayerAnimation;
import com.ultra.megamod.lib.spellengine.api.spell.fx.Sound;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.SpellModifiers;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.internals.target.EntityRelations;
import com.ultra.megamod.lib.spellengine.internals.target.SpellTarget;
import com.ultra.megamod.mixin.spellengine.entity.LivingEntityAccessor;
import com.ultra.megamod.lib.spellengine.utils.AnimationHelper;
import com.ultra.megamod.lib.spellengine.utils.AttributeModifierUtil;
import com.ultra.megamod.lib.spellengine.utils.SoundHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class Melee {

    public record Attack(
            int duration,
            int delay,
            int additional_strikes,
            int additional_strike_delay,
            boolean additional_hits_on_same_target,
            float speed,
            float forward_momentum,
            boolean allow_momentum_airborne,
            float movement_speed,
            float movement_slip,
            float range,
            Spell.Delivery.Melee.HitBox hitbox,
            PlayerAnimation animation,
            @Nullable AttackContext context
    ) {
    }
    /**
     * Context object that tracks the origin of a Melee.Attack
     * Allows mapping back from execution model to data model
     */
    public record AttackContext(
        Identifier spellId,
        String attackId
    ) {
        public static final AttackContext EMPTY = new AttackContext(Identifier.fromNamespaceAndPath("megamod", "empty"), "empty");
        /**
         * Create context for a specific attack
         */
        public static AttackContext of(Identifier spellId, String attackId) {
            return new AttackContext(spellId, attackId);
        }
    }

    public static class ActiveAttack {
        public final Attack attack;
        public final int createdAt;
        public final Item weapon;
        public final Set<Integer> hitEntityIds = new HashSet<>();
        private final ArrayList<Integer> hitTicks;

        public ActiveAttack(Attack attack, int createdAt, Item weapon) {
            this.attack = attack;
            this.createdAt = createdAt;
            this.weapon = weapon;
            var ticks = new ArrayList<Integer>();
            var firstHit = createdAt + attack.delay;
            ticks.add(firstHit);
            for (int i = 1; i <= attack.additional_strikes; i++) {
                ticks.add(firstHit + (i * attack.additional_strike_delay));
            }
            this.hitTicks = ticks;
        }

        public boolean isFinished(int currentTick) {
            return currentTick >= (createdAt + attack.duration) && currentTick >= hitTicks.getLast();
        }

        public boolean isDue(int currentTick) {
            return hitTicks.contains(currentTick);
        }
    }


    public record CombinedAttacks(
            List<Spell.Delivery.Melee.Attack> attacks,
            List<Spell.Modifier> spellModifiers
    ) {}

    public static CombinedAttacks allAttacksOf(Player caster, List<Spell.Delivery.Melee.Attack> meleeDataAttacks,
                                                                 Holder<Spell> spellEntry) {
        var attacks = new ArrayList<>(meleeDataAttacks);
        var modifiers = SpellModifiers.of(caster, spellEntry);
        for (var modifier: modifiers) {
            if (modifier.melee_attacks != null) {
                for (var attack : modifier.melee_attacks) {
                    attacks.add(attack);
                }
            }
        }
        return new CombinedAttacks(attacks, modifiers);
    }

    /**
     * Server-side: Map spell melee configuration to resolved MeleeAttack list
     * This flattens and resolves all server-side calculations (haste, etc.)
     */
    public static List<Attack> createMeleeAttacks(ServerPlayer caster, List<Spell.Delivery.Melee.Attack> meleeDataAttacks,
                                                  Holder<Spell> spellEntry) {
        var attacks = new ArrayList<Attack>();
        var attackSpeedMultiplier = AttributeModifierUtil.multipliersOf(Attributes.ATTACK_SPEED, caster);
        var spellId = spellEntry.unwrapKey().get().identifier();
        var allAttacks = allAttacksOf(caster, meleeDataAttacks, spellEntry);
        for (var attack : allAttacks.attacks()) {
            // Calculate haste-affected duration
            var meleeAttack = convert(caster, spellId, attack, attackSpeedMultiplier, allAttacks.spellModifiers());
            attacks.add(meleeAttack);
        }
        return attacks;
    }

    private static Attack convert(ServerPlayer caster, Identifier spellId, Spell.Delivery.Melee.Attack attack, double attackSpeedMultiplier, List<Spell.Modifier> spellModifiers) {
        var speed = (float) (attack.attack_speed_multiplier * attackSpeedMultiplier);
        float duration = attack.duration > 0
                // `getCurrentItemAttackStrengthDelay` returns the attack cooldown in ticks
                ? attack.duration
                : Math.max(caster.getCurrentItemAttackStrengthDelay() * (1F / speed), 1);
        float delay = duration * attack.delay;
        var spell = SpellRegistry.from(caster.level()).get(spellId);
        var range = spell.isPresent() ? SpellHelper.getRange(caster, spell.get()) : (float)caster.entityInteractionRange();

        var momentumBonus = 0F;
        var slipBonus = 0F;
        for (var modifier : spellModifiers) {
            momentumBonus += modifier.melee_momentum_add;
            slipBonus += modifier.melee_slipperiness_add;
        }

        // Create resolved MeleeAttack with all calculations done
        return new Attack(
                Math.round(duration),
                Math.round(delay),
                attack.additional_strikes,
                Math.max(Math.round(duration * attack.additional_strike_delay), 1),
                attack.additional_hits_on_same_target,
                speed,
                attack.forward_momentum + momentumBonus,
                attack.allow_momentum_airborne,
                attack.movement_speed,
                attack.movement_slipperiness + slipBonus,
                range,
                attack.hitbox,
                attack.animation,
                new AttackContext(spellId, attack.id)
        );
    }

    @Nullable public static Spell.Delivery.Melee.Attack resolveAttackData(Player attacker, Level world, @Nullable AttackContext context) {
        if (context == null) {
            return null;
        }
        return resolveAttackData(attacker, world, context.spellId(), context.attackId()).attack;
    }

    public record ResolutionResult(
            Holder<Spell> spell,
            Spell.Delivery.Melee melee,
            Spell.Delivery.Melee.Attack attack
    ) {}
    @Nullable public static ResolutionResult resolveAttackData(Player attacker, Level world, Identifier spellId, String attackId) {
        var spellEntry = SpellRegistry.from(world).get(spellId).orElse(null);
        if (spellEntry == null) {
            return null;
        }

        var spell = spellEntry.value();
        if (spell.deliver.type == Spell.Delivery.Type.MELEE && spell.deliver.melee != null) {
            var allAttacks = allAttacksOf(attacker, spell.deliver.melee.attacks, spellEntry);
            for (var attack : allAttacks.attacks()) {
                if (attack.id.equals(attackId)) {
                    return new ResolutionResult(spellEntry, spell.deliver.melee, attack);
                }
            }
        }

        return null;
    }

    public static List<Integer> detectTargets(Player player, Attack attack) {
        var hitbox = attack.hitbox();
        var range = attack.range();
        var hitboxSize = new Vec3(hitbox.width * range, hitbox.height * range, hitbox.length * range);
        var result = TargetFinder.findAttackTargetResult(player, null, hitboxSize, hitbox.arc, range, hitbox.roll);

        return result.entities.stream().map(Entity::getId).toList();
    }

    private static final Supplier<Boolean> REPLAY = Suppliers.memoize(() -> net.neoforged.fml.ModList.get().isLoaded("replaymod"));

    public static void broadcastAttackFx(ServerPlayer player, AttackContext attackContext) {
        var world = player.level();
        var attackData = resolveAttackData(player, world, attackContext);
        if (attackData != null) {
            // Saving the attack on server side - mainly for the slipperiness
            var attackSpeedMultiplier = AttributeModifierUtil.multipliersOf(Attributes.ATTACK_SPEED, player);
            var attack = convert(player, attackContext.spellId(), attackData, attackSpeedMultiplier, List.of());
            ((SpellCasterEntity) player).setMeleeSkillAttack(new ActiveAttack(attack, player.tickCount, player.getMainHandItem().getItem()));
            // Sending fx to clients - animation, sound, particles
            List<ServerPlayer> trackers = player.level() instanceof ServerLevel sl ? sl.getChunkSource().chunkMap.getPlayers(player.chunkPosition(), false) : java.util.List.of();
            float speed = (float) (attackData.attack_speed_multiplier * AttributeModifierUtil.multipliersOf(Attributes.ATTACK_SPEED, player));
            if (REPLAY.get()) {
                AnimationHelper.sendAnimation(player, trackers, SpellCast.Animation.RELEASE, attackData.animation, speed);
            } else {
                AnimationHelper.sendAnimationExcluding(player, trackers, SpellCast.Animation.RELEASE, attackData.animation, speed);
            }
            SoundHelper.playSound(player.level(), player, attackData.swing_sound);
            ParticleHelper.sendBatches(player, attackData.particles, 1, trackers);
        }
    }

    private static final Identifier DAMAGE_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "melee_attack");
    public static void performAttackAgainstTargets(ServerPlayer player, AttackContext context, int[] targetIds) {
        var world = player.level();
        var focusMode = focusMode();
        var attributeInstance = player.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeModifier appliedDamageModifier = null;
        try {
            var lastAttackTime = ((LivingEntityAccessor)player).spellEngine_getLastAttackedTicks();
            var targets = new ArrayList<Entity>();
            var resolvedContext = resolveAttackData(player, world, context.spellId, context.attackId);
            var spellEntry = resolvedContext.spell();
            ((SpellCasterEntity)player).setActiveMeleeSkill(spellEntry);
            var attack = resolvedContext.attack();
            Sound impactSound = null;
            int impactSoundLimit = 0;
            var modifiers = SpellModifiers.of(player, spellEntry);
            var damageMultiplierBase = 0F;
            for (var modifier : modifiers) {
                damageMultiplierBase += modifier.melee_damage_multiplier;
            }
            if (attack != null && attributeInstance != null) {
                var damageModifierAmount = attack.damage_bonus + damageMultiplierBase;
                if (damageModifierAmount != 0) {
                    appliedDamageModifier = new AttributeModifier(DAMAGE_MODIFIER_ID, damageModifierAmount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
                    attributeInstance.addTransientModifier(appliedDamageModifier);
                }
                impactSound = attack.impact_sound;
                impactSoundLimit = attack.impact_sound_cap > 0 ? attack.impact_sound_cap : 999;
            }
            var attackRange = spellEntry != null ? SpellHelper.getRange(player, spellEntry) : (float)player.entityInteractionRange();

            for (int targetId : targetIds) {
                var target = world.getEntity(targetId);
                if (target != null && target.isAttackable()) {
                    if (!EntityRelations.actionAllowed(
                            focusMode, SpellTarget.Intent.HARMFUL,
                            player, target)) {
                        continue;
                    }

                    var distanceGuard = (attackRange + largesSideLength(target.getBoundingBox())) * 1.2F; // Adding some tolerance
                    if (player.distanceToSqr(target) > (distanceGuard * distanceGuard) ) {
                        continue;
                    }

                    var invulnerableTime = target.invulnerableTime;
                    target.invulnerableTime = 0;
                    ((LivingEntityAccessor)player).spellEngine_setLastAttackedTicks(100);
                    player.attack(target);
                    if (impactSound != null && impactSoundLimit > 0) {
                        SoundHelper.playSound(target.level(), target, impactSound);
                        impactSoundLimit -= 1;
                    }
                    targets.add(target);
                    target.invulnerableTime = invulnerableTime;
                }
            }

            if (!targets.isEmpty()) {
                var impactContext = new SpellHelper.ImpactContext()
                        .position(player.position());
                SpellHelper.meleeImpact(player, targets, spellEntry, impactContext);
            }
            ((LivingEntityAccessor)player).spellEngine_setLastAttackedTicks(lastAttackTime);
        } catch (Exception e) {
            System.err.println("Failed to perform melee attack: " + e.getMessage());
        }
        if (appliedDamageModifier != null) {
            attributeInstance.removeModifier(appliedDamageModifier);
        }
        ((SpellCasterEntity)player).setActiveMeleeSkill(null);
    }

    private static SpellTarget.FocusMode focusMode() {
        return SpellEngineMod.config.melee_skills_area_focus_mode ? SpellTarget.FocusMode.AREA : SpellTarget.FocusMode.DIRECT;
    }

    private static float largesSideLength(AABB boundingBox) {
        double x = boundingBox.getXsize();
        double y = boundingBox.getYsize();
        double z = boundingBox.getZsize();
        return Math.max((float)x, Math.max((float)y, (float)z));
    }
}
