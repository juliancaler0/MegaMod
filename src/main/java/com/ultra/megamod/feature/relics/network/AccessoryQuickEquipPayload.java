package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AccessoryQuickEquipPayload(int inventorySlot) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AccessoryQuickEquipPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "accessory_quick_equip"));

    public static final StreamCodec<FriendlyByteBuf, AccessoryQuickEquipPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public AccessoryQuickEquipPayload decode(FriendlyByteBuf buf) {
            return new AccessoryQuickEquipPayload(buf.readVarInt());
        }

        @Override
        public void encode(FriendlyByteBuf buf, AccessoryQuickEquipPayload payload) {
            buf.writeVarInt(payload.inventorySlot());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(AccessoryQuickEquipPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player rawPlayer = context.player();
            if (!(rawPlayer instanceof ServerPlayer player)) {
                return;
            }
            int slotIndex = payload.inventorySlot();
            if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) {
                return;
            }
            ItemStack stack = player.getInventory().getItem(slotIndex);
            if (stack.isEmpty()) return;

            Item itemType = stack.getItem();
            AccessorySlotType relicSlot;
            if (itemType instanceof RelicItem relicItem) {
                relicSlot = relicItem.getSlotType();
            } else if (itemType instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem) {
                relicSlot = AccessorySlotType.FACE;
            } else {
                return;
            }
            if (relicSlot == AccessorySlotType.NONE) {
                return;
            }

            AccessorySlotType targetSlot = findAvailableSlot(relicSlot, player);
            if (targetSlot == null) {
                return;
            }

            ItemStack toEquip = stack.copy();
            toEquip.setCount(1);
            stack.shrink(1);
            player.getInventory().setItem(slotIndex, stack);
            LibAccessoryLookup.setEquipped(player, targetSlot, toEquip);
            LibAccessoryLookup.syncToClient(player);
        });
    }

    private static AccessorySlotType findAvailableSlot(AccessorySlotType relicSlot, ServerPlayer player) {
        if (relicSlot == AccessorySlotType.HANDS_LEFT || relicSlot == AccessorySlotType.HANDS_RIGHT) {
            if (LibAccessoryLookup.getEquipped(player, AccessorySlotType.HANDS_LEFT).isEmpty()) {
                return AccessorySlotType.HANDS_LEFT;
            }
            if (LibAccessoryLookup.getEquipped(player, AccessorySlotType.HANDS_RIGHT).isEmpty()) {
                return AccessorySlotType.HANDS_RIGHT;
            }
            return null;
        }
        if (relicSlot == AccessorySlotType.RING_LEFT || relicSlot == AccessorySlotType.RING_RIGHT) {
            if (LibAccessoryLookup.getEquipped(player, AccessorySlotType.RING_LEFT).isEmpty()) {
                return AccessorySlotType.RING_LEFT;
            }
            if (LibAccessoryLookup.getEquipped(player, AccessorySlotType.RING_RIGHT).isEmpty()) {
                return AccessorySlotType.RING_RIGHT;
            }
            return null;
        }
        if (LibAccessoryLookup.getEquipped(player, relicSlot).isEmpty()) {
            return relicSlot;
        }
        return null;
    }
}
