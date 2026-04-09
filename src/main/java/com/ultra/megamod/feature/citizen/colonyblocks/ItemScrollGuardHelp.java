package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.huts.BlockHutGuardTower;
import com.ultra.megamod.feature.citizen.building.huts.BlockHutTownHall;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Spatial Guard Reinforcements Scroll.
 * Must register to BOTH a Town Hall AND a Guard Tower (two separate shift+clicks).
 * Right-click (hold) to summon guards from the registered Guard Tower to the player for 5 minutes.
 * After 5 minutes (6000 ticks), guards return to their tower.
 * 5% chance to fail. Consumed on use.
 * Stacks to 16.
 */
public class ItemScrollGuardHelp extends Item {

    private static final int USE_DURATION = 32;
    private static final double SEARCH_RADIUS = 128.0;
    private static final int RETURN_DELAY_TICKS = 6000; // 5 minutes

    public ItemScrollGuardHelp(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    // Shift+right-click on Town Hall or Guard Tower to register
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            BlockPos clickedPos = context.getClickedPos();
            BlockState state = level.getBlockState(clickedPos);

            if (state.getBlock() instanceof BlockHutTownHall) {
                if (!level.isClientSide()) {
                    ItemStack stack = context.getItemInHand();
                    CompoundTag existing = getOrCreateTag(stack);
                    existing.putInt("TownHallX", clickedPos.getX());
                    existing.putInt("TownHallY", clickedPos.getY());
                    existing.putInt("TownHallZ", clickedPos.getZ());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(existing));

                    ((ServerPlayer) player).displayClientMessage(Component.literal(
                        "\u00A7a\u00A7l\u2714 \u00A76Town Hall registered at ("
                        + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ() + ")"), false);

                    if (!existing.contains("GuardTowerX")) {
                        ((ServerPlayer) player).displayClientMessage(Component.literal(
                            "\u00A7e\u00A7l\u26A0 \u00A76Now shift+right-click on a Guard Tower to complete registration."), false);
                    }
                }
                return InteractionResult.SUCCESS;
            }

            if (state.getBlock() instanceof BlockHutGuardTower) {
                if (!level.isClientSide()) {
                    ItemStack stack = context.getItemInHand();
                    CompoundTag existing = getOrCreateTag(stack);
                    existing.putInt("GuardTowerX", clickedPos.getX());
                    existing.putInt("GuardTowerY", clickedPos.getY());
                    existing.putInt("GuardTowerZ", clickedPos.getZ());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(existing));

                    ((ServerPlayer) player).displayClientMessage(Component.literal(
                        "\u00A7a\u00A7l\u2714 \u00A76Guard Tower registered at ("
                        + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ() + ")"), false);

                    if (!existing.contains("TownHallX")) {
                        ((ServerPlayer) player).displayClientMessage(Component.literal(
                            "\u00A7e\u00A7l\u26A0 \u00A76Now shift+right-click on a Town Hall to complete registration."), false);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = getTagOrNull(stack);

        if (tag == null || !tag.contains("TownHallX") || !tag.contains("GuardTowerX")) {
            if (!level.isClientSide()) {
                String missing = "";
                if (tag == null || !tag.contains("TownHallX")) {
                    missing = "a Town Hall";
                }
                if (tag == null || !tag.contains("GuardTowerX")) {
                    if (!missing.isEmpty()) missing += " and ";
                    missing += "a Guard Tower";
                }
                ((ServerPlayer) player).displayClientMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76You must register this scroll to " + missing + " first!"), false);
            }
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide()) {
            return stack;
        }

        if (entity instanceof ServerPlayer player) {
            ServerLevel serverLevel = (ServerLevel) level;
            CompoundTag tag = getTagOrNull(stack);

            // Consume scroll regardless
            stack.shrink(1);

            if (tag == null || !tag.contains("TownHallX") || !tag.contains("GuardTowerX")) {
                player.displayClientMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76Scroll not fully registered!"), false);
                return stack;
            }

            // 5% chance to fail
            if (level.getRandom().nextFloat() < 0.05f) {
                player.displayClientMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76The scroll fizzles and fails!"), false);
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 0.5, 0.5, 0.5, 0.05);
                level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
                return stack;
            }

            BlockPos guardTowerPos = new BlockPos(
                tag.getIntOr("GuardTowerX", 0),
                tag.getIntOr("GuardTowerY", 0),
                tag.getIntOr("GuardTowerZ", 0)
            );

            // Find guards near the guard tower that belong to the player
            AABB searchBox = new AABB(guardTowerPos).inflate(SEARCH_RADIUS);
            List<MCEntityCitizen> recruits = serverLevel.getEntitiesOfClass(
                MCEntityCitizen.class, searchBox,
                recruit -> true // TODO: filter by owner via colony handler
            );

            if (recruits.isEmpty()) {
                player.displayClientMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76No guards found near the registered Guard Tower!"), false);
                return stack;
            }

            // Move guards to the player's position for 5 minutes
            BlockPos playerPos = player.blockPosition();
            long returnTick = serverLevel.getGameTime() + RETURN_DELAY_TICKS;

            for (MCEntityCitizen recruit : recruits) {
                // Move to player's location
                recruit.getNavigation().moveTo(
                    player.getX(), player.getY(), player.getZ(), 1.2);
            }

            // Schedule guard return (store return info in recruit entities via custom persistent data)
            // We use a simple approach: the recruits will naturally return when their hold pos is cleared
            // Schedule a delayed task to reset hold positions after 5 minutes
            serverLevel.getServer().execute(() -> {
                scheduleGuardReturn(serverLevel, recruits, guardTowerPos, returnTick);
            });

            // Visual/audio feedback
            serverLevel.sendParticles(ParticleTypes.NOTE,
                player.getX(), player.getY() + 2, player.getZ(),
                8, 0.5, 0.3, 0.5, 0.0);
            level.playSound(null, player.blockPosition(),
                (net.minecraft.sounds.SoundEvent) SoundEvents.RAID_HORN.value(),
                SoundSource.PLAYERS, 1.0f, 1.0f);

            player.displayClientMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A76" + recruits.size() + " guard(s) summoned to your position for 5 minutes!"), false);
        }

        return stack;
    }

    /**
     * Schedules guards to return to their tower after the delay.
     * Uses a tick-checking approach since we can't schedule arbitrary future tasks easily.
     */
    private void scheduleGuardReturn(ServerLevel level, List<MCEntityCitizen> recruits,
                                      BlockPos towerPos, long returnTick) {
        // Store return info on each recruit
        for (MCEntityCitizen recruit : recruits) {
            recruit.getPersistentData().putLong("ScrollReturnTick", returnTick);
            recruit.getPersistentData().putLong("ScrollReturnPos", towerPos.asLong());
        }
    }

    private static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            return customData.copyTag();
        }
        return new CompoundTag();
    }

    private static CompoundTag getTagOrNull(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        return customData.copyTag();
    }
}
