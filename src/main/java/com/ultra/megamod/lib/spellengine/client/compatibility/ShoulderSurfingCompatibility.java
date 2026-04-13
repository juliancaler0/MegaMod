package com.ultra.megamod.lib.spellengine.client.compatibility;

import net.minecraft.client.Minecraft;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainerHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;

/**
 * ShoulderSurfing compatibility stub.
 * IShoulderSurfingPlugin/IShoulderSurfingRegistrar are from a third-party mod
 * that is not bundled with MegaMod. This class is kept as a stub for potential
 * future integration when the ShoulderSurfing mod is present.
 */
public class ShoulderSurfingCompatibility {
    private static final int toleranceTicks = 3;
    private int lastTimeCasted = 0;

    public void register(Object registrar) {
        // ShoulderSurfing mod is not present, registration is a no-op
    }

    private int getTicks() {
        return lastTimeCasted;
    }
    private void setTicks(int ticks) {
        this.lastTimeCasted = ticks;
    }
}
