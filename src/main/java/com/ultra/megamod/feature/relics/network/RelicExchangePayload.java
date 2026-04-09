/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.RelicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RelicExchangePayload(String slotName, int amount) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<RelicExchangePayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"relic_exchange"));
    public static final StreamCodec<FriendlyByteBuf, RelicExchangePayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, RelicExchangePayload>(){

        public RelicExchangePayload decode(FriendlyByteBuf buf) {
            String slotName = buf.readUtf();
            int amount = buf.readInt();
            return new RelicExchangePayload(slotName, amount);
        }

        public void encode(FriendlyByteBuf buf, RelicExchangePayload payload) {
            buf.writeUtf(payload.slotName());
            buf.writeInt(payload.amount());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(RelicExchangePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            int cost;
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player = (ServerPlayer)patt0$temp;
            ItemStack stack = RelicExchangePayload.findRelicStack(player);
            if (stack == null || stack.isEmpty()) {
                return;
            }
            if (!(stack.getItem() instanceof RelicItem)) {
                return;
            }
            if (!RelicData.isInitialized(stack)) {
                return;
            }
            if (RelicData.getLevel(stack) >= 10) {
                return;
            }
            int exchangesToDo = Math.max(1, Math.min(payload.amount(), 10));
            for (int i = 0; i < exchangesToDo && player.totalExperience >= (cost = RelicData.getExchangeCost(stack)); ++i) {
                int xpGain = RelicData.getXpGainPerExchange(stack);
                player.giveExperiencePoints(-cost);
                RelicData.addXp(stack, xpGain);
                RelicData.addExchanges(stack, 1);
                if (RelicData.getLevel(stack) >= 10) break;
            }
        });
    }

    private static ItemStack findRelicStack(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof RelicItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty() && offHand.getItem() instanceof RelicItem) {
            return offHand;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof RelicItem)) continue;
            return stack;
        }
        return null;
    }
}

