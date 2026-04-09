package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.accessory.AccessoryEvents;
import com.ultra.megamod.feature.relics.accessory.AccessoryManager;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
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

public class AccessoryPayload {

    public record AccessorySyncPayload(CompoundTag tagData) implements CustomPacketPayload
    {
        public static volatile Map<String, String> clientEquipped = new HashMap<String, String>();
        /** Incremented each time clientEquipped is replaced, so renderers can detect changes. */
        public static volatile long syncVersion = 0;
        public static final CustomPacketPayload.Type<AccessorySyncPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"accessory_sync"));
        public static final StreamCodec<FriendlyByteBuf, AccessorySyncPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, AccessorySyncPayload>(){

            public AccessorySyncPayload decode(FriendlyByteBuf buf) {
                CompoundTag tag = buf.readNbt();
                return new AccessorySyncPayload(tag != null ? tag : new CompoundTag());
            }

            public void encode(FriendlyByteBuf buf, AccessorySyncPayload payload) {
                buf.writeNbt(payload.tagData());
            }
        };

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleOnClient(AccessorySyncPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                HashMap<String, String> parsed = new HashMap<>();
                CompoundTag tag = payload.tagData();
                for (String key : tag.keySet()) {
                    String value = tag.getStringOr(key, "");
                    if (!value.isEmpty()) {
                        parsed.put(key, value);
                    }
                }
                clientEquipped = parsed;
                syncVersion++;
            });
        }
    }

    public record AccessoryEquipPayload(String slotName, boolean unequip) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<AccessoryEquipPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"accessory_equip"));
        public static final StreamCodec<FriendlyByteBuf, AccessoryEquipPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, AccessoryEquipPayload>(){

            public AccessoryEquipPayload decode(FriendlyByteBuf buf) {
                String slotName = buf.readUtf();
                boolean unequip = buf.readBoolean();
                return new AccessoryEquipPayload(slotName, unequip);
            }

            public void encode(FriendlyByteBuf buf, AccessoryEquipPayload payload) {
                buf.writeUtf(payload.slotName());
                buf.writeBoolean(payload.unequip());
            }
        };

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handleOnServer(AccessoryEquipPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                AccessorySlotType slot;
                Player patt0$temp = context.player();
                if (!(patt0$temp instanceof ServerPlayer)) {
                    return;
                }
                ServerPlayer player = (ServerPlayer)patt0$temp;
                try {
                    slot = AccessorySlotType.valueOf(payload.slotName());
                }
                catch (IllegalArgumentException e) {
                    return;
                }
                if (slot == AccessorySlotType.NONE) {
                    return;
                }
                ServerLevel overworld = player.level().getServer().overworld();
                AccessoryManager manager = AccessoryManager.get(overworld);
                if (payload.unequip()) {
                    ItemStack removed = manager.removeEquipped(player.getUUID(), slot);
                    if (!removed.isEmpty() && !player.getInventory().add(removed)) {
                        player.spawnAtLocation(player.level(), removed);
                    }
                } else {
                    ItemStack heldItem = player.getMainHandItem();
                    if (heldItem.isEmpty()) {
                        AccessoryEvents.syncToClient(player);
                        player.inventoryMenu.broadcastChanges();
                        return;
                    }
                    Item heldItemType = heldItem.getItem();
                    AccessorySlotType relicSlot;
                    if (heldItemType instanceof RelicItem relicItem) {
                        relicSlot = relicItem.getSlotType();
                    } else if (heldItemType instanceof com.ultra.megamod.feature.dungeons.item.UmvuthanaMaskItem) {
                        relicSlot = AccessorySlotType.FACE;
                    } else {
                        AccessoryEvents.syncToClient(player);
                        player.inventoryMenu.broadcastChanges();
                        return;
                    }
                    AccessorySlotType targetSlot = slot;
                    boolean compatible = (relicSlot == targetSlot);
                    if (!compatible && (relicSlot == AccessorySlotType.HANDS_LEFT || relicSlot == AccessorySlotType.HANDS_RIGHT)) {
                        compatible = (targetSlot == AccessorySlotType.HANDS_LEFT || targetSlot == AccessorySlotType.HANDS_RIGHT);
                    }
                    if (!compatible && (relicSlot == AccessorySlotType.RING_LEFT || relicSlot == AccessorySlotType.RING_RIGHT)) {
                        compatible = (targetSlot == AccessorySlotType.RING_LEFT || targetSlot == AccessorySlotType.RING_RIGHT);
                    }
                    if (!compatible) {
                        AccessoryEvents.syncToClient(player);
                        player.inventoryMenu.broadcastChanges();
                        return;
                    }
                    ItemStack toEquip = heldItem.copy();
                    toEquip.setCount(1);
                    ItemStack mainHand = player.getMainHandItem();
                    mainHand.shrink(1);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, mainHand);
                    manager.setEquipped(player.getUUID(), targetSlot, toEquip);
                }
                AccessoryEvents.syncToClient(player);
                player.inventoryMenu.broadcastChanges();
            });
        }
    }
}

