package com.ultra.megamod.lib.etf.features.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import com.ultra.megamod.lib.etf.utils.ETFEntity;

public interface ETFPlayerEntity extends ETFEntity {


    Entity etf$getEntity();

    boolean etf$isTeammate(Player player);

    Inventory etf$getInventory();

    @Deprecated
    boolean etf$isPartVisible(PlayerModelPart part);

    Component etf$getName();

    String etf$getUuidAsString();
}
