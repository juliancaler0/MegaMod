package com.ultra.megamod.feature.villagerrefresh;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server → Client payload: sends updated villager offers after an admin edit action.
 */
public record AdminTradeOffersPayload(int villagerEntityId, int villagerLevel, MerchantOffers offers) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AdminTradeOffersPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "admin_trade_offers"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdminTradeOffersPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AdminTradeOffersPayload decode(RegistryFriendlyByteBuf buf) {
                    int entityId = buf.readVarInt();
                    int level = buf.readVarInt();
                    MerchantOffers offers = MerchantOffers.STREAM_CODEC.decode(buf);
                    return new AdminTradeOffersPayload(entityId, level, offers);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, AdminTradeOffersPayload payload) {
                    buf.writeVarInt(payload.villagerEntityId());
                    buf.writeVarInt(payload.villagerLevel());
                    MerchantOffers.STREAM_CODEC.encode(buf, payload.offers());
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnClient(AdminTradeOffersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof AdminTradeEditorScreen screen) {
                screen.updateOffers(payload.offers(), payload.villagerLevel());
            }
        });
    }
}
