package com.ultra.megamod.feature.recovery;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.network.handlers.MailHandler;
import com.ultra.megamod.feature.relics.accessory.AccessoryManager;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Map;

@EventBusSubscriber(modid = MegaMod.MODID)
public class DeathRecoveryEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();
        if (!FeatureToggleManager.get(level).isEnabled("death_recovery")) return;

        BlockPos deathPos = findSafePos(player, level);

        // Place gravestone block
        BlockState gravestoneState = GravestoneRegistry.GRAVESTONE.get().defaultBlockState()
            .setValue(HorizontalDirectionalBlock.FACING, player.getDirection());
        level.setBlock(deathPos, gravestoneState, 3);

        BlockEntity be = level.getBlockEntity(deathPos);
        if (!(be instanceof GravestoneBlockEntity grave)) return;

        grave.setOwner(player.getUUID(), player.getGameProfile().name());

        // Store main inventory (all 36 slots)
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                grave.addItem(stack);
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        // Store armor
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    grave.addItem(armor);
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }

        // Store offhand
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            grave.addItem(offhand);
            player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }

        // Store accessories from AccessoryManager
        try {
            AccessoryManager accMgr = AccessoryManager.get(level);
            Map<AccessorySlotType, ItemStack> equipped = accMgr.getAllEquipped(player.getUUID());
            for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    grave.addItem(entry.getValue());
                    accMgr.removeEquipped(player.getUUID(), entry.getKey());
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.warn("Failed to store accessories in gravestone: {}", e.getMessage());
        }

        // Send death coordinates via mail
        String dimensionName = level.dimension().identifier().toString();
        String body = String.format(
            "You died at X: %d, Y: %d, Z: %d in %s.\n\nYour gravestone will expire in 60 minutes.",
            deathPos.getX(), deathPos.getY(), deathPos.getZ(), dimensionName
        );
        MailHandler.sendSystemMail(player.getUUID(), "Death Recovery", body, level);

        // Also send chat message with coords
        player.sendSystemMessage(
            net.minecraft.network.chat.Component.literal(
                "\u00A76[Death Recovery] \u00A77Your items are at \u00A7e" +
                deathPos.getX() + ", " + deathPos.getY() + ", " + deathPos.getZ() +
                " \u00A77in \u00A7b" + dimensionName
            )
        );
    }

    private static BlockPos findSafePos(ServerPlayer player, ServerLevel level) {
        int x = player.getBlockX();
        int y = player.getBlockY();
        int z = player.getBlockZ();

        // Void death: place at Y=1
        if (y < level.getMinY()) {
            y = level.getMinY() + 1;
        }

        // Clamp to build height
        if (y >= level.getMaxY()) {
            y = level.getMaxY() - 1;
        }

        BlockPos candidate = new BlockPos(x, y, z);

        // If the position is occupied by a solid block or another gravestone, scan nearby
        if (!canPlaceAt(level, candidate)) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        BlockPos test = candidate.offset(dx, dy, dz);
                        if (canPlaceAt(level, test)) {
                            return test;
                        }
                    }
                }
            }
        }

        // Fallback: just use the position anyway (replace whatever is there)
        return candidate;
    }

    private static boolean canPlaceAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced();
    }
}
