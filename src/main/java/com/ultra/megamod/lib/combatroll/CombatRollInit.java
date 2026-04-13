package com.ultra.megamod.lib.combatroll;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.combatroll.api.CombatRoll;
import com.ultra.megamod.lib.combatroll.utils.SoundHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Main initialization for CombatRoll system.
 * Call {@link #register(IEventBus)} from MegaMod's constructor to register all deferred registries.
 * Call {@link #init()} after registration is complete.
 *
 * Example registration in MegaMod.java:
 *   CombatRollInit.register(modEventBus);
 *   CombatRollInit.init();
 */
public class CombatRollInit {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MegaMod.MODID);

    static {
        SoundHelper.soundKeys.forEach(soundKey -> SOUND_EVENTS.register(soundKey,
                () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MegaMod.MODID, soundKey))));
    }

    /**
     * Register all CombatRoll deferred registries on the mod event bus.
     * Forces class loading of CombatRoll.Attributes to ensure all entries
     * are added before the RegisterEvent fires.
     */
    public static void register(IEventBus modEventBus) {
        // Force-load the Attributes class so all static DeferredRegister entries are created
        // BEFORE register(modEventBus) subscribes to the event bus.
        var _forceLoad = CombatRoll.Attributes.DISTANCE;
        CombatRoll.ATTRIBUTES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
    }

    /**
     * Initialize CombatRoll after registries are set up.
     */
    public static void init() {
        CombatRollMod.init();
    }
}
