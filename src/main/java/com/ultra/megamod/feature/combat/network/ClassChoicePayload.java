package com.ultra.megamod.feature.combat.network;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.PlayerClassManager;
import com.ultra.megamod.feature.combat.PlayerClassManager.PlayerClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server payload sent when a player picks their class from the selection screen.
 * The server validates the choice and persists it.
 */
public record ClassChoicePayload(String className) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClassChoicePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "class_choice"));

    public static final StreamCodec<FriendlyByteBuf, ClassChoicePayload> STREAM_CODEC =
            new StreamCodec<FriendlyByteBuf, ClassChoicePayload>() {
                @Override
                public ClassChoicePayload decode(FriendlyByteBuf buf) {
                    return new ClassChoicePayload(buf.readUtf(64));
                }

                @Override
                public void encode(FriendlyByteBuf buf, ClassChoicePayload payload) {
                    buf.writeUtf(payload.className(), 64);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(ClassChoicePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Validate the class name
            PlayerClass chosen;
            try {
                chosen = PlayerClass.valueOf(payload.className().toUpperCase());
            } catch (IllegalArgumentException e) {
                MegaMod.LOGGER.warn("Player {} sent invalid class choice: {}", player.getGameProfile().name(), payload.className());
                return;
            }

            if (chosen == PlayerClass.NONE) {
                MegaMod.LOGGER.warn("Player {} tried to choose NONE class", player.getGameProfile().name());
                return;
            }

            ServerLevel level = (ServerLevel) player.level();
            PlayerClassManager manager = PlayerClassManager.get(level);

            // Don't allow changing class if already chosen (admin reclass should go through admin panel)
            if (manager.hasChosenClass(player.getUUID())) {
                player.sendSystemMessage(Component.literal("You have already chosen a class!")
                        .withStyle(ChatFormatting.RED));
                return;
            }

            // Set the class
            manager.setClass(player.getUUID(), chosen);
            manager.saveToDisk(level);

            // Announce to the player
            int color = chosen.getColor();
            player.sendSystemMessage(Component.literal("You have become a ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(chosen.getDisplayName())
                            .withStyle(s -> s.withColor(color).withBold(true)))
                    .append(Component.literal("!")
                            .withStyle(ChatFormatting.GRAY)));

            // Send title
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                    Component.literal(chosen.getDisplayName())
                            .withStyle(s -> s.withColor(color).withBold(true))));
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                    Component.literal("Class Selected")
                            .withStyle(ChatFormatting.GRAY)));

            // Play a sound
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.TOTEM_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.6f, 1.2f);

            // Sync class to client for HUD/tooltip display
            PacketDistributor.sendToPlayer(player, new ClassSyncPayload(chosen.name()));
        });
    }
}
