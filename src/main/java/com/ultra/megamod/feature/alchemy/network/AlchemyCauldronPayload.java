package com.ultra.megamod.feature.alchemy.network;

import com.ultra.megamod.feature.alchemy.block.AlchemyCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: actions on an alchemy cauldron.
 * Actions: "add_water", "add_ingredient", "collect_result", "stir"
 */
public record AlchemyCauldronPayload(String action, BlockPos pos, String data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AlchemyCauldronPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "alchemy_cauldron_action"));

    public static final StreamCodec<FriendlyByteBuf, AlchemyCauldronPayload> STREAM_CODEC =
            new StreamCodec<>() {
                public AlchemyCauldronPayload decode(FriendlyByteBuf buf) {
                    return new AlchemyCauldronPayload(buf.readUtf(128), buf.readBlockPos(), buf.readUtf(256));
                }
                public void encode(FriendlyByteBuf buf, AlchemyCauldronPayload payload) {
                    buf.writeUtf(payload.action(), 128);
                    buf.writeBlockPos(payload.pos());
                    buf.writeUtf(payload.data(), 256);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(AlchemyCauldronPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player p = context.player();
            if (!(p instanceof ServerPlayer player)) return;

            ServerLevel level = (ServerLevel) player.level();
            BlockEntity be = level.getBlockEntity(payload.pos());
            if (!(be instanceof AlchemyCauldronBlockEntity cauldron)) return;

            // Verify player is close enough
            if (player.distanceToSqr(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) > 64) return;

            switch (payload.action()) {
                case "stir" -> {
                    // Stirring advances brewing by 20 ticks
                    if (cauldron.isBrewing()) {
                        // Directly advance progress by simulating extra ticks
                        // This is a simplified approach - we just call tick repeatedly
                        for (int i = 0; i < 20; i++) {
                            AlchemyCauldronBlockEntity.serverTick(level, payload.pos(), level.getBlockState(payload.pos()), cauldron);
                        }
                        level.playSound(null, payload.pos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.5f, 0.8f);
                        player.displayClientMessage(Component.literal("\u00a7dYou stir the cauldron..."), true);
                    }
                }
                case "request_sync" -> {
                    // Send current state to the requesting client
                    sendSync(player, cauldron, payload.pos());
                }
            }
        });
    }

    private static void sendSync(ServerPlayer player, AlchemyCauldronBlockEntity cauldron, BlockPos pos) {
        StringBuilder ingredients = new StringBuilder();
        for (String ing : cauldron.getIngredients()) {
            if (!ingredients.isEmpty()) ingredients.append(",");
            ingredients.append(ing);
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new AlchemyCauldronSyncPayload(
                        pos,
                        cauldron.getWaterLevel(),
                        ingredients.toString(),
                        cauldron.getBrewingProgress(),
                        cauldron.hasResult(),
                        cauldron.getOutputPotionId()
                ));
    }
}
