package com.ultra.megamod.lib.accessories.networking.client;

import com.ultra.megamod.lib.accessories.impl.option.AccessoriesPlayerOptionsHolder;
import com.ultra.megamod.lib.accessories.utils.EndecUtils;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import net.minecraft.world.entity.player.Player;

public record SyncPlayerOptions(AccessoriesPlayerOptionsHolder options) {
    public static final StructEndec<SyncPlayerOptions> ENDEC = StructEndecBuilder.of(
            EndecUtils.createMapCarrierEndec(AccessoriesPlayerOptionsHolder::new).fieldOf("options", SyncPlayerOptions::options),
            SyncPlayerOptions::new
    );

    //@Environment(EnvType.CLIENT)
    public static void handlePacket(SyncPlayerOptions packet, Player player) {
        EndecUtils.readDataFrom(AccessoriesPlayerOptionsHolder.getOptions(player), packet.options());
    }
}
