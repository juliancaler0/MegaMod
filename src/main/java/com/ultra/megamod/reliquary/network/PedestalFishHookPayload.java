package com.ultra.megamod.reliquary.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.ultra.megamod.reliquary.Reliquary;

import java.util.function.Consumer;

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

	/**
	 * Populated by the client-side proxy to route the payload into
	 * {@code PedestalFishHookRenderer} without dragging {@code net.minecraft.client.*}
	 * onto the common classloader (which would break dedicated-server startup).
	 * Defaults to a no-op so the server can safely register the payload.
	 */
	public static Consumer<PedestalFishHookPayload> CLIENT_HANDLER = payload -> {};

	public static void handlePayload(PedestalFishHookPayload payload) {
		CLIENT_HANDLER.accept(payload);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
