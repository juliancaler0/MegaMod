package com.ultra.megamod.feature.combat.relics.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported 1:1 from Relics-1.21.1's RelicSounds.
 * Uses NeoForge DeferredRegister for SoundEvents instead of Fabric's Registry.register.
 */
public class RelicSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MegaMod.MODID);

    public record Entry(String name, DeferredHolder<SoundEvent, SoundEvent> holder) {
        public Identifier id() {
            return Identifier.fromNamespaceAndPath(MegaMod.MODID, name);
        }

        public Holder<SoundEvent> value() {
            return holder;
        }
    }

    public static final List<Entry> entries = new ArrayList<>();

    private static Entry add(String name) {
        var id = Identifier.fromNamespaceAndPath(MegaMod.MODID, name);
        var holder = SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
        var entry = new Entry(name, holder);
        entries.add(entry);
        return entry;
    }

    public static final Entry SHARPEN = add("sharpen");
    public static final Entry MEDAL_USE = add("medal_use");
    public static final Entry EAGLE_BOOST = add("eagle_boost");
    public static final Entry POTION_GENERIC = add("potion_generic");
    public static final Entry INTELLECT_BUFF = add("intellect_buff");
    public static final Entry SPELL_HASTE_ACTIVATE_1 = add("spell_haste_activate_1");
    public static final Entry LIGHTNING_IMPACT_SMALL = add("lightning_impact_small");
    public static final Entry STUN_GENERIC = add("stun_generic");
    public static final Entry LEVITATE_GENERIC = add("levitate_generic");
    public static final Entry BLOODLUST_ACTIVATE = add("bloodlust_activate");
    public static final Entry SPELL_HASTE_ACTIVATE_2 = add("spell_haste_activate_2");
    public static final Entry BOW_STRING_ACTIVATE = add("bow_string_activate");
    public static final Entry HOLY_WATER_IMPACT = add("holy_water_impact");
    public static final Entry DEFENSE_ACTIVATE_1 = add("defense_activate_1");
    public static final Entry DEFENSE_ACTIVATE_2 = add("defense_activate_2");
    public static final Entry MELEE_ACTIVATE_1 = add("melee_activate_1");
    public static final Entry SPELL_POWER_ACTIVATE_2 = add("spell_power_activate_2");
    public static final Entry SPELL_POWER_ACTIVATE_3 = add("spell_power_activate_3");
    public static final Entry HEART_OF_BEAST_ACTIVATE = add("heart_of_beast_activate");
    public static final Entry HEALING_ZONE_ACTIVATE = add("healing_zone_activate");
    public static final Entry SPELL_ZONE_ACTIVATE = add("spell_zone_activate");
    public static final Entry MAGIC_ZONE_PRESENCE = add("magic_zone_presence");
    public static final Entry HORN_ACTIVATE = add("horn_activate");
    public static final Entry MONKEY_ACTIVATE = add("monkey_activate");
    public static final Entry HOOK_ACTIVATE = add("hook_activate");

    public static void init(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
