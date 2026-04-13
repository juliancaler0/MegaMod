package com.ultra.megamod.lib.accessories.networking.server;

import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record ContainerClose() {
    public static final StructEndec<ContainerClose> ENDEC = Endec.unit(new ContainerClose());

    public static void handlePacket(ContainerClose packet, Player player) {
        ((ServerPlayer)player).doCloseContainer();
    }
}
