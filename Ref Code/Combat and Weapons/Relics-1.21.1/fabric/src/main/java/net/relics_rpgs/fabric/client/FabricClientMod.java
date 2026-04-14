package net.relics_rpgs.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.relics_rpgs.client.RelicsClientMod;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RelicsClientMod.init();
    }
}
