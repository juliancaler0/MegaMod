package com.ultra.megamod.lib.accessories.networking.client;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.config.ConfigWrapper;
import com.ultra.megamod.lib.accessories.owo.config.Option;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * Stubbed out - OWO config synchronization not ported.
 */
public record SyncServerOverrideOption(String configId, Option.Key optionKey, FriendlyByteBuf buf) {

    public static final StructEndec<SyncServerOverrideOption> ENDEC = StructEndecBuilder.of(
        Endec.STRING.fieldOf("config_id", SyncServerOverrideOption::configId),
        Endec.STRING.xmap(Option.Key::new, k -> k.asString()).fieldOf("option_key", SyncServerOverrideOption::optionKey),
        Endec.STRING.xmap(s -> (FriendlyByteBuf) null, buf -> "").fieldOf("buf", SyncServerOverrideOption::buf),
        SyncServerOverrideOption::new
    );

    public static <T> void hookUpdate(Consumer<Consumer<T>> hook, ConfigWrapper<?> wrapper, Option.Key optionKey) {
        // No-op - OWO config sync not ported
    }

    public static <T> void sendUpdatePacket(ConfigWrapper<?> wrapper, Option.Key optionKey) {
        // No-op
    }

    public static <T> void sendUpdatePacket(Option<T> option) {
        // No-op
    }

    public static void handlePacket(SyncServerOverrideOption packet, Player player) {
        // No-op
    }
}
