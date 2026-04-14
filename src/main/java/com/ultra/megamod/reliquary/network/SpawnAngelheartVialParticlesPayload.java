package com.ultra.megamod.reliquary.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.reliquary.util.StreamCodecHelper;

import java.util.function.Consumer;

public record SpawnAngelheartVialParticlesPayload(Vec3 position) implements CustomPacketPayload {
	public static final Type<SpawnAngelheartVialParticlesPayload> TYPE = new Type<>(com.ultra.megamod.reliquary.Reliquary.getRL("angelheart_vial_particles"));
	public static final StreamCodec<FriendlyByteBuf, SpawnAngelheartVialParticlesPayload> STREAM_CODEC = StreamCodec.composite(
			StreamCodecHelper.VEC_3_STREAM_CODEC,
			SpawnAngelheartVialParticlesPayload::position,
			SpawnAngelheartVialParticlesPayload::new);

	/**
	 * Populated by the client-side proxy to spawn the angelheart-vial item +
	 * magenta spell particle burst. Defaults to a no-op so the dedicated server
	 * can register the payload without loading {@code net.minecraft.client.*}.
	 */
	public static Consumer<SpawnAngelheartVialParticlesPayload> CLIENT_HANDLER = payload -> {};

	public static void handlePayload(SpawnAngelheartVialParticlesPayload payload) {
		CLIENT_HANDLER.accept(payload);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
