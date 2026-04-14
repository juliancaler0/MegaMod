package com.ultra.megamod.reliquary.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import com.ultra.megamod.reliquary.Reliquary;

import java.util.function.Consumer;

public record SpawnThrownPotionImpactParticlesPayload(int color, double posX, double posY,
													  double posZ) implements CustomPacketPayload {
	public static final Type<SpawnThrownPotionImpactParticlesPayload> TYPE = new Type<>(Reliquary.getRL("thrown_potion_impact_particles"));
	public static final StreamCodec<FriendlyByteBuf, SpawnThrownPotionImpactParticlesPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			SpawnThrownPotionImpactParticlesPayload::color,
			ByteBufCodecs.DOUBLE,
			SpawnThrownPotionImpactParticlesPayload::posX,
			ByteBufCodecs.DOUBLE,
			SpawnThrownPotionImpactParticlesPayload::posY,
			ByteBufCodecs.DOUBLE,
			SpawnThrownPotionImpactParticlesPayload::posZ,
			SpawnThrownPotionImpactParticlesPayload::new);

	/**
	 * Populated by the client-side proxy to spawn the thrown-potion impact
	 * particle burst. Defaults to a no-op so the dedicated server can register
	 * the payload without loading {@code net.minecraft.client.*}.
	 */
	public static Consumer<SpawnThrownPotionImpactParticlesPayload> CLIENT_HANDLER = payload -> {};

	public static void handlePayload(SpawnThrownPotionImpactParticlesPayload payload) {
		CLIENT_HANDLER.accept(payload);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
