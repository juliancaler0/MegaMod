package com.ultra.megamod.feature.casino;

import com.ultra.megamod.feature.casino.network.OpenSlotMachinePayload;
import com.ultra.megamod.feature.casino.slots.SlotMachineBlockEntity;
import com.ultra.megamod.feature.furniture.FurnitureRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;

/**
 * Makes decorative slot machine furniture blocks open the slots GUI directly.
 * First tries to find a real SlotMachineBlock within 5 blocks for persistence.
 * If none found, opens a virtual slot session using the decorative block's position.
 */
@EventBusSubscriber(modid = "megamod")
public class DecorativeSlotRedirect {

    private static Set<Block> DECORATIVE_SLOTS;

    private static Set<Block> getDecorativeSlots() {
        if (DECORATIVE_SLOTS == null) {
            DECORATIVE_SLOTS = Set.of(
                    FurnitureRegistry.CASINO2_BUFFALO_SLOT_MACHINE.get(),
                    FurnitureRegistry.CASINO2_GAMBLING_GAME_MACHINE.get(),
                    FurnitureRegistry.CASINO2_MADONNA_GAMBLING_MACHINE.get(),
                    FurnitureRegistry.CASINO_GAME_BIGWIN.get(),
                    FurnitureRegistry.CASINO_GAME_SLOT.get(),
                    FurnitureRegistry.CASINO2_VENEZIA_SLOT.get()
            );
        }
        return DECORATIVE_SLOTS;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Block clicked = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (!getDecorativeSlots().contains(clicked)) return;

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos clickedPos = event.getPos();

        // Search for a real SlotMachineBlock within 5 blocks
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos checkPos = clickedPos.offset(dx, dy, dz);
                    if (level.getBlockEntity(checkPos) instanceof SlotMachineBlockEntity slotBE) {
                        if (slotBE.isOccupied() && !slotBE.isUsedBy(player.getUUID())) continue;

                        slotBE.occupy(player.getUUID());
                        int wallet = com.ultra.megamod.feature.casino.chips.ChipManager.get(level).getBalance(player.getUUID());
                        PacketDistributor.sendToPlayer(player,
                                new OpenSlotMachinePayload(checkPos, slotBE.getBetIndex(), slotBE.getLineMode(), wallet));
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

        // No real slot machine nearby — open a virtual session directly
        // Use the decorative block's position as the slot ID; the server handler
        // will create a temporary BlockEntity or handle it gracefully
        int wallet = com.ultra.megamod.feature.casino.chips.ChipManager.get(level).getBalance(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                new OpenSlotMachinePayload(clickedPos, 0, 0, wallet));
        event.setCanceled(true);
    }
}
