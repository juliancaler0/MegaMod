package com.ultra.megamod.feature.alchemy.network;

import com.ultra.megamod.feature.alchemy.AlchemyRecipeRegistry;
import com.ultra.megamod.feature.alchemy.block.AlchemyGrindstoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> Server: actions on a grindstone.
 * Actions: "insert_item", "collect_output", "request_sync"
 */
public record AlchemyGrindstonePayload(String action, BlockPos pos, String data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AlchemyGrindstonePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "alchemy_grindstone_action"));

    public static final StreamCodec<FriendlyByteBuf, AlchemyGrindstonePayload> STREAM_CODEC =
            new StreamCodec<>() {
                public AlchemyGrindstonePayload decode(FriendlyByteBuf buf) {
                    return new AlchemyGrindstonePayload(buf.readUtf(128), buf.readBlockPos(), buf.readUtf(256));
                }
                public void encode(FriendlyByteBuf buf, AlchemyGrindstonePayload payload) {
                    buf.writeUtf(payload.action(), 128);
                    buf.writeBlockPos(payload.pos());
                    buf.writeUtf(payload.data(), 256);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(AlchemyGrindstonePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player p = context.player();
            if (!(p instanceof ServerPlayer player)) return;

            ServerLevel level = (ServerLevel) player.level();
            BlockEntity be = level.getBlockEntity(payload.pos());
            if (!(be instanceof AlchemyGrindstoneBlockEntity grindstone)) return;

            if (player.distanceToSqr(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) > 64) return;

            switch (payload.action()) {
                case "collect_output" -> {
                    if (grindstone.hasOutput()) {
                        ItemStack output = grindstone.collectOutput();
                        if (!output.isEmpty()) {
                            if (!player.getInventory().add(output)) {
                                player.drop(output, false);
                            }
                            level.playSound(null, payload.pos(), SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                        }
                    }
                }
                case "request_sync" -> {
                    sendSync(player, grindstone, payload.pos());
                }
            }
        });
    }

    private static void sendSync(ServerPlayer player, AlchemyGrindstoneBlockEntity grindstone, BlockPos pos) {
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new AlchemyGrindstoneSyncPayload(
                        pos,
                        grindstone.isGrinding(),
                        grindstone.getGrindingProgress(),
                        grindstone.getGrindingTotal(),
                        grindstone.hasOutput()
                ));
    }
}
