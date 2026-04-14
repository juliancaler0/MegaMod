package com.ultra.megamod.reliquary.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.api.IPedestal;
import com.ultra.megamod.reliquary.client.render.PedestalFishHookRenderer;
import com.ultra.megamod.reliquary.util.WorldHelper;

public record PedestalFishHookPayload(BlockPos pedestalPos, double hookX, double hookY,
									  double hookZ) implements CustomPacketPayload {
	public static final Type<PedestalFishHookPayload> TYPE = new Type<>(Reliquary.getRL("pedestal_fish_hook"));
	public static final StreamCodec<FriendlyByteBuf, PedestalFishHookPayload> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC,
			PedestalFishHookPayload::pedestalPos,
			ByteBufCodecs.DOUBLE,
			PedestalFishHookPayload::hookX,
			ByteBufCodecs.DOUBLE,
			PedestalFishHookPayload::hookY,
			ByteBufCodecs.DOUBLE,
			PedestalFishHookPayload::hookZ,
			PedestalFishHookPayload::new);

	public static void handlePayload(PedestalFishHookPayload payload) {
		ClientLevel level = Minecraft.getInstance().level;
		WorldHelper.getBlockEntity(level, payload.pedestalPos, IPedestal.class).ifPresent(pedestal -> {
			PedestalFishHookRenderer.HookRenderingData data = null;
			if (payload.hookY > 0) {
				data = new PedestalFishHookRenderer.HookRenderingData(payload.hookX, payload.hookY, payload.hookZ);
			}

			pedestal.setItemData(data);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
