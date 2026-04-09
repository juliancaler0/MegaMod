package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import com.ultra.megamod.feature.combat.animation.AttackHand;
import com.ultra.megamod.feature.combat.animation.WeaponAttributeRegistry;
import com.ultra.megamod.feature.combat.animation.WeaponAttributes;
import com.ultra.megamod.feature.combat.animation.api.MinecraftClient_BetterCombat;
import com.ultra.megamod.feature.combat.animation.api.PlayerAttackProperties;
import com.ultra.megamod.feature.combat.animation.client.*;
import com.ultra.megamod.feature.combat.animation.collision.TargetFinder;
import com.ultra.megamod.feature.combat.animation.config.BetterCombatConfig;
import com.ultra.megamod.feature.combat.animation.logic.AnimatedHand;
import com.ultra.megamod.feature.combat.animation.logic.PlayerAttackHelper;
import com.ultra.megamod.feature.combat.animation.logic.WeaponSwing;
import com.ultra.megamod.feature.combat.animation.network.C2S_AttackRequest;
// S2C_AttackAnimation is sent by server only (BetterCombatHandler), not from client
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Full BetterCombat client attack controller.
 * Ported 1:1 from BetterCombat's MinecraftClientInject.
 *
 * Implements the complete attack flow:
 * - startUpswing() with timing calculations
 * - attackFromUpswingIfNeeded() per tick
 * - performAttack() with client-side TargetFinder hit detection
 * - Combo management with timeout and weapon-switch reset
 * - Hold-to-attack with proper cooldown checking
 * - Swing-thru-grass with smart detection
 * - Feint (cancel upswing) via keybind
 * - Mining detection with weapon whitelist/blacklist
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftClient_BetterCombat {

    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public HitResult hitResult;
    @Shadow private int rightClickDelay;

    @Unique private boolean megamod$isHoldingAttackInput = false;
    @Unique private boolean megamod$isHarvesting = false;
    @Unique private ItemStack megamod$upswingStack;
    @Unique private ItemStack megamod$lastAttackedWithItemStack;
    @Unique private WeaponSwing megamod$ongoingSwing;
    @Unique private int megamod$lastAttacked = 1000;
    @Unique private float megamod$lastSwingDuration = 0;
    @Unique private int megamod$comboReset = 0;
    @Unique private List<Entity> megamod$targetsInReach = null;

    @Unique private Minecraft megamod$thisClient() {
        return (Minecraft) (Object) this;
    }

    @Unique private int megamod$currentTime() {
        return player != null ? player.tickCount : 0;
    }

    @Unique private int megamod$currentUpswingTicks() {
        if (megamod$ongoingSwing == null) return 0;
        return megamod$ongoingSwing.upswingTicksLeft(megamod$currentTime());
    }

    // ═══════════════════════════════════════════
    // Attack interception — press to attack
    // ═══════════════════════════════════════════

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void megamod$interceptAttack(CallbackInfoReturnable<Boolean> cir) {
        if (player == null) return;
        var mainHand = player.getMainHandItem();
        WeaponAttributes attrs = WeaponAttributeRegistry.getAttributes(mainHand);

        // DEBUG: Log every attack attempt
        com.ultra.megamod.MegaMod.LOGGER.info("[BetterCombat] startAttack called. Item: " + mainHand.getItem()
                + " | HasAttrs: " + (attrs != null)
                + " | HasAttacks: " + (attrs != null && attrs.attacks() != null ? attrs.attacks().length : 0));

        if (attrs != null && attrs.attacks() != null && attrs.attacks().length > 0) {
            if (megamod$isTargetingMineableBlock() || megamod$isHarvesting) {
                megamod$isHarvesting = true;
                com.ultra.megamod.MegaMod.LOGGER.info("[BetterCombat] Mining block, skipping combat");
                return;
            }
            com.ultra.megamod.MegaMod.LOGGER.info("[BetterCombat] Starting upswing!");
            megamod$startUpswing(attrs);
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    // Hold to attack
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void megamod$holdToAttack(boolean leftClick, CallbackInfo ci) {
        if (!leftClick || player == null) return;
        WeaponAttributes attrs = WeaponAttributeRegistry.getAttributes(player.getMainHandItem());
        if (attrs == null || attrs.attacks() == null || attrs.attacks().length == 0) return;

        boolean isPressed = megamod$thisClient().options.keyAttack.isDown();
        if (isPressed && !megamod$isHoldingAttackInput) {
            if (megamod$isTargetingMineableBlock() || megamod$isHarvesting) {
                megamod$isHarvesting = true;
                return;
            } else {
                ci.cancel();
            }
        }

        if (BetterCombatConfig.isHoldToAttackEnabled && isPressed) {
            megamod$isHoldingAttackInput = true;
            megamod$startUpswing(attrs);
            ci.cancel();
        } else {
            megamod$isHarvesting = false;
            megamod$isHoldingAttackInput = false;
        }
    }

    // Block item use during upswing
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void megamod$blockItemUseDuringUpswing(CallbackInfo ci) {
        if (player == null) return;
        var hand = megamod$getCurrentHand();
        if (hand == null) return;
        double upswingRate = hand.upswingRate();
        if (megamod$currentUpswingTicks() > 0 || player.getAttackStrengthScale(0) < (1.0 - upswingRate)) {
            ci.cancel();
        }
    }

    // ═══════════════════════════════════════════
    // Mining detection (swing-thru-grass)
    // ═══════════════════════════════════════════

    @Unique
    private boolean megamod$isTargetingMineableBlock() {
        if (!BetterCombatConfig.isMiningWithWeaponsEnabled) return false;
        if (BetterCombatConfig.isAttackInsteadOfMineWhenEnemiesCloseEnabled && this.hasTargetsInReach()) return false;

        Minecraft mc = megamod$thisClient();
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) mc.hitResult;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            if (megamod$shouldSwingThruGrass()) {
                return !state.getCollisionShape(mc.level, pos).isEmpty() || state.getDestroySpeed(mc.level, pos) != 0.0F;
            } else {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean megamod$shouldSwingThruGrass() {
        if (!BetterCombatConfig.isSwingThruGrassEnabled) return false;
        if (BetterCombatConfig.isSwingThruGrassSmart && !this.hasTargetsInReach()) return false;
        return true;
    }

    // ═══════════════════════════════════════════
    // Core attack flow — upswing → attack → combo
    // ═══════════════════════════════════════════

    @Unique
    private void megamod$startUpswing(WeaponAttributes attributes) {
        if (player == null || player.isPassenger()) return;

        var attackHand = megamod$getCurrentHand();
        if (attackHand == null) return;
        float upswingRate = (float) attackHand.upswingRate();
        if (megamod$currentUpswingTicks() > 0 || player.isUsingItem()
                || player.getAttackStrengthScale(0) < (1.0 - upswingRate)) {
            return;
        }

        // Start upswing
        player.stopUsingItem();
        megamod$lastAttacked = 0;
        megamod$upswingStack = player.getMainHandItem();
        float attackCooldownTicks = PlayerAttackHelper.getAttackCooldownTicksCapped(player);
        this.megamod$comboReset = Math.round(attackCooldownTicks * BetterCombatConfig.combo_reset_rate);
        int upswingTicks = Math.max(Math.round(attackCooldownTicks * upswingRate), 1);
        this.megamod$ongoingSwing = new WeaponSwing(attackHand, megamod$currentTime(), upswingTicks, attackCooldownTicks);
        this.megamod$lastSwingDuration = attackCooldownTicks;
        this.rightClickDelay = Math.round(attackCooldownTicks);

        // Play animation via PlayerAnimator
        String animationName = attackHand.attack().animation();
        com.ultra.megamod.MegaMod.LOGGER.info("[BetterCombat] Upswing started! Anim: " + animationName + " | cooldown: " + attackCooldownTicks + " | upswing: " + upswingTicks);
        if (animationName == null || animationName.isEmpty()) {
            animationName = megamod$fallbackAnimationName(attackHand, attributes);
            com.ultra.megamod.MegaMod.LOGGER.info("[BetterCombat] Using fallback animation: " + animationName);
        }
        boolean isOffHand = attackHand.isOffHand();
        var animatedHand = AnimatedHand.from(isOffHand, attributes.twoHanded());
        if (player instanceof PlayerAttackAnimatable animatable) {
            animatable.playAttackAnimation(animationName, animatedHand, attackCooldownTicks, upswingRate);
        }

        // Animation is played locally above. Server broadcasts to other clients
        // via BetterCombatHandler.handleAttackRequest() when C2S_AttackRequest arrives.

        // Track swing state for particle system
        var dir = attackHand.attack().swingDirection() != null
                ? attackHand.attack().swingDirection() : WeaponAttributes.SwingDirection.SLASH_RIGHT;
        SwingAnimationState.startSwing(player.getId(), dir, isOffHand, attributes.twoHanded(), getComboCount());
    }

    @Unique
    private void megamod$attackFromUpswingIfNeeded() {
        if (megamod$ongoingSwing != null && megamod$currentUpswingTicks() <= 0) {
            megamod$performAttack();
            megamod$upswingStack = null;
        }
    }

    @Unique
    private void megamod$performAttack() {
        // Feint check
        if (CombatKeybindings.FEINT_KEY != null && CombatKeybindings.FEINT_KEY.isDown()) {
            megamod$cancelWeaponSwing();
            return;
        }

        var weaponSwing = this.megamod$ongoingSwing;
        if (weaponSwing == null) return;
        var hand = weaponSwing.attackHand();
        if (hand == null) return;
        var attack = hand.attack();
        var upswingRate = hand.upswingRate();
        if (player.getAttackStrengthScale(0) < (1.0 - upswingRate)) return;

        // Client-side target detection using TargetFinder
        var cursorTarget = getCursorTarget();
        var range = PlayerAttackHelper.getAttackRange(player);
        List<Entity> targets = TargetFinder.findAttackTargets(player, cursorTarget, attack, range);
        megamod$targetsInReach = targets;

        // Send attack request with entity IDs to server
        int[] entityIds = targets.stream().mapToInt(Entity::getId).toArray();
        int cursorId = cursorTarget != null ? cursorTarget.getId() : -1;
        var attackPacket = new C2S_AttackRequest(
                getComboCount(), player.isShiftKeyDown(),
                player.getInventory().getSelectedSlot(), cursorId, entityIds);
        ClientPacketDistributor.sendToServer(attackPacket);

        // Execute attacks locally for responsive feel
        for (var target : targets) {
            player.attack(target);
        }
        player.resetAttackStrengthTicker();

        // Advance combo
        megamod$setComboCount(getComboCount() + 1);
        if (!hand.isOffHand()) {
            megamod$lastAttackedWithItemStack = hand.itemStack();
        }
    }

    @Unique
    private void megamod$cancelWeaponSwing() {
        if (player instanceof PlayerAttackAnimatable animatable) {
            var downWind = (int) Math.round(PlayerAttackHelper.getAttackCooldownTicksCapped(player)
                    * (1 - 0.5 * BetterCombatConfig.getUpswingMultiplier()));
            animatable.stopAttackAnimation(downWind);
        }
        megamod$upswingStack = null;
        megamod$ongoingSwing = null;
        rightClickDelay = 0;
    }

    @Unique
    private void megamod$resetComboIfNeeded() {
        if (megamod$lastAttacked > megamod$comboReset && getComboCount() > 0) {
            megamod$setComboCount(0);
        }
        // Weapon switch detection
        if (!PlayerAttackHelper.shouldAttackWithOffHand(player, getComboCount())) {
            if (player.getMainHandItem().isEmpty()
                    || (megamod$lastAttackedWithItemStack != null
                    && !megamod$lastAttackedWithItemStack.getItem().equals(player.getMainHandItem().getItem()))) {
                megamod$setComboCount(0);
            }
        }
    }

    // ═══════════════════════════════════════════
    // Per-tick updates
    // ═══════════════════════════════════════════

    @Inject(method = "tick", at = @At("HEAD"))
    private void megamod$preTick(CallbackInfo ci) {
        // Admin module state reset
        AdminModuleState.attackTickActive = false;
        AdminModuleState.useTickActive = false;

        if (player == null) return;
        megamod$targetsInReach = null;
        megamod$lastAttacked += 1;

        // Reset harvesting flag when attack key is released
        if (!megamod$thisClient().options.keyAttack.isDown()) {
            megamod$isHarvesting = false;
            megamod$isHoldingAttackInput = false;
        }

        // Check swing validity
        if (megamod$ongoingSwing != null) {
            int time = megamod$currentTime();
            var swing = megamod$ongoingSwing;
            if (swing.ticksLeft(time) <= 0) megamod$ongoingSwing = null;
            if (!player.isAlive() || !swing.isValid(time)) megamod$cancelWeaponSwing();
        }

        // Cancel if weapon changed
        if (megamod$upswingStack != null && !ItemStack.matches(player.getMainHandItem(), megamod$upswingStack)) {
            megamod$cancelWeaponSwing();
        }

        megamod$attackFromUpswingIfNeeded();
        megamod$resetComboIfNeeded();
    }

    // Admin module hooks
    @Inject(method = "startAttack", at = @At("HEAD"))
    private void megamod$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (AdminModuleState.attackTickEnabled) AdminModuleState.attackTickActive = true;
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void megamod$onStartUse(CallbackInfo ci) {
        if (AdminModuleState.useTickEnabled) AdminModuleState.useTickActive = true;
    }

    // ═══════════════════════════════════════════
    // MinecraftClient_BetterCombat implementation
    // ═══════════════════════════════════════════

    @Unique private AttackHand megamod$getCurrentHand() {
        return PlayerAttackHelper.getCurrentAttack(player, getComboCount());
    }

    @Unique private void megamod$setComboCount(int count) {
        if (player instanceof PlayerAttackProperties props) {
            props.setComboCount(count);
        }
    }

    @Unique
    private String megamod$fallbackAnimationName(AttackHand hand, WeaponAttributes attrs) {
        var dir = hand.attack().swingDirection();
        if (dir == null) dir = WeaponAttributes.SwingDirection.SLASH_RIGHT;
        return switch (dir) {
            case SLASH_RIGHT -> attrs.twoHanded() ? "two_handed_slash_horizontal_right" : "one_handed_slash_horizontal_right";
            case SLASH_LEFT -> attrs.twoHanded() ? "two_handed_slash_horizontal_left" : "one_handed_slash_horizontal_left";
            case SLASH_DOWN -> attrs.twoHanded() ? "two_handed_slam" : "one_handed_slam";
            case STAB -> attrs.twoHanded() ? "two_handed_stab_left" : "one_handed_stab";
            case UPPERCUT -> "one_handed_uppercut_right";
            case SPIN -> "two_handed_spin";
        };
    }

    @Override
    public int getComboCount() {
        if (player instanceof PlayerAttackProperties props) return props.getComboCount();
        return 0;
    }

    @Override
    public boolean hasTargetsInReach() {
        return megamod$targetsInReach != null && !megamod$targetsInReach.isEmpty();
    }

    @Override
    public float getSwingProgress() {
        if (megamod$lastAttacked > megamod$lastSwingDuration || megamod$lastSwingDuration <= 0) return 1F;
        return (float) megamod$lastAttacked / megamod$lastSwingDuration;
    }

    @Override
    public int getUpswingTicks() {
        return megamod$currentUpswingTicks();
    }

    @Override
    public void cancelUpswing() {
        if (megamod$currentUpswingTicks() > 0) megamod$cancelWeaponSwing();
    }

    @Override
    public @Nullable AttackHand getCurrentAttackHand() {
        return megamod$ongoingSwing != null ? megamod$ongoingSwing.attackHand() : null;
    }
}
