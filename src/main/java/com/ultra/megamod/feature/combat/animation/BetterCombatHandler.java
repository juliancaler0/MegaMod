package com.ultra.megamod.feature.combat.animation;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.Attack;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.HitboxShape;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes.SwingDirection;
import com.ultra.megamod.feature.computer.network.handlers.PartyHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side attack handler that replaces vanilla melee behaviour with a
 * Better Combat-style combo system.
 * <p>
 * When a player attacks with a weapon that has registered {@link WeaponAttributes},
 * this handler takes over: it selects the correct attack from the combo sequence,
 * applies the damage multiplier, finds additional targets in the directional hitbox,
 * advances the combo tracker, and broadcasts an {@link AttackAnimationPayload} to
 * nearby clients so they can play the matching swing animation.
 * <p>
 * Weapons without attributes fall through to vanilla attack logic untouched.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class BetterCombatHandler {

    /**
     * Handle the new C2S_AttackRequest from client with entity IDs.
     * Uses BC's validation model: client sends target IDs, server validates range.
     */
    public static void handleAttackRequest(
            com.ultra.megamod.feature.combat.animation.network.C2S_AttackRequest payload,
            net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof net.minecraft.server.level.ServerPlayer player)) return;

            // Get weapon attributes
            var mainHand = player.getMainHandItem();
            if (mainHand.isEmpty()) return;
            var attrs = WeaponAttributeRegistry.getAttributes(mainHand);
            if (attrs == null || attrs.attacks() == null || attrs.attacks().length == 0) return;

            // Use the combo count from the packet
            int comboCount = payload.comboCount();
            var attackHand = com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper.getCurrentAttack(player, comboCount);
            if (attackHand == null) return;

            var selectedAttack = attackHand.attack();
            boolean isOffHand = attackHand.isOffHand();
            var activeAttributes = attackHand.attributes();

            // Calculate attack range
            double attackRange = com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper.getAttackRange(player);
            float tsrm = com.ultra.megamod.feature.combat.animation.config.ScopedCombatConfig.targetSearchRangeMultiplier(player);
            double rangeSquared = attackRange * attackRange * tsrm * tsrm;

            // Process entity IDs from client
            // Wrap in attribute swap for off-hand attacks (dual-wielding)
            com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper.swapHandAttributes(
                    player, isOffHand, () -> {
                // Apply damage multiplier from attack definition
                float damageMultiplier = (float) selectedAttack.damageMultiplier();
                float dualWieldMultiplier = com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper
                        .getDualWieldingAttackDamageMultiplier(player, attackHand);
                damageMultiplier *= dualWieldMultiplier;

                int targetIndex = 0;
                for (int entityId : payload.entityIds()) {
                    var target = player.level().getEntity(entityId);
                    if (target == null || !target.isAlive()) continue;
                    if (!(target instanceof net.minecraft.world.entity.LivingEntity living)) continue;

                    // Validate range
                    if (player.distanceToSqr(target) > rangeSquared) continue;

                    // Allow fast attacks (bypass damage throttle)
                    if (com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.allow_fast_attacks) {
                        living.invulnerableTime = 0;
                    }

                    // Sweeping damage falloff: each extra target takes less damage
                    if (com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.allow_reworked_sweeping
                            && targetIndex > 0) {
                        int extraTargetCount = com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.reworked_sweeping_extra_target_count;
                        float maxPenalty = com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig.reworked_sweeping_maximum_damage_penalty;
                        float penalty = Math.min((float) targetIndex / extraTargetCount, 1.0f) * maxPenalty;
                        // Temporarily reduce damage for this hit by adding a negative modifier
                        // (BC uses a temporary attribute modifier approach)
                    }

                    // Attack the target
                    player.attack(target);
                    targetIndex++;
                }
            });

            // Broadcast animation to tracking clients
            var direction = selectedAttack.swingDirection() != null
                    ? selectedAttack.swingDirection() : WeaponAttributes.SwingDirection.SLASH_RIGHT;
            String animName = selectedAttack.animation() != null ? selectedAttack.animation()
                    : mapDirectionToAnimation(direction, activeAttributes.twoHanded());
            int usedIndex = attackHand.combo().current() - 1;

            var animPayload = new com.ultra.megamod.feature.combat.animation.network.S2C_AttackAnimation(
                    player.getId(), usedIndex, animName,
                    com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper.getAttackCooldownTicksCapped(player),
                    (float) selectedAttack.upswing());
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, animPayload);

            // Movement speed penalty
            com.ultra.megamod.feature.attributes.AttributeHelper.addModifier(
                    player, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                    MELEE_SWING_SLOW_ID, MELEE_SWING_SPEED_PENALTY,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

            // Schedule removal of speed penalty after 7 ticks (~350ms)
            // Use a simple tick counter instead of Thread.sleep
            var server = player.level().getServer();
            if (server != null) {
                final var playerRef = player;
                server.execute(() -> {
                    // Remove after a short delay (next tick for safety)
                    server.execute(() ->
                        com.ultra.megamod.feature.attributes.AttributeHelper.removeModifier(
                                playerRef, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                                MELEE_SWING_SLOW_ID));
                });
            }
        });
    }

    /** Base melee reach in blocks (vanilla default). */
    private static final double BASE_REACH = 3.0;

    /** Minimum damage multiplier we'll accept from an attack definition. */
    private static final double MIN_DAMAGE_MULTIPLIER = 0.1;

    /** Unique identifier for the melee swing movement speed penalty. */
    private static final Identifier MELEE_SWING_SLOW_ID = Identifier.fromNamespaceAndPath("megamod", "melee_swing_slow");

    /** 50% movement speed reduction while a melee swing is active. */
    private static final double MELEE_SWING_SPEED_PENALTY = -0.5;

    /** Duration of the swing slow in ticks (~350ms). */
    private static final int SWING_SLOW_DURATION_TICKS = 7;

    /** Maximum number of extra targets hit by a sweeping attack. */
    private static final int MAX_EXTRA_SWEEP_TARGETS = 4;

    /** Base sweep multiplier for the 1st extra target (diminishes by 0.1 per subsequent target). */
    private static final float SWEEP_BASE_MULTIPLIER = 0.5f;

    /** Minimum sweep multiplier floor (4th+ extra target). */
    private static final float SWEEP_MIN_MULTIPLIER = 0.25f;

    /** Sweep multiplier step: each successive extra target loses this much damage. */
    private static final float SWEEP_DIMINISH_STEP = 0.1f;

    /**
     * Tracks the server tick at which each player's melee swing slow was applied.
     * Used by the tick handler to know when to remove the modifier.
     */
    private static final Map<UUID, Long> SWING_SLOW_APPLIED_AT = new ConcurrentHashMap<>();

    // ── Client-initiated attack handler ────────────────────────────────

    /**
     * Processes a BetterCombat attack request sent from the client via
     * {@link BetterCombatAttackPayload}. The client mixin intercepts
     * {@code Minecraft.startAttack()}, cancels vanilla's swing/attack,
     * and sends the payload here instead.
     * <p>
     * This method performs the full combo attack flow:
     * <ol>
     *   <li>Resolve weapon attributes and combo state</li>
     *   <li>Select the correct attack from the combo sequence</li>
     *   <li>Find the primary target via server-side raycast</li>
     *   <li>Deal damage to the primary target via {@code player.attack()}</li>
     *   <li>Find and damage additional sweeping targets in the directional cone</li>
     *   <li>Broadcast animation to all tracking clients</li>
     *   <li>Apply melee swing movement speed penalty</li>
     * </ol>
     *
     * @param player the server-side player who initiated the attack
     */
    public static void handleClientAttackRequest(ServerPlayer player) {
        // 1. Get weapon from main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        // 2. Look up weapon attributes
        WeaponAttributes attributes = WeaponAttributeRegistry.getAttributes(mainHand);
        if (attributes == null || attributes.attacks() == null || attributes.attacks().length == 0) return;

        // 3. Determine combo state
        UUID playerId = player.getUUID();
        long currentTick = player.level().getServer().getTickCount();
        boolean dualWielding = isDualWielding(player);
        boolean isOffHand = dualWielding && shouldAttackWithOffHand(player, playerId);

        // Resolve which hand's attributes to use for dual-wielding
        WeaponAttributes activeAttributes = attributes;
        if (isOffHand) {
            ItemStack offHand = player.getOffhandItem();
            WeaponAttributes offAttributes = WeaponAttributeRegistry.getAttributes(offHand);
            if (offAttributes != null && offAttributes.attacks() != null && offAttributes.attacks().length > 0) {
                activeAttributes = offAttributes;
            } else {
                isOffHand = false;
            }
        }

        // 4. Select the attack from the combo sequence
        int rawComboIndex = PlayerComboTracker.getComboIndex(playerId);
        Attack selectedAttack = selectAttack(player, activeAttributes, rawComboIndex, dualWielding, isOffHand);
        if (selectedAttack == null) return;

        // 5. Advance combo
        int usedIndex = PlayerComboTracker.advanceCombo(playerId, activeAttributes.category(), currentTick);

        // 6. Set attack context for damage multiplier
        double damageMultiplier = Math.max(selectedAttack.damageMultiplier(), MIN_DAMAGE_MULTIPLIER);
        CombatAttackContext.setActiveAttack(playerId, selectedAttack, damageMultiplier, isOffHand);

        // 7. Server-side raycast to find the primary target
        double range = BASE_REACH + activeAttributes.rangeBonus();
        Entity primaryTarget = findPrimaryTarget(player, range);

        // 8. Attack the primary target via vanilla's player.attack() — triggers AttackEntityEvent
        //    but our onPlayerAttack will see the CombatAttackContext is already set and skip
        //    re-processing. The vanilla attack applies damage, knockback, enchantment effects.
        if (primaryTarget != null) {
            player.attack(primaryTarget);

            // Apply knockback reduction for fast weapons
            if (selectedAttack.damageMultiplier() < 1.0 && primaryTarget instanceof LivingEntity primaryLiving) {
                float knockbackScale = (float) Math.min(1.0, selectedAttack.damageMultiplier());
                Vec3 currentMotion = primaryLiving.getDeltaMovement();
                primaryLiving.setDeltaMovement(
                        currentMotion.x * knockbackScale,
                        currentMotion.y,
                        currentMotion.z * knockbackScale
                );
            }

        }

        // 9. Find additional sweeping targets in the directional hitbox
        if (selectedAttack.angle() > 0) {
            List<LivingEntity> sweepTargets = findTargetsInCone(player, selectedAttack, range);
            float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            int extraTargetIndex = 0;
            for (LivingEntity target : sweepTargets) {
                if (target == primaryTarget) continue;
                if (target == player) continue;
                if (!target.isAlive()) continue;
                if (target instanceof ArmorStand armorStand && armorStand.isMarker()) continue;
                if (extraTargetIndex >= MAX_EXTRA_SWEEP_TARGETS) break;

                float sweepMultiplier = Math.max(SWEEP_MIN_MULTIPLIER,
                        SWEEP_BASE_MULTIPLIER - (extraTargetIndex * SWEEP_DIMINISH_STEP));
                float sweepDamage = (float) (baseDamage * damageMultiplier * sweepMultiplier);
                if (sweepDamage > 0) {
                    target.hurt(player.damageSources().playerAttack(player), sweepDamage);
                }
                extraTargetIndex++;
            }
        }

        // 10. Broadcast animation payload to nearby clients
        SwingDirection direction = selectedAttack.swingDirection() != null
                ? selectedAttack.swingDirection()
                : SwingDirection.SLASH_RIGHT;
        String animName = selectedAttack.animation() != null ? selectedAttack.animation()
                : mapDirectionToAnimation(direction, activeAttributes.twoHanded());

        float cooldownTicks = com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper
                .getAttackCooldownTicksCapped(player);
        AttackAnimationPayload animPayload = new AttackAnimationPayload(
                player.getId(),
                usedIndex,
                direction.ordinal(),
                isOffHand,
                activeAttributes.twoHanded(),
                animName,
                cooldownTicks,
                (float) selectedAttack.upswing()
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, animPayload);

        // 11. Apply melee swing movement speed penalty
        AttributeHelper.addModifier(player, Attributes.MOVEMENT_SPEED, MELEE_SWING_SLOW_ID,
                MELEE_SWING_SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        SWING_SLOW_APPLIED_AT.put(playerId, currentTick);

        // 12. Reset the attack strength timer so the cooldown bar restarts
        player.resetAttackStrengthTicker();
    }

    /**
     * Server-side raycast to find the entity the player is looking at within melee range.
     * Uses the server-side player position and look direction — does not trust the client
     * to tell us which entity to hit.
     *
     * @param player the attacking player
     * @param range  effective melee reach in blocks
     * @return the closest living entity in the player's line of sight, or {@code null}
     */
    private static Entity findPrimaryTarget(ServerPlayer player, double range) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookDir.scale(range));

        // Get all entities in the reach box
        AABB searchBox = player.getBoundingBox().expandTowards(lookDir.scale(range)).inflate(1.0);
        List<Entity> candidates = player.level().getEntities(player, searchBox,
                e -> e instanceof LivingEntity && e != player && e.isAlive() && e.isPickable());

        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : candidates) {
            AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
            var hitResult = entityBox.clip(eyePos, endPos);

            if (hitResult.isPresent()) {
                double dist = eyePos.distanceToSqr(hitResult.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = entity;
                }
            }
        }

        return closest;
    }

    // ── Main event handler ──────────────────────────────────────────────

    // TODO: Swing-through-grass — client-side feature that prevents non-collidable blocks
    //  (tall grass, flowers, ferns, etc.) from blocking weapon swings. When the player's
    //  crosshair hits a non-solid block but a valid entity target exists behind it within
    //  reach, the attack should pass through to the entity. This requires a client-side mixin
    //  (likely on MultiPlayerGameMode or the attack raycast) to re-cast the ray ignoring
    //  non-collidable blocks before sending the attack packet to the server.

    /**
     * Intercepts every player melee attack. If the held weapon has
     * {@link WeaponAttributes} we run the combo logic; otherwise vanilla handles it.
     * <p>
     * When the attack was initiated by {@link #handleClientAttackRequest}, the
     * {@link CombatAttackContext} will already be set. In that case, we skip the
     * redundant combo/sweep logic (it was already handled) but allow vanilla damage
     * to proceed.
     */
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        // If CombatAttackContext is already set, this attack was initiated by
        // handleClientAttackRequest() which called player.attack(). The combo logic,
        // sweeping, animation broadcast, and swing slow have already been applied.
        // Let vanilla damage proceed (don't cancel) but skip our redundant processing.
        if (CombatAttackContext.getActiveAttack(playerId) != null) {
            return;
        }

        // --- Legacy path: direct vanilla attacks (e.g. from non-mixin clients) ---
        // This fallback ensures the combo system still works if a player somehow
        // bypasses the client mixin (e.g. mods, macros, or non-BetterCombat weapons
        // that happen to have attributes added at runtime).

        // 1. Get weapon from main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        // 2. Look up weapon attributes
        WeaponAttributes attributes = WeaponAttributeRegistry.getAttributes(mainHand);
        if (attributes == null || attributes.attacks() == null || attributes.attacks().length == 0) {
            return; // No attributes — let vanilla handle it
        }

        // 3. Determine combo state
        long currentTick = player.level().getServer().getTickCount();
        boolean dualWielding = isDualWielding(player);
        boolean isOffHand = dualWielding && shouldAttackWithOffHand(player, playerId);

        // Resolve which hand's attributes to use for dual-wielding
        WeaponAttributes activeAttributes = attributes;
        if (isOffHand) {
            ItemStack offHand = player.getOffhandItem();
            WeaponAttributes offAttributes = WeaponAttributeRegistry.getAttributes(offHand);
            if (offAttributes != null && offAttributes.attacks() != null && offAttributes.attacks().length > 0) {
                activeAttributes = offAttributes;
            } else {
                isOffHand = false; // Fall back to main hand
            }
        }

        // 4. Select the attack from the combo sequence
        int rawComboIndex = PlayerComboTracker.getComboIndex(playerId);
        Attack selectedAttack = selectAttack(player, activeAttributes, rawComboIndex, dualWielding, isOffHand);
        if (selectedAttack == null) {
            return; // All attacks filtered out — let vanilla handle
        }

        // 5. Advance combo (returns the index that was used)
        int usedIndex = PlayerComboTracker.advanceCombo(playerId, activeAttributes.category(), currentTick);

        // 6. Apply damage multiplier
        double damageMultiplier = Math.max(selectedAttack.damageMultiplier(), MIN_DAMAGE_MULTIPLIER);
        CombatAttackContext.setActiveAttack(playerId, selectedAttack, damageMultiplier, isOffHand);

        // 7. Find additional sweeping targets in the directional hitbox
        double range = BASE_REACH + activeAttributes.rangeBonus();

        List<LivingEntity> sweepTargets = findTargetsInCone(player, selectedAttack, range);
        Entity primaryTarget = event.getTarget();

        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        int extraTargetIndex = 0;
        for (LivingEntity target : sweepTargets) {
            if (target == primaryTarget) continue;
            if (target == player) continue;
            if (!target.isAlive()) continue;
            if (target instanceof ArmorStand armorStand && armorStand.isMarker()) continue;
            if (extraTargetIndex >= MAX_EXTRA_SWEEP_TARGETS) break;

            float sweepMultiplier = Math.max(SWEEP_MIN_MULTIPLIER, SWEEP_BASE_MULTIPLIER - (extraTargetIndex * SWEEP_DIMINISH_STEP));
            float sweepDamage = (float) (baseDamage * damageMultiplier * sweepMultiplier);
            if (sweepDamage > 0) {
                target.hurt(player.damageSources().playerAttack(player), sweepDamage);
            }
            extraTargetIndex++;
        }

        // Apply knockback reduction for fast weapons
        if (selectedAttack.damageMultiplier() < 1.0 && primaryTarget instanceof LivingEntity primaryLiving) {
            float knockbackScale = (float) Math.min(1.0, selectedAttack.damageMultiplier());
            Vec3 currentMotion = primaryLiving.getDeltaMovement();
            primaryLiving.setDeltaMovement(
                    currentMotion.x * knockbackScale,
                    currentMotion.y,
                    currentMotion.z * knockbackScale
            );
        }

        // 8. Broadcast animation payload to nearby clients
        SwingDirection direction = selectedAttack.swingDirection() != null
                ? selectedAttack.swingDirection()
                : SwingDirection.SLASH_RIGHT;
        String animName2 = selectedAttack.animation() != null ? selectedAttack.animation()
                : mapDirectionToAnimation(direction, activeAttributes.twoHanded());

        float cooldownTicks2 = com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper
                .getAttackCooldownTicksCapped(player);
        AttackAnimationPayload animPayload = new AttackAnimationPayload(
                player.getId(),
                usedIndex,
                direction.ordinal(),
                isOffHand,
                activeAttributes.twoHanded(),
                animName2,
                cooldownTicks2,
                (float) selectedAttack.upswing()
        );
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, animPayload);

        // 9. Apply melee swing movement speed penalty (-50% for ~350ms / 7 ticks)
        AttributeHelper.addModifier(player, Attributes.MOVEMENT_SPEED, MELEE_SWING_SLOW_ID,
                MELEE_SWING_SPEED_PENALTY, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        SWING_SLOW_APPLIED_AT.put(playerId, currentTick);
    }

    // ── Melee swing slow removal ────────────────────────────────────────

    /**
     * Removes the melee swing movement speed penalty after its duration expires.
     * Runs every tick for each server-side player that has the modifier applied.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();
        Long appliedAt = SWING_SLOW_APPLIED_AT.get(playerId);
        if (appliedAt == null) return;

        long currentTick = player.level().getServer().getTickCount();
        if (currentTick - appliedAt >= SWING_SLOW_DURATION_TICKS) {
            AttributeHelper.removeModifier(player, Attributes.MOVEMENT_SPEED, MELEE_SWING_SLOW_ID);
            SWING_SLOW_APPLIED_AT.remove(playerId);
        }
    }

    // ── Attack selection ────────────────────────────────────────────────

    /**
     * Selects the appropriate attack from the weapon's combo sequence based on the
     * current combo index. Attacks whose conditions are not met are filtered out first;
     * then the index wraps around the remaining attacks.
     *
     * @param player        the attacking player
     * @param attributes    the active hand's weapon attributes
     * @param comboIndex    current raw combo index
     * @param dualWielding  whether the player is dual-wielding
     * @param isOffHand     whether this attack comes from the off-hand
     * @return the selected {@link Attack}, or {@code null} if none qualify
     */
    public static Attack selectAttack(ServerPlayer player, WeaponAttributes attributes,
                                       int comboIndex, boolean dualWielding, boolean isOffHand) {
        if (attributes.attacks() == null || attributes.attacks().length == 0) return null;

        boolean isMounted = player.getVehicle() != null;
        boolean isSameCategory = false;
        if (dualWielding) {
            WeaponAttributes mainAttr = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
            WeaponAttributes offAttr = WeaponAttributeRegistry.getAttributes(player.getOffhandItem());
            if (mainAttr != null && offAttr != null
                    && mainAttr.category() != null && offAttr.category() != null) {
                isSameCategory = mainAttr.category().equals(offAttr.category());
            }
        }

        // Filter attacks by conditions
        final boolean fDual = dualWielding;
        final boolean fSameCat = isSameCategory;
        final boolean fOffHand = isOffHand;
        final boolean fMounted = isMounted;

        Attack[] filtered = Arrays.stream(attributes.attacks())
                .filter(a -> a.evaluateConditions(fDual, fSameCat, fOffHand, fMounted))
                .toArray(Attack[]::new);

        if (filtered.length == 0) return null;

        // For dual-wielding, compute hand-specific combo count
        int effectiveIndex;
        if (dualWielding) {
            // Each hand gets every other combo step
            int handSpecific = ((isOffHand && comboIndex > 0) ? (comboIndex - 1) : comboIndex) / 2;
            effectiveIndex = handSpecific % filtered.length;
        } else {
            effectiveIndex = Math.floorMod(comboIndex, filtered.length);
        }

        return filtered[effectiveIndex];
    }

    // ── Directional hitbox detection ────────────────────────────────────

    /**
     * Finds all living entities within the attack's directional hitbox.
     * <ul>
     *   <li>{@link HitboxShape#HORIZONTAL_PLANE}: horizontal arc centred on look direction</li>
     *   <li>{@link HitboxShape#VERTICAL_PLANE}: vertical arc centred on look direction</li>
     *   <li>{@link HitboxShape#FORWARD_BOX}: narrow box extending forward from player</li>
     * </ul>
     *
     * @param player the attacking player
     * @param attack the attack definition (shape, angle)
     * @param range  effective reach in blocks
     * @return list of living entities hit (may include the primary target)
     */
    public static List<LivingEntity> findTargetsInCone(ServerPlayer player, Attack attack, double range) {
        List<LivingEntity> targets = new ArrayList<>();

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle().normalize();

        // Broad-phase: get all entities in a bounding box around the player
        AABB searchBox = player.getBoundingBox().inflate(range + 1.0);
        List<Entity> nearby = player.level().getEntities(player, searchBox,
                e -> e instanceof LivingEntity && e != player && e.isAlive());

        double angleRad = Math.toRadians(attack.angle() > 0 ? attack.angle() / 2.0 : 45.0);
        HitboxShape shape = attack.hitbox() != null ? attack.hitbox() : HitboxShape.HORIZONTAL_PLANE;

        // Pre-fetch party membership for friendly-fire filtering
        Set<UUID> partyMembers = PartyHandler.getPartyMembers(player.getUUID());

        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity living)) continue;

            // ── Friendly entity filtering (skip allies in sweeping/cleave) ──

            // Skip players on the same team
            if (entity instanceof Player otherPlayer) {
                if (otherPlayer.getTeam() != null && otherPlayer.getTeam().equals(player.getTeam())) continue;
                // Skip players in the same party
                if (partyMembers != null && partyMembers.contains(otherPlayer.getUUID())) continue;
            }

            // Skip tamed animals owned by the attacker
            if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(player)) continue;

            // Skip vanilla villagers / wandering traders
            if (entity instanceof AbstractVillager) continue;

            // Skip MegaMod citizen NPCs
            if (entity instanceof MCEntityCitizen) continue;

            Vec3 targetCenter = entity.getBoundingBox().getCenter();
            Vec3 toTarget = targetCenter.subtract(eyePos);
            double distance = toTarget.length();

            // Range check
            if (distance > range + entity.getBbWidth() / 2.0) continue;

            switch (shape) {
                case HORIZONTAL_PLANE -> {
                    // Project both vectors onto the horizontal (XZ) plane
                    Vec3 flatLook = new Vec3(lookDir.x, 0, lookDir.z).normalize();
                    Vec3 flatToTarget = new Vec3(toTarget.x, 0, toTarget.z).normalize();

                    if (flatLook.lengthSqr() < 0.001 || flatToTarget.lengthSqr() < 0.001) continue;

                    double dot = flatLook.dot(flatToTarget);
                    double angle = Math.acos(Math.min(1.0, Math.max(-1.0, dot)));
                    if (angle <= angleRad) {
                        targets.add(living);
                    }
                }
                case VERTICAL_PLANE -> {
                    // Project onto the vertical plane defined by the look direction
                    // Use the plane formed by lookDir and the Y axis
                    Vec3 flatLook = new Vec3(lookDir.x, 0, lookDir.z).normalize();
                    if (flatLook.lengthSqr() < 0.001) {
                        // Looking straight up/down — use forward as X, Y as Y
                        flatLook = new Vec3(1, 0, 0);
                    }

                    // Project toTarget onto the vertical plane containing lookDir
                    // Vertical component
                    double vertComponent = toTarget.y;
                    // Forward component (along horizontal look direction)
                    double fwdComponent = toTarget.x * flatLook.x + toTarget.z * flatLook.z;

                    Vec3 projectedDir = new Vec3(fwdComponent, vertComponent, 0).normalize();
                    Vec3 projectedLook = new Vec3(
                            Math.sqrt(lookDir.x * lookDir.x + lookDir.z * lookDir.z),
                            lookDir.y, 0
                    ).normalize();

                    if (projectedDir.lengthSqr() < 0.001 || projectedLook.lengthSqr() < 0.001) continue;

                    double dot = projectedDir.dot(projectedLook);
                    double angle = Math.acos(Math.min(1.0, Math.max(-1.0, dot)));
                    if (angle <= angleRad) {
                        targets.add(living);
                    }
                }
                case FORWARD_BOX -> {
                    // Narrow box: check that target is roughly ahead of the player
                    Vec3 toTargetNorm = toTarget.normalize();
                    double dot = lookDir.dot(toTargetNorm);

                    // Must be within ~60 degrees forward and within a 1.5 block wide corridor
                    if (dot > 0.5) {
                        // Check lateral distance from the look line
                        Vec3 projected = lookDir.scale(lookDir.dot(toTarget));
                        Vec3 lateral = toTarget.subtract(projected);
                        if (lateral.length() <= 1.5) {
                            targets.add(living);
                        }
                    }
                }
            }
        }

        return targets;
    }

    // ── Dual-wielding helpers ───────────────────────────────────────────

    /**
     * Checks if both hands hold weapons with {@link WeaponAttributes} and neither
     * weapon is two-handed.
     */
    public static boolean isDualWielding(Player player) {
        WeaponAttributes mainAttr = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        WeaponAttributes offAttr = WeaponAttributeRegistry.getAttributes(player.getOffhandItem());
        return mainAttr != null && !mainAttr.twoHanded()
                && offAttr != null && !offAttr.twoHanded();
    }

    /**
     * When dual-wielding, odd combo counts use the off-hand.
     */
    private static boolean shouldAttackWithOffHand(Player player, UUID playerId) {
        int comboIndex = PlayerComboTracker.getComboIndex(playerId);
        return comboIndex % 2 == 1;
    }

    // ── Per-player cleanup ────────────────────────────────────────────

    /**
     * Remove swing slow tracking for a specific player (e.g., on disconnect).
     */
    public static void removeSwingSlow(UUID playerId) {
        SWING_SLOW_APPLIED_AT.remove(playerId);
    }

    /**
     * Clear all static combat state. Called on server shutdown to prevent
     * stale data persisting across singleplayer world reloads.
     */
    public static void clearAll() {
        SWING_SLOW_APPLIED_AT.clear();
        CombatAttackContext.clearAll();
    }

    // ── Attack context for damage events ────────────────────────────────

    /**
     * Temporary per-attack context so that downstream damage events (e.g.
     * {@code LivingDamageEvent}) can read the active combo multiplier.
     * Cleared after the attack resolves.
     */
    public static final class CombatAttackContext {
        private static final java.util.Map<UUID, ActiveAttack> ACTIVE = new java.util.concurrent.ConcurrentHashMap<>();

        public record ActiveAttack(Attack attack, double damageMultiplier, boolean isOffHand) { }

        public static void setActiveAttack(UUID playerId, Attack attack, double multiplier, boolean isOffHand) {
            ACTIVE.put(playerId, new ActiveAttack(attack, multiplier, isOffHand));
        }

        public static ActiveAttack getActiveAttack(UUID playerId) {
            return ACTIVE.get(playerId);
        }

        public static ActiveAttack consumeActiveAttack(UUID playerId) {
            return ACTIVE.remove(playerId);
        }

        public static void clear(UUID playerId) {
            ACTIVE.remove(playerId);
        }

        /** Clear all tracked attack contexts. Called on server shutdown. */
        public static void clearAll() {
            ACTIVE.clear();
        }
    }

    /**
     * Maps a SwingDirection to a default BetterCombat animation name
     * when no explicit animation is set on the Attack record.
     */
    private static String mapDirectionToAnimation(SwingDirection direction, boolean twoHanded) {
        return switch (direction) {
            case SLASH_RIGHT -> twoHanded ? "two_handed_slash_horizontal_right" : "one_handed_slash_horizontal_right";
            case SLASH_LEFT -> twoHanded ? "two_handed_slash_horizontal_left" : "one_handed_slash_horizontal_left";
            case SLASH_DOWN -> twoHanded ? "two_handed_slam" : "one_handed_slam";
            case STAB -> twoHanded ? "two_handed_stab_left" : "one_handed_stab";
            case UPPERCUT -> "one_handed_uppercut_right";
            case SPIN -> "two_handed_spin";
        };
    }
}
