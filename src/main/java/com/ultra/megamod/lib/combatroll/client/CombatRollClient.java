package com.ultra.megamod.lib.combatroll.client;

import com.ultra.megamod.lib.playeranim.minecraft.api.PlayerAnimationFactory;
import com.ultra.megamod.lib.playeranim.core.enums.PlayState;
import com.ultra.megamod.lib.combatroll.CombatRollMod;
import com.ultra.megamod.lib.combatroll.client.animation.RollAnimationController;
import com.ultra.megamod.lib.combatroll.config.ClientConfig;
import com.ultra.megamod.lib.combatroll.config.HudConfig;

public class CombatRollClient {
    public static ClientConfig config = new ClientConfig();
    public static HudConfig hudConfig = HudConfig.createDefault();

    public static void initialize() {
        // Config is static defaults - no AutoConfig needed
    }

    /**
     * Sets up player animation system.
     * Must be called during client initialization AFTER resources are available.
     * For NeoForge: Call via event.enqueueWork() in FMLClientSetupEvent
     */
    public static void setupAnimations() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
            RollAnimationController.ID,
            1000,  // Priority (matches old implementation)
            player -> new RollAnimationController(player,
                (controller, state, animSetter) -> PlayState.STOP
            )
        );
    }
}
