package com.ultra.megamod.lib.owo.util.pond;

import com.ultra.megamod.lib.owo.client.screens.MenuNetworkingInternals;
import net.minecraft.world.entity.player.Player;

public interface OwoAbstractContainerMenuExtension {
    void owo$attachToPlayer(Player player);

    void owo$readPropertySync(MenuNetworkingInternals.SyncPropertiesPacket packet);

    void owo$handlePacket(MenuNetworkingInternals.LocalPacket packet, boolean clientbound);
}
