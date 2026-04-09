/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Rotations
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 */
package com.ultra.megamod.feature.armorstand;

import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="megamod")
public class BetterArmorStands {
    private static final String POSE_INDEX_TAG = "megamod:pose_index";
    private static final String TOOL_RACK_TAG = "megamod:tool_rack";
    private static final String ARMS_INITIALIZED_TAG = "megamod:arms_initialized";
    private static final Rotations[][] POSES = new Rotations[][]{{new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f)}, {new Rotations(-5.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(-70.0f, 0.0f, 10.0f), new Rotations(-70.0f, 0.0f, -10.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f)}, {new Rotations(-5.0f, 10.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(-120.0f, 30.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f)}, {new Rotations(0.0f, 20.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(-90.0f, 18.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f)}, {new Rotations(-2.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(-40.0f, 30.0f, 20.0f), new Rotations(-40.0f, -30.0f, -20.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f)}, {new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, -90.0f), new Rotations(0.0f, 0.0f, 90.0f), new Rotations(0.0f, 0.0f, 0.0f), new Rotations(0.0f, 0.0f, 0.0f)}};

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ArmorStand armorStand = (ArmorStand)entity;
        if (armorStand.getPersistentData().getBooleanOr(ARMS_INITIALIZED_TAG, false)) {
            return;
        }
        armorStand.setShowArms(true);
        armorStand.getPersistentData().putBoolean(ARMS_INITIALIZED_TAG, true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }
        BlockPos leverPos = event.getPos();
        BlockState state = level.getBlockState(leverPos);
        if (!(state.getBlock() instanceof LeverBlock)) {
            return;
        }
        boolean powered = (Boolean)state.getValue((Property)BlockStateProperties.POWERED);
        AABB searchBox = new AABB(leverPos).inflate(3.0);
        List<ArmorStand> armorStands = level.getEntitiesOfClass(ArmorStand.class, searchBox);
        if (armorStands.isEmpty()) {
            return;
        }
        ArmorStand nearest = armorStands.stream().min(Comparator.comparingDouble(as -> as.distanceToSqr((double)leverPos.getX() + 0.5, (double)leverPos.getY() + 0.5, (double)leverPos.getZ() + 0.5))).orElse(null);
        if (nearest == null) {
            return;
        }
        int currentIndex = nearest.getPersistentData().getIntOr(POSE_INDEX_TAG, 0);
        int nextIndex = (currentIndex + 1) % POSES.length;
        nearest.getPersistentData().putInt(POSE_INDEX_TAG, nextIndex);
        BetterArmorStands.applyPose(nearest, POSES[nextIndex]);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getTarget();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        ArmorStand armorStand = (ArmorStand)entity;
        ItemStack heldItem = player2.getMainHandItem();
        if (!heldItem.is(Items.TRIPWIRE_HOOK)) {
            return;
        }
        boolean isToolRack = armorStand.getPersistentData().getBooleanOr(TOOL_RACK_TAG, false);
        if (!isToolRack) {
            armorStand.getPersistentData().putBoolean(TOOL_RACK_TAG, true);
            BetterArmorStands.setArmorStandSmall(armorStand, true);
            armorStand.setInvisible(true);
            armorStand.setNoGravity(true);
            if (!player2.isCreative()) {
                heldItem.shrink(1);
            }
        } else {
            armorStand.getPersistentData().putBoolean(TOOL_RACK_TAG, false);
            BetterArmorStands.setArmorStandSmall(armorStand, false);
            armorStand.setInvisible(false);
            armorStand.setNoGravity(false);
        }
        event.setCanceled(true);
    }

    private static void setArmorStandSmall(ArmorStand armorStand, boolean small) {
        byte flags = (Byte)armorStand.getEntityData().get(ArmorStand.DATA_CLIENT_FLAGS);
        flags = small ? (byte)(flags | 1) : (byte)(flags & 0xFFFFFFFE);
        armorStand.getEntityData().set(ArmorStand.DATA_CLIENT_FLAGS, flags);
    }

    private static void applyPose(ArmorStand armorStand, Rotations[] pose) {
        armorStand.setHeadPose(pose[0]);
        armorStand.setBodyPose(pose[1]);
        armorStand.setLeftArmPose(pose[2]);
        armorStand.setRightArmPose(pose[3]);
        armorStand.setLeftLegPose(pose[4]);
        armorStand.setRightLegPose(pose[5]);
    }
}

