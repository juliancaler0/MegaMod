package com.ultra.megamod.lib.playeranim.minecraft;

import net.neoforged.fml.ModList;

/**
 * Service for checking mod availability.
 * Stubbed: no longer depends on org.redlance AdvancedService/ServiceUtils.
 */
public interface PlayerAnimLibService {
    PlayerAnimLibService INSTANCE = new PlayerAnimLibService() {
        @Override
        public boolean isModLoaded(String id) {
            return ModList.get().isLoaded(id);
        }
    };

    boolean isModLoaded(String id);
}
