package com.ultra.megamod.feature.adminmodules.modules.movement;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import com.ultra.megamod.feature.attributes.AttributeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MovementModules {

    public static void register(Consumer<AdminModule> reg) {
        reg.accept(new NoFall());
        reg.accept(new Jesus());
        reg.accept(new Sprint());
        reg.accept(new Step());
        reg.accept(new NoSlow());
        reg.accept(new SafeWalk());
        reg.accept(new Spider());
        reg.accept(new Velocity());
        reg.accept(new ElytraBoost());
        reg.accept(new ElytraFly());
        reg.accept(new AutoJump());
        reg.accept(new LongJump());
        reg.accept(new BoatFly());
        reg.accept(new PhaseWalk());
        reg.accept(new ReverseStep());
        reg.accept(new Parkour());
        reg.accept(new TridentBoost());
        reg.accept(new TickShift());
        reg.accept(new Blink());
        reg.accept(new EntitySpeed());
        reg.accept(new IceSpeed());
        reg.accept(new AirJump());
        reg.accept(new FastClimb());
        reg.accept(new AntiVoid());
        reg.accept(new AutoWalk());
        reg.accept(new GUIMove());
        reg.accept(new HighJump());
        reg.accept(new Sneak());
        reg.accept(new ClickTP());
        reg.accept(new EntityControl());
        reg.accept(new Anchor());
        reg.accept(new Slippy());
        reg.accept(new AutoWasp());
    }

    // ── NoFall ──────────────────────────────────────────────────────────────
    static class NoFall extends AdminModule {
        NoFall() { super("no_fall", "NoFall", "Prevents fall damage", ModuleCategory.MOVEMENT); }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            if (event.getSource().is(DamageTypes.FALL)) {
                event.setNewDamage(0);
            }
        }
    }

    // ── Jesus ───────────────────────────────────────────────────────────────
    static class Jesus extends AdminModule {
        private ModuleSetting.BoolSetting lava;
        private ModuleSetting.EnumSetting mode;
        private ModuleSetting.DoubleSetting speedBoost;
        Jesus() { super("jesus", "Jesus", "Walk on water and lava", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            lava = bool("Lava", true, "Also walk on lava");
            mode = enumVal("Mode", "Solid", List.of("Solid", "Bounce"), "Solid=teleport to surface, Bounce=push up");
            speedBoost = decimal("Speed", 1.0, 0.5, 2.0, "Water walking speed boost");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.isCrouching() || player.isSpectator()) return;

            // Check a wider area: the block at feet, below feet, and slightly below player Y
            BlockPos feet = player.blockPosition();
            BlockPos below = feet.below();
            // Also check the block right at the player's Y minus a small offset (catches edge cases
            // where player is exactly at block boundary)
            BlockPos atY = BlockPos.containing(player.getX(), player.getY() - 0.1, player.getZ());

            boolean waterContact = false;
            boolean lavaContact = false;
            for (BlockPos check : new BlockPos[]{feet, below, atY}) {
                if (level.getBlockState(check).getFluidState().is(net.minecraft.tags.FluidTags.WATER)) waterContact = true;
                if (lava.getValue() && level.getBlockState(check).getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) lavaContact = true;
            }

            if (waterContact || lavaContact) {
                // Find the surface Y efficiently: start from feet and scan upward
                // Surface = first block above fluid with no fluid
                BlockPos checkPos = feet;
                int maxSearch = 16;
                while (maxSearch-- > 0 && level.getBlockState(checkPos).getFluidState().getAmount() > 0) {
                    checkPos = checkPos.above();
                }
                double surfaceY = checkPos.getY();

                if ("Bounce".equals(mode.getValue())) {
                    if (player.getY() < surfaceY + 0.5) {
                        player.setDeltaMovement(player.getDeltaMovement().x, 0.4, player.getDeltaMovement().z);
                        player.hurtMarked = true;
                    }
                } else {
                    // Solid mode: snap to surface and simulate ground
                    Vec3 vel = player.getDeltaMovement();
                    if (player.getY() < surfaceY - 0.1) {
                        // Below surface: teleport up quickly
                        player.teleportTo(player.getX(), surfaceY, player.getZ());
                        player.setDeltaMovement(vel.x, 0, vel.z);
                    } else if (player.getY() < surfaceY + 0.1) {
                        // At surface: hold position and allow normal ground movement
                        player.teleportTo(player.getX(), surfaceY, player.getZ());
                        player.setDeltaMovement(vel.x, 0, vel.z);
                    } else {
                        // Above surface (jumping): only prevent sinking, don't clamp upward velocity
                        if (vel.y < 0) {
                            player.setDeltaMovement(vel.x, 0, vel.z);
                        }
                    }
                    // Simulate ground so the player can sprint and move normally
                    player.setOnGround(true);
                    player.fallDistance = 0;
                    player.hurtMarked = true;

                    // Apply speed boost for smoother water walking
                    double boost = speedBoost.getValue();
                    if (boost > 1.0) {
                        double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
                        if (horizSpeedSq > 0.0001 && horizSpeedSq < boost * boost * 0.1) {
                            player.setDeltaMovement(player.getDeltaMovement().x * boost, player.getDeltaMovement().y, player.getDeltaMovement().z * boost);
                            player.hurtMarked = true;
                        }
                    }
                }
            }
        }
    }

    // ── Sprint ──────────────────────────────────────────────────────────────
    static class Sprint extends AdminModule {
        private ModuleSetting.BoolSetting omniSprint;
        Sprint() { super("sprint", "Sprint", "Always sprinting", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            omniSprint = bool("OmniSprint", false, "Sprint in all directions");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // player.zza/xxa are CLIENT-SIDE fields and are always 0 on the server.
            // Use actual horizontal velocity to detect movement instead.
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            boolean isMoving = horizSpeedSq > 0.0001;

            if (omniSprint.getValue()) {
                // Sprint in any direction as long as player is moving
                if (!player.isSprinting() && isMoving) {
                    player.setSprinting(true);
                }
            } else {
                // Only sprint when moving forward (check look direction alignment with velocity)
                if (!player.isSprinting() && isMoving) {
                    Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
                    Vec3 moveDir = new Vec3(vel.x, 0, vel.z).normalize();
                    // Dot product > 0.5 means roughly facing forward
                    if (look.dot(moveDir) > 0.5) {
                        player.setSprinting(true);
                    }
                }
            }
        }
    }

    // ── Step ────────────────────────────────────────────────────────────────
    static class Step extends AdminModule {
        private ModuleSetting.DoubleSetting height;
        private static final Identifier STEP_MODIFIER_ID = Identifier.fromNamespaceAndPath("megamod", "module_step");
        Step() { super("step", "Step", "Step up blocks instantly", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() { height = decimal("Height", 1.0, 0.5, 2.5, "Step height"); }
        @Override public void onEnable(ServerPlayer player) {
            applyStepHeight(player);
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Re-apply each tick in case something removed it
            applyStepHeight(player);
        }
        @Override public void onDisable(ServerPlayer player) {
            AttributeHelper.removeModifier(player, Attributes.STEP_HEIGHT, STEP_MODIFIER_ID);
        }
        private void applyStepHeight(ServerPlayer player) {
            // Default step height is 0.6. We want it to be the configured height.
            // So add (target - 0.6) as an ADD_VALUE modifier.
            double bonus = height.getValue() - 0.6;
            AttributeHelper.addModifier(player, Attributes.STEP_HEIGHT, STEP_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_VALUE);
        }
    }

    // ── NoSlow ──────────────────────────────────────────────────────────────
    // Server-side: removes Slowness/Mining Fatigue effects and cobwebs.
    // Client-side: mixin prevents item-use movement slowdown.
    static class NoSlow extends AdminModule {
        NoSlow() { super("no_slow", "NoSlow", "Prevents all slowdown: effects, cobwebs, and item-use speed reduction", ModuleCategory.MOVEMENT); }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.noSlowEnabled = true;
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Remove slow effects
            player.removeEffect(MobEffects.SLOWNESS);
            player.removeEffect(MobEffects.MINING_FATIGUE);
            // Remove cobwebs at player position
            BlockPos pos = player.blockPosition();
            if (level.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                level.destroyBlock(pos, false);
            }
            BlockPos above = pos.above();
            if (level.getBlockState(above).getBlock() == Blocks.COBWEB) {
                level.destroyBlock(above, false);
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.noSlowEnabled = false;
        }
    }

    // ── SafeWalk ────────────────────────────────────────────────────────────
    static class SafeWalk extends AdminModule {
        SafeWalk() { super("safe_walk", "SafeWalk", "Prevents walking off edges", ModuleCategory.MOVEMENT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Simulate sneak-edge behavior: if the player is on ground and moving toward
            // an edge (air below the block ahead), cancel horizontal movement to prevent falling off.
            if (!player.onGround()) return;
            if (player.isCrouching()) return; // Already sneaking, vanilla handles this

            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            if (horizSpeedSq < 0.0001) return;

            // Check the block below where the player would be next tick
            double nextX = player.getX() + vel.x;
            double nextZ = player.getZ() + vel.z;
            BlockPos nextBelow = BlockPos.containing(nextX, player.getY() - 0.5, nextZ);

            // Edge = the block directly below the next position is air (no ground to stand on)
            if (level.getBlockState(nextBelow).isAir()) {
                // Edge detected -- stop horizontal movement
                player.setDeltaMovement(0, vel.y, 0);
                player.hurtMarked = true;
            }
        }
    }

    // ── Spider ──────────────────────────────────────────────────────────────
    static class Spider extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        Spider() { super("spider", "Spider", "Climb any wall like a spider", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.2, 0.1, 0.5, "Climb speed");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Actively check for solid blocks adjacent to the player instead of relying
            // on horizontalCollision which can be unreliable server-side in 1.21.11
            BlockPos pos = player.blockPosition();
            boolean touchingWall = false;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos adjacent = pos.relative(dir);
                if (level.getBlockState(adjacent).isSolidRender()) {
                    touchingWall = true;
                    break;
                }
                // Also check one block up (player is 2 blocks tall)
                BlockPos adjacentUp = adjacent.above();
                if (level.getBlockState(adjacentUp).isSolidRender()) {
                    touchingWall = true;
                    break;
                }
            }

            if (touchingWall && !player.onGround()) {
                Vec3 motion = player.getDeltaMovement();
                double climbSpeed = speed.getValue();
                player.setDeltaMovement(motion.x, climbSpeed, motion.z);
                player.fallDistance = 0;
                player.hurtMarked = true;
            }
        }
    }

    // ── Velocity ────────────────────────────────────────────────────────────
    static class Velocity extends AdminModule {
        private ModuleSetting.DoubleSetting horizontal;
        private ModuleSetting.DoubleSetting vertical;
        // Pending knockback corrections: stored as {playerUUID -> pre-damage velocity}
        // Applied on the next server tick after damage (when knockback has been applied)
        private final Map<UUID, Vec3> pendingCorrections = new HashMap<>();
        Velocity() { super("velocity", "Velocity", "Reduces knockback from attacks", ModuleCategory.MOVEMENT); }
        @Override public boolean isClientSide() { return true; }
        @Override protected void initSettings() {
            horizontal = decimal("Horizontal", 0.0, 0.0, 100.0, "Horizontal knockback multiplier (0=none, 100=full)");
            vertical = decimal("Vertical", 0.0, 0.0, 100.0, "Vertical knockback multiplier (0=none, 100=full)");
        }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.velocityEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.velocityEnabled = false;
            pendingCorrections.remove(player.getUUID());
        }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            // Knockback is applied AFTER the damage event via LivingEntity.knockback().
            // Save pre-damage velocity now and apply correction on the next tick
            // (in onServerTick) when knockback has already been applied.
            pendingCorrections.put(player.getUUID(), player.getDeltaMovement());
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            Vec3 savedVel = pendingCorrections.remove(player.getUUID());
            if (savedVel != null) {
                double hMult = horizontal.getValue() / 100.0;
                double vMult = vertical.getValue() / 100.0;
                Vec3 postKBVel = player.getDeltaMovement();
                // Interpolate between saved velocity and post-knockback velocity
                double newX = savedVel.x + (postKBVel.x - savedVel.x) * hMult;
                double newY = savedVel.y + (postKBVel.y - savedVel.y) * vMult;
                double newZ = savedVel.z + (postKBVel.z - savedVel.z) * hMult;
                player.setDeltaMovement(newX, newY, newZ);
                player.hurtMarked = true;
            }
        }
    }

    // ── ElytraBoost ─────────────────────────────────────────────────────────
    static class ElytraBoost extends AdminModule {
        private ModuleSetting.DoubleSetting power;
        private ModuleSetting.BoolSetting noFallDamage;
        ElytraBoost() { super("elytra_boost", "ElytraBoost", "Boosts elytra flight speed", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            power = decimal("Power", 1.5, 0.5, 5.0, "Boost multiplier");
            noFallDamage = bool("NoFallDamage", true, "Prevent fall/crash damage while boosting");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // player.isFallFlying() works server-side in 1.21.11
            if (player.isFallFlying()) {
                Vec3 look = player.getLookAngle();
                Vec3 vel = player.getDeltaMovement();
                double boost = power.getValue() * 0.05;
                player.setDeltaMovement(vel.add(look.x * boost, look.y * boost + 0.02, look.z * boost));
                if (noFallDamage.getValue()) {
                    player.fallDistance = 0;
                }
                player.hurtMarked = true;
            }
        }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            // Prevent elytra crash damage (FLY_INTO_WALL) while boosting
            if (noFallDamage.getValue() && player.isFallFlying()) {
                if (event.getSource().is(DamageTypes.FLY_INTO_WALL) || event.getSource().is(DamageTypes.FALL)) {
                    event.setNewDamage(0);
                }
            }
        }
    }

    // ── ElytraFly ───────────────────────────────────────────────────────────
    static class ElytraFly extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        ElytraFly() { super("elytra_fly", "ElytraFly", "Elytra flight without fireworks", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 1.5, 0.5, 5.0, "Flight speed");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.isFallFlying()) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.scale(speed.getValue()));
                player.fallDistance = 0;
                player.hurtMarked = true;
            }
        }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            // Prevent crash damage while elytra flying with this module
            if (player.isFallFlying()) {
                if (event.getSource().is(DamageTypes.FLY_INTO_WALL) || event.getSource().is(DamageTypes.FALL)) {
                    event.setNewDamage(0);
                }
            }
        }
    }

    // ── AutoJump ────────────────────────────────────────────────────────────
    static class AutoJump extends AdminModule {
        AutoJump() { super("auto_jump", "AutoJump", "Auto-jumps when walking into blocks", ModuleCategory.MOVEMENT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (!player.onGround()) return;

            // Check actual velocity to see if player is moving
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            if (horizSpeedSq < 0.0001) return;

            // Check for a solid block in the movement direction at feet level
            BlockPos pos = player.blockPosition();
            Vec3 moveDir = new Vec3(vel.x, 0, vel.z).normalize();
            BlockPos ahead = BlockPos.containing(pos.getX() + moveDir.x, pos.getY(), pos.getZ() + moveDir.z);

            if (level.getBlockState(ahead).isSolidRender() || level.getBlockState(ahead.above()).isSolidRender()) {
                player.jumpFromGround();
            }
        }
    }

    // ── LongJump ────────────────────────────────────────────────────────────
    static class LongJump extends AdminModule {
        private ModuleSetting.DoubleSetting power;
        private final Map<UUID, Boolean> wasOnGround = new HashMap<>();
        LongJump() { super("long_jump", "LongJump", "Jump much farther", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() { power = decimal("Power", 2.0, 1.0, 5.0, "Jump distance multiplier"); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            UUID uid = player.getUUID();
            boolean prevOnGround = wasOnGround.getOrDefault(uid, true);
            Vec3 m = player.getDeltaMovement();
            // Only boost ONCE at the start of a jump (transition from ground to air with upward velocity)
            // to prevent exponential speed increase from multiplying every tick
            if (prevOnGround && !player.onGround() && m.y > 0.1) {
                player.setDeltaMovement(m.x * power.getValue(), m.y, m.z * power.getValue());
                player.hurtMarked = true;
            }
            wasOnGround.put(uid, player.onGround());
        }
        @Override public void onDisable(ServerPlayer player) {
            wasOnGround.remove(player.getUUID());
        }
    }

    // ── BoatFly ─────────────────────────────────────────────────────────────
    static class BoatFly extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        private ModuleSetting.BoolSetting noFall;
        BoatFly() { super("boat_fly", "BoatFly", "Fly while in a boat", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.5, 0.1, 2.0, "Flight speed");
            noFall = bool("NoFall", true, "Prevent fall damage when landing");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.isPassenger() && player.getVehicle() != null) {
                // setNoGravity(true) works on boats in 1.21.11 -- it's an Entity field
                player.getVehicle().setNoGravity(true);
                Vec3 look = player.getLookAngle();
                player.getVehicle().setDeltaMovement(look.scale(speed.getValue()));
                // Must mark the vehicle entity as needing sync
                player.getVehicle().hurtMarked = true;
                if (noFall.getValue()) {
                    player.fallDistance = 0;
                    player.getVehicle().fallDistance = 0;
                }
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            if (player.isPassenger() && player.getVehicle() != null) {
                player.getVehicle().setNoGravity(false);
                player.getVehicle().setDeltaMovement(Vec3.ZERO);
                player.getVehicle().hurtMarked = true;
            }
        }
    }

    // ── PhaseWalk ───────────────────────────────────────────────────────────
    static class PhaseWalk extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        PhaseWalk() { super("phase_walk", "PhaseWalk", "Walk through blocks via noclip", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.5, 0.1, 1.0, "Noclip movement speed");
        }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            // Prevent suffocation damage while noclipping through blocks
            if (event.getSource().is(DamageTypes.IN_WALL)) {
                event.setNewDamage(0);
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // noPhysics is an Entity field that exists in 1.21.11 -- disables collision
            player.noPhysics = true;
            // Always apply look-based movement while noclipping. When inside blocks,
            // normal movement is zero because collision stops it, so we must always drive
            // movement from the look direction. The player steers by looking.
            Vec3 look = player.getLookAngle();
            double s = speed.getValue();
            // Check if player has any horizontal velocity (indicates they're pressing movement keys)
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            // Inside solid blocks, velocity is always zero -- apply look-direction movement.
            // Outside blocks with natural velocity, also apply to ensure consistent speed.
            BlockPos pos = player.blockPosition();
            boolean insideSolid = level.getBlockState(pos).isSolidRender()
                || level.getBlockState(pos.above()).isSolidRender();
            if (insideSolid || horizSpeedSq < 0.001) {
                player.setDeltaMovement(look.x * s, look.y * s, look.z * s);
                player.hurtMarked = true;
            }
            // Prevent suffocation damage and fall damage
            player.fallDistance = 0;
        }
        @Override public void onDisable(ServerPlayer player) {
            player.noPhysics = false;
            // Teleport player up if they're inside a solid block to prevent getting stuck
            BlockPos pos = player.blockPosition();
            ServerLevel level = (ServerLevel) player.level();
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) {
                // Find the nearest air space above
                BlockPos safe = pos.above();
                int maxSearch = 64;
                while (maxSearch-- > 0 && (!level.getBlockState(safe).isAir() || !level.getBlockState(safe.above()).isAir())) {
                    safe = safe.above();
                }
                player.teleportTo(player.getX(), safe.getY(), player.getZ());
            }
        }
    }

    // ── ReverseStep ─────────────────────────────────────────────────────────
    static class ReverseStep extends AdminModule {
        private ModuleSetting.DoubleSetting fallSpeed;
        ReverseStep() { super("reverse_step", "ReverseStep", "Instant step down from blocks", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            fallSpeed = decimal("Speed", 0.5, 0.3, 1.5, "Step-down speed");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Only apply at edges: player is on ground, not jumping, and moving
            if (!player.onGround() || player.getDeltaMovement().y > 0) return;

            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            if (horizSpeedSq < 0.0001) return; // Not moving, don't force down

            // Check the block directly below where the player currently is -- if feet are
            // at a block edge, check 1 block ahead in movement direction (scaled to ~1 block)
            Vec3 moveDir = new Vec3(vel.x, 0, vel.z).normalize();
            BlockPos ahead = BlockPos.containing(
                player.getX() + moveDir.x * 0.8,
                player.getY() - 1,
                player.getZ() + moveDir.z * 0.8
            );
            // Only step down if the block 1 below ahead is air (edge detected)
            // but there IS a solid block further down (not a cliff/void)
            if (level.getBlockState(ahead).isAir()) {
                BlockPos twoBelow = ahead.below();
                boolean hasSolidBelow = !level.getBlockState(twoBelow).isAir();
                if (hasSolidBelow) {
                    player.setDeltaMovement(vel.x, -fallSpeed.getValue(), vel.z);
                    player.fallDistance = 0;
                    player.hurtMarked = true;
                }
            }
        }
    }

    // ── Parkour ─────────────────────────────────────────────────────────────
    static class Parkour extends AdminModule {
        Parkour() { super("parkour", "Parkour", "Auto-jumps at block edges", ModuleCategory.MOVEMENT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Use actual velocity instead of player.zza (which is always 0 on server)
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            if (player.onGround() && horizSpeedSq > 0.0001) {
                // Use movement direction instead of facing direction for more accurate edge detection
                Vec3 moveDir = new Vec3(vel.x, 0, vel.z).normalize();
                BlockPos ahead = BlockPos.containing(
                    player.getX() + moveDir.x * 0.8,
                    player.getY(),
                    player.getZ() + moveDir.z * 0.8
                );
                if (level.getBlockState(ahead.below()).isAir()) {
                    player.jumpFromGround();
                }
            }
        }
    }

    // ── TridentBoost ────────────────────────────────────────────────────────
    static class TridentBoost extends AdminModule {
        private ModuleSetting.DoubleSetting power;
        TridentBoost() { super("trident_boost", "TridentBoost", "Enhanced trident riptide boost", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() { power = decimal("Power", 3.0, 1.0, 8.0, "Boost multiplier"); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            boolean holdingTrident = player.getMainHandItem().is(Items.TRIDENT) || player.getOffhandItem().is(Items.TRIDENT);
            if (holdingTrident && (player.isInWater() || level.isRainingAt(player.blockPosition()))) {
                // Only apply the boost if the player is actively using (charging) the trident,
                // and cap max speed to prevent exponential acceleration
                if (player.isUsingItem()) {
                    Vec3 vel = player.getDeltaMovement();
                    double currentSpeedSq = vel.lengthSqr();
                    double maxSpeed = power.getValue() * 2.0;
                    if (currentSpeedSq < maxSpeed * maxSpeed) {
                        Vec3 look = player.getLookAngle();
                        double p = power.getValue() * 0.1; // Scale down per-tick to prevent runaway
                        Vec3 boost = new Vec3(look.x * p, look.y * p * 0.5, look.z * p);
                        player.setDeltaMovement(vel.add(boost));
                        player.hurtMarked = true;
                    }
                }
            }
        }
    }

    // ── TickShift ───────────────────────────────────────────────────────────
    static class TickShift extends AdminModule {
        private ModuleSetting.IntSetting multiplier;
        TickShift() { super("tick_shift", "TickShift", "Speeds up player tick processing", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() { multiplier = integer("Multiplier", 2, 1, 5, "Movement ticks per game tick"); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            Vec3 vel = player.getDeltaMovement();
            double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
            if (horizSpeedSq < 0.0001 && Math.abs(vel.y) < 0.001) return; // Not moving
            int extra = multiplier.getValue() - 1;
            if (extra <= 0) return;
            // Apply extra movement ticks by teleporting along the velocity vector.
            // This effectively multiplies movement speed without changing the velocity itself.
            double totalDx = vel.x * extra;
            double totalDy = vel.y * extra;
            double totalDz = vel.z * extra;
            player.teleportTo(player.getX() + totalDx, player.getY() + totalDy, player.getZ() + totalDz);
            player.fallDistance = 0; // Prevent fall damage from rapid vertical teleporting
            player.hurtMarked = true;
        }
    }

    // ── Blink ───────────────────────────────────────────────────────────────
    static class Blink extends AdminModule {
        private ModuleSetting.DoubleSetting distance;
        Blink() { super("blink", "Blink", "Teleports to where you're looking", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            distance = decimal("Distance", 8.0, 1.0, 32.0, "Teleport distance");
        }
        @Override public void onEnable(ServerPlayer player) {
            // player.pick() works server-side in 1.21.11
            HitResult hit = player.pick(distance.getValue(), 0.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                Vec3 pos = blockHit.getLocation();
                player.teleportTo(pos.x, pos.y + 1.0, pos.z);
                player.fallDistance = 0;
            } else {
                // No block hit -- teleport forward by distance along look direction
                Vec3 look = player.getLookAngle();
                double dist = distance.getValue();
                player.teleportTo(player.getX() + look.x * dist, player.getY() + look.y * dist, player.getZ() + look.z * dist);
                player.fallDistance = 0;
            }
            setEnabled(false);
        }
    }

    // ── EntitySpeed ─────────────────────────────────────────────────────────
    static class EntitySpeed extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        EntitySpeed() { super("entity_speed", "EntitySpeed", "Increases mount speed", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 1.5, 0.5, 5.0, "Speed multiplier applied to mount's existing velocity");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.isPassenger() && player.getVehicle() != null) {
                // Multiply the mount's existing velocity rather than overriding it.
                // This respects the mount's own steering (horses, boats, etc.) and only
                // amplifies speed when the mount is actually moving.
                Vec3 mountVel = player.getVehicle().getDeltaMovement();
                double horizSpeedSq = mountVel.x * mountVel.x + mountVel.z * mountVel.z;
                if (horizSpeedSq > 0.0001) {
                    double mult = speed.getValue();
                    player.getVehicle().setDeltaMovement(mountVel.x * mult, mountVel.y, mountVel.z * mult);
                    player.getVehicle().hurtMarked = true;
                }
            }
        }
    }

    // ── IceSpeed ────────────────────────────────────────────────────────────
    static class IceSpeed extends AdminModule {
        IceSpeed() { super("ice_speed", "IceSpeed", "Extreme speed on ice blocks", ModuleCategory.MOVEMENT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            BlockPos below = player.blockPosition().below();
            if (level.getBlockState(below).is(Blocks.ICE) || level.getBlockState(below).is(Blocks.PACKED_ICE) || level.getBlockState(below).is(Blocks.BLUE_ICE)) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 20, 4, false, false));
            }
        }
    }

    // ── AirJump ─────────────────────────────────────────────────────────────
    static class AirJump extends AdminModule {
        private ModuleSetting.IntSetting maxJumps;
        private final Map<UUID, Integer> jumpCounts = new HashMap<>();
        private final Map<UUID, Double> lastYVel = new HashMap<>();
        AirJump() { super("air_jump", "AirJump", "Jump in mid-air (double jump)", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            maxJumps = integer("MaxJumps", 1, 1, 5, "Number of extra air jumps");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            UUID uid = player.getUUID();
            Vec3 vel = player.getDeltaMovement();
            double prevY = lastYVel.getOrDefault(uid, 0.0);

            if (player.onGround()) {
                // Reset jump count when on ground
                jumpCounts.put(uid, 0);
            } else {
                int jumps = jumpCounts.getOrDefault(uid, 0);
                // Detect a jump input in mid-air: the client sends a position packet with
                // upward velocity when the player presses jump. We detect this as a transition
                // from falling/neutral (prevY <= 0) to rising (vel.y > 0), OR as a sudden
                // upward velocity increase while already in air (vel.y - prevY > 0.3).
                boolean jumpDetected = (prevY <= 0 && vel.y > 0.1)
                    || (vel.y - prevY > 0.3 && vel.y > 0);

                if (jumpDetected && jumps < maxJumps.getValue()) {
                    player.setDeltaMovement(vel.x, 0.42, vel.z);
                    player.hurtMarked = true;
                    player.fallDistance = 0;
                    jumpCounts.put(uid, jumps + 1);
                }
            }
            lastYVel.put(uid, vel.y);
        }
        @Override public void onDisable(ServerPlayer player) {
            jumpCounts.remove(player.getUUID());
            lastYVel.remove(player.getUUID());
        }
    }

    // ── FastClimb ───────────────────────────────────────────────────────────
    static class FastClimb extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        FastClimb() { super("fast_climb", "FastClimb", "Climb ladders faster", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.35, 0.1, 1.0, "Climb speed");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // onClimbable() works server-side -- checks if entity is in a ladder/vine block
            if (player.onClimbable()) {
                Vec3 motion = player.getDeltaMovement();
                // Respect the player's intended direction: if crouching, stay in place (vanilla behavior).
                // If natural Y velocity is negative (descending), boost downward speed instead.
                if (player.isCrouching()) {
                    // Crouching on ladder = stay in place (vanilla), don't override
                    return;
                }
                if (motion.y >= 0) {
                    // Moving up or stationary -- boost upward
                    player.setDeltaMovement(motion.x, speed.getValue(), motion.z);
                } else {
                    // Moving down -- boost downward for faster descent
                    player.setDeltaMovement(motion.x, -speed.getValue(), motion.z);
                }
                player.hurtMarked = true;
            }
        }
    }

    // ── AntiVoid ────────────────────────────────────────────────────────────
    static class AntiVoid extends AdminModule {
        private ModuleSetting.IntSetting height;
        AntiVoid() { super("anti_void", "AntiVoid", "Prevents falling into the void", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            height = integer("Height", 64, 1, 128, "Safe teleport height");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.getY() < level.getMinY() - 5) {
                player.teleportTo(player.getX(), level.getMinY() + height.getValue(), player.getZ());
                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0;
                player.hurtMarked = true;
            }
        }
    }

    // ── AutoWalk ────────────────────────────────────────────────────────────
    static class AutoWalk extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        AutoWalk() { super("auto_walk", "AutoWalk", "Automatically walks forward", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.2, 0.05, 1.0, "Walk speed");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
            player.setDeltaMovement(look.scale(speed.getValue()).add(0, player.getDeltaMovement().y, 0));
            player.hurtMarked = true;
        }
    }

    // ── GUIMove ─────────────────────────────────────────────────────────────
    // Client-side: mixin re-enables WASD input while screens are open.
    // Server-side: fallback momentum push when container menu is open.
    static class GUIMove extends AdminModule {
        GUIMove() { super("gui_move", "GUIMove", "WASD movement works while GUIs/inventories are open", ModuleCategory.MOVEMENT); }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.guiMoveEnabled = true;
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // The actual GUI movement is handled client-side by the mixin (AdminModuleState.guiMoveEnabled).
            // Server-side: just ensure the player isn't forcefully stopped while a container is open.
            // We don't push the player in any direction -- we just preserve existing momentum.
            if (player.containerMenu != player.inventoryMenu) {
                // The server normally clamps movement when a container is open.
                // By re-marking the player as needing sync, we help ensure client-driven
                // movement packets are respected.
                player.hurtMarked = true;
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.guiMoveEnabled = false;
        }
    }

    // ── HighJump ────────────────────────────────────────────────────────────
    static class HighJump extends AdminModule {
        private ModuleSetting.DoubleSetting power;
        private final Map<UUID, Boolean> wasOnGround = new HashMap<>();
        HighJump() { super("high_jump", "HighJump", "Multiplies jump height", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() { power = decimal("Power", 2.0, 1.5, 5.0, "Jump height multiplier"); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            UUID uid = player.getUUID();
            boolean prevOnGround = wasOnGround.getOrDefault(uid, true);
            Vec3 vel = player.getDeltaMovement();
            if (prevOnGround && !player.onGround() && vel.y > 0.1) {
                player.setDeltaMovement(vel.x, vel.y * power.getValue(), vel.z);
                player.hurtMarked = true;
            }
            wasOnGround.put(uid, player.onGround());
        }
        @Override public void onDisable(ServerPlayer player) {
            wasOnGround.remove(player.getUUID());
        }
    }

    // ── Sneak ───────────────────────────────────────────────────────────────
    static class Sneak extends AdminModule {
        Sneak() { super("sneak", "Sneak", "Force permanent sneaking", ModuleCategory.MOVEMENT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            player.setShiftKeyDown(true);
        }
        @Override public void onDisable(ServerPlayer player) {
            player.setShiftKeyDown(false);
        }
    }

    // ── ClickTP ─────────────────────────────────────────────────────────────
    static class ClickTP extends AdminModule {
        ClickTP() { super("click_tp", "ClickTP", "Teleport to where you're looking (200 blocks)", ModuleCategory.MOVEMENT); }
        @Override public void onEnable(ServerPlayer player) {
            // player.pick() works server-side in 1.21.11
            HitResult hit = player.pick(200.0, 0.0f, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                Vec3 pos = blockHit.getLocation();
                player.teleportTo(pos.x, pos.y + 1.0, pos.z);
                player.fallDistance = 0;
            }
            setEnabled(false);
        }
    }

    // ── EntityControl ───────────────────────────────────────────────────────
    static class EntityControl extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        EntityControl() { super("entity_control", "EntityControl", "Steer any ridden entity with look direction", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.5, 0.1, 2.0, "Entity movement speed");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.isPassenger() && player.getVehicle() != null) {
                Vec3 look = player.getLookAngle();
                // Apply speed in look direction, including vertical component for flying mounts
                player.getVehicle().setDeltaMovement(look.scale(speed.getValue()));
                player.getVehicle().hurtMarked = true;
            }
        }
    }

    // ── Anchor ──────────────────────────────────────────────────────────────
    static class Anchor extends AdminModule {
        private final Map<UUID, Double> savedY = new HashMap<>();
        Anchor() { super("anchor", "Anchor", "Lock Y position to current height", ModuleCategory.MOVEMENT); }
        @Override public void onEnable(ServerPlayer player) {
            savedY.put(player.getUUID(), player.getY());
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            Double lockY = savedY.get(player.getUUID());
            if (lockY != null && Math.abs(player.getY() - lockY) > 0.05) {
                player.teleportTo(player.getX(), lockY, player.getZ());
                player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                player.hurtMarked = true;
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            savedY.remove(player.getUUID());
        }
    }

    // ── Slippy ──────────────────────────────────────────────────────────────
    static class Slippy extends AdminModule {
        private ModuleSetting.DoubleSetting friction;
        Slippy() { super("slippy", "Slippy", "Ice-like sliding on all blocks", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            friction = decimal("Friction", 1.5, 1.0, 3.0, "Momentum preservation factor (higher = more slippery)");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.onGround()) {
                Vec3 vel = player.getDeltaMovement();
                double horizSpeedSq = vel.x * vel.x + vel.z * vel.z;
                if (horizSpeedSq < 0.0001) return; // Not moving, nothing to preserve
                // MC applies friction ~0.6 for normal blocks. To simulate ice (0.98 friction),
                // we need to counteract the normal friction. We boost horizontal velocity by the
                // friction factor to preserve momentum across ticks.
                double f = friction.getValue();
                player.setDeltaMovement(vel.x * f, vel.y, vel.z * f);
                player.hurtMarked = true;
            }
        }
    }

    // ── AutoWasp ────────────────────────────────────────────────────────────
    static class AutoWasp extends AdminModule {
        private ModuleSetting.DoubleSetting height;
        private final Map<UUID, Integer> tickCounters = new HashMap<>();
        AutoWasp() { super("auto_wasp", "AutoWasp", "Wasp-like hovering by rapidly toggling flight", ModuleCategory.MOVEMENT); }
        @Override protected void initSettings() {
            height = decimal("Height", 0.4, 0.2, 1.0, "Hover amplitude");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            UUID uid = player.getUUID();
            int tick = tickCounters.getOrDefault(uid, 0);
            Vec3 vel = player.getDeltaMovement();
            if (tick % 3 == 0) {
                player.setDeltaMovement(vel.x, height.getValue(), vel.z);
            }
            player.fallDistance = 0;
            player.hurtMarked = true;
            tickCounters.put(uid, tick + 1);
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounters.remove(player.getUUID());
        }
    }
}
