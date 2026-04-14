package com.ultra.megamod.feature.combat.client;

import com.ultra.megamod.feature.combat.network.ClassSelectionPayload;
import net.minecraft.client.Minecraft;

/**
 * Client-only proxy that installs the real screen-opening handler onto
 * {@link ClassSelectionPayload#CLIENT_HANDLER}. Called from
 * {@code MegaModClient} so neither this class nor its {@code Minecraft} /
 * {@code Screen} references are ever loaded on the dedicated server — which
 * would otherwise be rejected by {@code NeoForgeDevDistCleaner} while
 * payload registrations are being executed on the common side.
 */
public final class ClassSelectionClientProxy {
	private ClassSelectionClientProxy() {}

	public static void init() {
		ClassSelectionPayload.CLIENT_HANDLER = (payload, ctx) ->
				ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new ClassSelectionScreen()));
	}
}
