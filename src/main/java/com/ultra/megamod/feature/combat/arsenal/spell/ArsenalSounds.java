package com.ultra.megamod.feature.combat.arsenal.spell;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class ArsenalSounds {

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.SOUND_EVENT, MegaMod.MODID);

    public static final class Entry {
        private final Identifier id;
        private final SoundEvent soundEvent;
        private DeferredHolder<SoundEvent, SoundEvent> holder;
        private int variants = 1;

        public Entry(Identifier id, SoundEvent soundEvent) {
            this.id = id;
            this.soundEvent = soundEvent;
        }

        public Entry(String name) {
            this(Identifier.fromNamespaceAndPath(MegaMod.MODID, name));
        }

        public Entry(Identifier id) {
            this(id, SoundEvent.createVariableRangeEvent(id));
        }

        public Entry travelDistance(float distance) {
            return new Entry(id, SoundEvent.createFixedRangeEvent(id, distance));
        }

        public Entry variants(int variants) {
            this.variants = variants;
            return this;
        }

        public Identifier id() {
            return id;
        }

        public SoundEvent soundEvent() {
            if (holder != null) return holder.get();
            return soundEvent;
        }

        public Holder<SoundEvent> entry() {
            return holder;
        }

        public int variants() {
            return variants;
        }
    }

    public static final List<Entry> entries = new ArrayList<>();
    private static Entry add(Entry entry) {
        entries.add(entry);
        entry.holder = SOUND_EVENTS.register(entry.id().getPath(), () -> entry.soundEvent);
        return entry;
    }

    public static final Entry shield_equip = add(new Entry("arsenal_shield_equip").variants(3));
    public static final Entry wither_impact = add(new Entry("arsenal_wither_impact"));
    public static final Entry poison_cloud_spawn = add(new Entry("arsenal_poison_cloud_spawn"));
    public static final Entry poison_cloud_tick = add(new Entry("arsenal_poison_cloud_tick"));
    public static final Entry leeching_impact = add(new Entry("arsenal_leeching_impact"));
    public static final Entry guardian_strike_release = add(new Entry("arsenal_guardian_strike_release"));
    public static final Entry guardian_strike_impact = add(new Entry("arsenal_guardian_strike_impact"));
    public static final Entry guardian_heal_impact = add(new Entry("arsenal_guardian_heal_impact"));
    public static final Entry missile_impact = add(new Entry("arsenal_missile_impact"));
    public static final Entry missile_release = add(new Entry("arsenal_missile_release"));
    public static final Entry sunder_impact = add(new Entry("arsenal_sunder_impact"));
    public static final Entry unyielding_impact = add(new Entry("arsenal_unyielding_impact"));
    public static final Entry shockwave_impact = add(new Entry("arsenal_shockwave_impact"));
    public static final Entry shockwave_release = add(new Entry("arsenal_shockwave_release"));
    public static final Entry spike_impact = add(new Entry("arsenal_spike_impact"));
    public static final Entry spell_cooldown_impact = add(new Entry("arsenal_spell_cooldown_impact"));
    public static final Entry swirling = add(new Entry("arsenal_swirling"));
    public static final Entry radiance_impact = add(new Entry("arsenal_radiance_impact"));
    public static final Entry rampaging_activate = add(new Entry("arsenal_rampaging_activate"));
    public static final Entry focusing_activate = add(new Entry("arsenal_focusing_activate"));
    public static final Entry surging_activate = add(new Entry("arsenal_surging_activate"));

    public static void init(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
