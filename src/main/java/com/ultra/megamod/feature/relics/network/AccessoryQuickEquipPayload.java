/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.accessory.AccessoryEvents;
import com.ultra.megamod.feature.relics.accessory.AccessoryManager;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AccessoryQuickEquipPayload(int inventorySlot) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<AccessoryQuickEquipPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"accessory_quick_equip"));
    public static final StreamCodec<FriendlyByteBuf, AccessoryQuickEquipPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, AccessoryQuickEquipPayload>(){

        public AccessoryQuickEquipPayload decode(FriendlyByteBuf buf) {
            return new AccessoryQuickEquipPayload(buf.readVarInt());
        }

        public void encode(FriendlyByteBuf buf, AccessoryQuickEquipPayload payload) {
            buf.writeVarInt(payload.inventorySlot());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(AccessoryQuickEquipPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Item patt1$temp;
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player = (ServerPlayer)patt0$temp;
            int slotIndex = payload.inventorySlot();
            if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) {
                return;
            }
            ItemStack stack = player.getInventory().getItem(slotIndex);
            if (stack.isEmpty()) return;
            patt1$temp = stack.getItem();
            AccessorySlotType relicSlot;
            if (patt1$temp instanceof RelicItem relicItem) {
                relicSlot = relicItem.getSlotType();
            } else if (patt1$temp instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem) {
                relicSlot = AccessorySlotType.FACE;
            } else {
                return;
            }
            if (relicSlot == AccessorySlotType.NONE) {
                return;
            }
            ServerLevel overworld = player.level().getServer().overworld();
            AccessoryManager manager = AccessoryManager.get(overworld);
            AccessorySlotType targetSlot = AccessoryQuickEquipPayload.findAvailableSlot(relicSlot, manager, player);
            if (targetSlot == null) {
                return;
            }
            ItemStack toEquip = stack.copy();
            toEquip.setCount(1);
            stack.shrink(1);
            player.getInventory().setItem(slotIndex, stack);
            manager.setEquipped(player.getUUID(), targetSlot, toEquip);
            AccessoryEvents.syncToClient(player);
        });
    }

    private static AccessorySlotType findAvailableSlot(AccessorySlotType relicSlot, AccessoryManager manager, ServerPlayer player) {
        UUID playerId = player.getUUID();
        if (relicSlot == AccessorySlotType.HANDS_LEFT || relicSlot == AccessorySlotType.HANDS_RIGHT) {
            if (manager.getEquipped(playerId, AccessorySlotType.HANDS_LEFT).isEmpty()) {
                return AccessorySlotType.HANDS_LEFT;
            }
            if (manager.getEquipped(playerId, AccessorySlotType.HANDS_RIGHT).isEmpty()) {
                return AccessorySlotType.HANDS_RIGHT;
            }
            return null;
        }
        if (relicSlot == AccessorySlotType.RING_LEFT || relicSlot == AccessorySlotType.RING_RIGHT) {
            if (manager.getEquipped(playerId, AccessorySlotType.RING_LEFT).isEmpty()) {
                return AccessorySlotType.RING_LEFT;
            }
            if (manager.getEquipped(playerId, AccessorySlotType.RING_RIGHT).isEmpty()) {
                return AccessorySlotType.RING_RIGHT;
            }
            return null;
        }
        if (manager.getEquipped(playerId, relicSlot).isEmpty()) {
            return relicSlot;
        }
        return null;
    }
}

