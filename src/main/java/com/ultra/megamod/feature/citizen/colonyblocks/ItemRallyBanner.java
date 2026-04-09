package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;
import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Rally Banner — multi-tower registration system matching MegaColonies spec:
 *
 *   Shift+right-click on Guard Tower / Barracks Tower → toggle registration (add/remove)
 *   Right-click in air → rally all guards from registered towers to player's position
 *   Shift+right-click in air → dismiss guards (return to their posts)
 *
 * Supports multiple registered towers stored in the item's CUSTOM_DATA.
 * Guards within 128 blocks of their registered tower will respond to rally commands.
 */
public class ItemRallyBanner extends Item {

    private static final double GUARD_SEARCH_RADIUS = 128.0;
    private static final int MAX_TOWERS = 20;

    public ItemRallyBanner(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || level.isClientSide()) return InteractionResult.SUCCESS;

        // Only shift+right-click to register/unregister towers
        if (!player.isShiftKeyDown()) return InteractionResult.PASS;

        BlockPos clickedPos = context.getClickedPos();
        ServerPlayer sp = (ServerPlayer) player;

        // Check if it's a Guard Tower or Barracks Tower
        String buildingId = null;
        BlockEntity be = level.getBlockEntity(clickedPos);
        if (be instanceof TileEntityColonyBuilding tile) {
            buildingId = tile.getBuildingId();
        }
        if (buildingId == null && level.getBlockState(clickedPos).getBlock() instanceof AbstractBlockHut<?> hut) {
            buildingId = hut.getBuildingId();
        }

        if (buildingId == null || (!buildingId.equals("guard_tower") && !buildingId.equals("barracks_tower")
                && !buildingId.equals("barracks"))) {
            sp.displayClientMessage(Component.literal("Must target a Guard Tower or Barracks!")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        // Get or create the tower list
        List<BlockPos> towers = loadTowers(tag);

        // Toggle this tower
        boolean removed = towers.removeIf(pos -> pos.equals(clickedPos));
        if (removed) {
            sp.displayClientMessage(Component.literal("Removed " + formatBuildingName(buildingId)
                    + " at (" + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ() + ")")
                    .withStyle(ChatFormatting.YELLOW), false);
            level.playSound(null, clickedPos, SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    SoundSource.PLAYERS, 1.0f, 0.8f);
        } else {
            if (towers.size() >= MAX_TOWERS) {
                sp.displayClientMessage(Component.literal("Maximum " + MAX_TOWERS + " towers can be registered!")
                        .withStyle(ChatFormatting.RED), false);
                return InteractionResult.CONSUME;
            }
            towers.add(clickedPos);
            sp.displayClientMessage(Component.literal("Registered " + formatBuildingName(buildingId)
                    + " at (" + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ() + ")")
                    .withStyle(ChatFormatting.GREEN), false);
            level.playSound(null, clickedPos, SoundEvents.ITEM_FRAME_ADD_ITEM,
                    SoundSource.PLAYERS, 1.0f, 1.2f);
        }

        sp.displayClientMessage(Component.literal("Towers registered: " + towers.size())
                .withStyle(ChatFormatting.GRAY), false);

        // Save back
        saveTowers(tag, towers);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ServerPlayer sp = (ServerPlayer) player;
        ServerLevel sl = (ServerLevel) level;

        List<BlockPos> towers = loadTowers(tag);

        if (towers.isEmpty()) {
            sp.displayClientMessage(Component.literal("No towers registered! Shift+right-click on Guard Towers or Barracks to register them.")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            // DISMISS — return guards to their posts
            return dismissGuards(sl, sp, towers, tag, stack);
        } else {
            // RALLY — move guards to player position
            return rallyGuards(sl, sp, towers, tag, stack);
        }
    }

    private InteractionResult rallyGuards(ServerLevel level, ServerPlayer player, List<BlockPos> towers,
                                           CompoundTag tag, ItemStack stack) {
        BlockPos rallyPoint = player.blockPosition();
        int rallied = 0;

        for (BlockPos towerPos : towers) {
            // Search for guards near each registered tower
            AABB searchBox = new AABB(towerPos).inflate(GUARD_SEARCH_RADIUS);
            List<MCEntityCitizen> guards = level.getEntitiesOfClass(
                    MCEntityCitizen.class, searchBox,
                    recruit -> true // TODO: filter by owner via colony handler
            );

            for (MCEntityCitizen guard : guards) {
                guard.getNavigation().moveTo(
                        rallyPoint.getX() + 0.5, rallyPoint.getY(), rallyPoint.getZ() + 0.5, 1.2);
                rallied++;
            }
        }

        // Store rally state
        tag.putBoolean("rallied", true);
        tag.putInt("rallyX", rallyPoint.getX());
        tag.putInt("rallyY", rallyPoint.getY());
        tag.putInt("rallyZ", rallyPoint.getZ());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        // Visual/audio feedback
        level.sendParticles(ParticleTypes.FLAME,
                rallyPoint.getX() + 0.5, rallyPoint.getY() + 0.5, rallyPoint.getZ() + 0.5,
                24, 0.5, 1.0, 0.5, 0.02);
        level.sendParticles(ParticleTypes.END_ROD,
                rallyPoint.getX() + 0.5, rallyPoint.getY() + 2.0, rallyPoint.getZ() + 0.5,
                16, 0.2, 0.5, 0.2, 0.01);
        level.playSound(null, rallyPoint,
                (net.minecraft.sounds.SoundEvent) SoundEvents.RAID_HORN.value(),
                SoundSource.PLAYERS, 1.0f, 1.2f);

        if (rallied > 0) {
            player.displayClientMessage(Component.literal("\u00A7a\u00A7l\u2714 Rally! " + rallied
                    + " guard(s) moving to your position from " + towers.size() + " tower(s).")
                    .withStyle(ChatFormatting.GREEN), false);
        } else {
            player.displayClientMessage(Component.literal("Rally point set, but no guards found near registered towers.")
                    .withStyle(ChatFormatting.YELLOW), false);
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult dismissGuards(ServerLevel level, ServerPlayer player, List<BlockPos> towers,
                                             CompoundTag tag, ItemStack stack) {
        int dismissed = 0;

        for (BlockPos towerPos : towers) {
            AABB searchBox = new AABB(towerPos).inflate(GUARD_SEARCH_RADIUS * 2); // Wider search for dismiss
            List<MCEntityCitizen> guards = level.getEntitiesOfClass(
                    MCEntityCitizen.class, searchBox,
                    recruit -> true // TODO: filter by owner via colony handler
            );

            for (MCEntityCitizen guard : guards) {
                // Navigate back toward their tower
                guard.getNavigation().moveTo(
                        towerPos.getX() + 0.5, towerPos.getY(), towerPos.getZ() + 0.5, 1.0);
                dismissed++;
            }
        }

        // Clear rally state
        tag.remove("rallied");
        tag.remove("rallyX");
        tag.remove("rallyY");
        tag.remove("rallyZ");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(),
                SoundSource.PLAYERS, 1.0f, 0.5f);

        player.displayClientMessage(Component.literal("\u00A7e\u00A7l\u2690 Dismissed! " + dismissed
                + " guard(s) returning to their posts.").withStyle(ChatFormatting.YELLOW), false);

        return InteractionResult.CONSUME;
    }

    // ==================== Tower List Persistence ====================

    private static List<BlockPos> loadTowers(CompoundTag tag) {
        List<BlockPos> towers = new ArrayList<>();
        ListTag list = tag.getListOrEmpty("towers");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag towerTag = list.getCompoundOrEmpty(i);
            towers.add(new BlockPos(
                    towerTag.getIntOr("x", 0),
                    towerTag.getIntOr("y", 0),
                    towerTag.getIntOr("z", 0)
            ));
        }
        return towers;
    }

    private static void saveTowers(CompoundTag tag, List<BlockPos> towers) {
        ListTag list = new ListTag();
        for (BlockPos pos : towers) {
            CompoundTag towerTag = new CompoundTag();
            towerTag.putInt("x", pos.getX());
            towerTag.putInt("y", pos.getY());
            towerTag.putInt("z", pos.getZ());
            list.add(towerTag);
        }
        tag.put("towers", (Tag) list);
    }

    private static String formatBuildingName(String buildingId) {
        return switch (buildingId) {
            case "guard_tower" -> "Guard Tower";
            case "barracks_tower" -> "Barracks Tower";
            case "barracks" -> "Barracks";
            default -> buildingId;
        };
    }
}
