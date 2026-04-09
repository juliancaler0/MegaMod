package com.ultra.megamod.feature.citizen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CitizenSoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(
        (ResourceKey) Registries.SOUND_EVENT, (String) "megamod"
    );

    public static final Supplier<SoundEvent> CITIZEN_HIRE = SOUNDS.register("citizen_hire",
        () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("megamod", "citizen_hire")));

    public static final Supplier<SoundEvent> CITIZEN_DISMISS = SOUNDS.register("citizen_dismiss",
        () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("megamod", "citizen_dismiss")));

    public static final Supplier<SoundEvent> CITIZEN_PROMOTE = SOUNDS.register("citizen_promote",
        () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("megamod", "citizen_promote")));

    public static final Supplier<SoundEvent> CITIZEN_WORK = SOUNDS.register("citizen_work",
        () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("megamod", "citizen_work")));

    public static final Supplier<SoundEvent> CITIZEN_COMBAT = SOUNDS.register("citizen_combat",
        () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("megamod", "citizen_combat")));
}
