package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.huts.BlockHutTownHall;
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
 * Ultrasafe Colony Group Teleport Scroll.
 * Shift+right-click on a Town Hall hut block to register.
 * Right-click (hold) to teleport player AND all players within 8 blocks to the registered Town Hall.
 * 5% chance to fail (scroll still consumed).
 * Stacks to 16.
 */
public class ItemScrollAreaTP extends Item {

    private static final int USE_DURATION = 32;
    private static final double GROUP_RADIUS = 8.0;

    public ItemScrollAreaTP(Properties properties) {
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

    // Shift+right-click on Town Hall block to register
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
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("TownHallX", clickedPos.getX());
                    tag.putInt("TownHallY", clickedPos.getY());
                    tag.putInt("TownHallZ", clickedPos.getZ());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                    ((ServerPlayer) player).displayClientMessage(Component.literal(
                        "\u00A7a\u00A7l\u2714 \u00A76Scroll registered to Town Hall at ("
                        + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ() + ")"), false);
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
        BlockPos townHallPos = getTownHallPos(stack);

        if (townHallPos == null) {
            if (!level.isClientSide()) {
                ((ServerPlayer) player).displayClientMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76You must register this scroll to a Town Hall first!"), false);
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
            BlockPos townHallPos = getTownHallPos(stack);

            // Consume one scroll regardless
            stack.shrink(1);

            if (townHallPos == null) {
                player.displayClientMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76You must register this scroll to a Town Hall first!"), false);
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

            // Gather all players within 8 blocks of the user
            AABB searchBox = player.getBoundingBox().inflate(GROUP_RADIUS);
            List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(
                ServerPlayer.class, searchBox);

            // Teleport all nearby players (including the user)
            double destX = townHallPos.getX() + 0.5;
            double destY = townHallPos.getY() + 1;
            double destZ = townHallPos.getZ() + 0.5;

            int teleported = 0;
            for (ServerPlayer nearby : nearbyPlayers) {
                // Spawn particles at origin
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                    nearby.getX(), nearby.getY() + 1, nearby.getZ(),
                    32, 0.5, 0.5, 0.5, 0.1);

                nearby.teleportTo(serverLevel,
                    destX, destY, destZ,
                    java.util.Set.of(), nearby.getYRot(), nearby.getXRot(), true);
                teleported++;

                if (!nearby.equals(player)) {
                    nearby.displayClientMessage(Component.literal(
                        "\u00A7a\u00A7l\u2714 \u00A76You were teleported to a Town Hall by " + player.getGameProfile().name() + "!"), false);
                }
            }

            // Particles and sound at origin
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0f, 1.0f);

            // Particles and sound at destination
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                destX, destY + 0.5, destZ,
                48, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, townHallPos, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0f, 1.0f);

            player.displayClientMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A76Group teleported " + teleported + " player(s) to the Town Hall!"), false);
        }

        return stack;
    }

    private static BlockPos getTownHallPos(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("TownHallX")) return null;
        return new BlockPos(
            tag.getIntOr("TownHallX", 0),
            tag.getIntOr("TownHallY", 0),
            tag.getIntOr("TownHallZ", 0)
        );
    }
}
