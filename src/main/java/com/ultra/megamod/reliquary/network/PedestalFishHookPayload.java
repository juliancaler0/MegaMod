package com.ultra.megamod.reliquary.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.ultra.megamod.reliquary.Reliquary;

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
		// TODO: wire to PedestalFishHookRenderer.getInstance().renderHookAt(payload.pedestalPos,
		// payload.hookX, payload.hookY, payload.hookZ) once the client-layer agent restores
		// com.ultra.megamod.reliquary.client.render.PedestalFishHookRenderer.
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
