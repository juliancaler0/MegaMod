package com.ultra.megamod.lib.azurelib.common.network.packet;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import com.ultra.megamod.lib.azurelib.common.animation.cache.AzIdentifiableItemStackAnimatorCache;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.AzDispatchSide;
import com.ultra.megamod.lib.azurelib.common.animation.dispatch.command.AzCommand;
import com.ultra.megamod.lib.azurelib.common.network.AbstractPacket;
import com.ultra.megamod.lib.azurelib.common.platform.services.AzureLibNetwork;

public record AzItemStackDispatchCommandPacket(
    UUID itemStackId,
    AzCommand dispatchCommand
) implements AbstractPacket {

    public static final Type<AzItemStackDispatchCommandPacket> TYPE = new Type<>(
        AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID
    );

    public static final StreamCodec<FriendlyByteBuf, AzItemStackDispatchCommandPacket> CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        AzItemStackDispatchCommandPacket::itemStackId,
        AzCommand.CODEC,
        AzItemStackDispatchCommandPacket::dispatchCommand,
        AzItemStackDispatchCommandPacket::new
    );

    public void handle() {
        var animator = AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(itemStackId);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
