package com.ultra.megamod.feature.citizen.block;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.FactionData;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.furniture.FurnitureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Town Chest — massive storage block for colony logistics.
 * Only 1 allowed per faction. Opens a scrollable 324-slot inventory.
 * Multiple Warehouse Workers can reference the same Town Chest.
 */
public class TownChestBlock extends FurnitureBlock implements EntityBlock {
    public static final MapCodec<TownChestBlock> CODEC = TownChestBlock.simpleCodec(TownChestBlock::new);

    public TownChestBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends TownChestBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TownChestBlockEntity(pos, state);
    }

    /**
     * Called after block is placed by a player. Register the Town Chest with the player's faction.
     */
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(placer instanceof ServerPlayer player)) return;

        ServerLevel serverLevel = (ServerLevel) level;
        FactionManager fm = FactionManager.get(serverLevel);
        String factionId = fm.getPlayerFaction(player.getUUID());

        if (factionId == null) {
            // Auto-create a faction for the player (free of charge)
            String playerName = player.getGameProfile().name();
            String autoFactionId = playerName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            String autoDisplayName = playerName + "'s Town";
            FactionData created = fm.createFaction(autoFactionId, autoDisplayName, player.getUUID(), 0);
            if (created == null) {
                // ID collision — add UUID suffix
                autoFactionId = autoFactionId + "_" + player.getUUID().toString().substring(0, 4);
                created = fm.createFaction(autoFactionId, autoDisplayName, player.getUUID(), 0);
            }
            if (created == null) {
                player.sendSystemMessage(Component.literal("\u00A7c\u00A7l\u2716 \u00A76Failed to create faction. You may be banned from factions."));
                return;
            }
            factionId = autoFactionId;
            fm.saveToDisk(serverLevel);
            player.sendSystemMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A76Faction '\u00A7f" + autoDisplayName + "\u00A76' created automatically!"));
        }

        FactionData faction = fm.getFaction(factionId);
        if (faction == null) return;

        // Check if faction already has a Town Chest
        if (faction.hasTownChest()) {
            BlockPos existing = faction.getTownChestPos();
            // Verify the existing chest is still there
            if (level.getBlockEntity(existing) instanceof TownChestBlockEntity) {
                // Faction already has one — destroy this one and refund
                player.sendSystemMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76Your faction already has a Town Chest at \u00A7f("
                    + existing.getX() + ", " + existing.getY() + ", " + existing.getZ()
                    + ")\u00A76! Only 1 per faction."));
                level.destroyBlock(pos, true); // drop as item
                return;
            } else {
                // Old position is stale, clear it
                faction.setTownChestPos(BlockPos.ZERO);
            }
        }

        // Register this as the faction's Town Chest
        faction.setTownChestPos(pos);
        fm.saveToDisk(serverLevel);

        // Auto-claim a 3x3 chunk territory centered on the chest
        ClaimManager cm = ClaimManager.get(serverLevel);
        int centerChunkX = pos.getX() >> 4;
        int centerChunkZ = pos.getZ() >> 4;
        int claimed = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (cm.claimChunk(factionId, centerChunkX + dx, centerChunkZ + dz)) {
                    claimed++;
                }
            }
        }
        if (claimed > 0) {
            cm.saveToDisk(serverLevel);
        }

        player.sendSystemMessage(Component.literal(
            "\u00A7a\u00A7l\u2714 \u00A76Town Chest registered for faction \u00A7f" + faction.getDisplayName()
            + "\u00A76 at \u00A7f(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")"));
        if (claimed > 0) {
            player.sendSystemMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A76Auto-claimed \u00A7f" + claimed + "\u00A76 chunks around the Town Chest."));
        }
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TownChestBlockEntity chest) {
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                    (containerId, playerInv, p) -> new TownChestMenu(containerId, playerInv, chest),
                    Component.literal("Town Chest")
            ), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock()) && !oldState.isAir()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TownChestBlockEntity chest) {
                chest.dropContents(level, pos);
            }
        }
    }

    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TownChestBlockEntity chest) {
            chest.dropContents(level, pos);
        }

        // Clear the faction's Town Chest reference
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            FactionManager fm = FactionManager.get(serverLevel);
            // Find which faction owns this chest by checking all factions
            for (FactionData faction : fm.getAllFactions()) {
                if (faction.hasTownChest() && faction.getTownChestPos().equals(pos)) {
                    faction.setTownChestPos(BlockPos.ZERO);
                    fm.saveToDisk(serverLevel);
                    if (player instanceof ServerPlayer sp) {
                        sp.sendSystemMessage(Component.literal(
                            "\u00A7e\u00A7l\u26A0 \u00A76Town Chest removed from faction \u00A7f" + faction.getDisplayName()));
                    }
                    break;
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
