package com.ultra.megamod.feature.combat.paladins.content;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class PaladinSounds {

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
        // Register via DeferredRegister
        entry.holder = SOUND_EVENTS.register(entry.id().getPath(), () -> entry.soundEvent);
        return entry;
    }

    public static final Entry paladin_armor_equip = add(new Entry("plate_equip").variants(3));
    public static final Entry priest_robe_equip = add(new Entry("cloth_equip").variants(3));
    public static final Entry shield_equip = add(new Entry("shield_equip"));
    public static final Entry holy_barrier_activate = add(new Entry("holy_barrier_activate"));
    public static final Entry holy_barrier_idle = add(new Entry("holy_barrier_idle"));
    public static final Entry holy_barrier_impact = add(new Entry("holy_barrier_impact"));
    public static final Entry holy_barrier_deactivate = add(new Entry("holy_barrier_deactivate"));
    public static final Entry divine_protection_release = add(new Entry("divine_protection_release"));
    public static final Entry judgement_impact = add(new Entry("judgement_impact").travelDistance(48F));
    public static final Entry divine_protection_impact = add(new Entry("divine_protection_impact"));
    public static final Entry battle_banner_release = add(new Entry("battle_banner_release"));
    public static final Entry battle_banner_presence = add(new Entry("battle_banner_presence"));
    public static final Entry holy_shock_damage = add(new Entry("holy_shock_damage"));
    public static final Entry holy_shock_heal = add(new Entry("holy_shock_heal"));
    public static final Entry holy_beam_start_casting = add(new Entry("holy_beam_start_casting"));
    public static final Entry holy_beam_casting = add(new Entry("holy_beam_casting"));
    public static final Entry holy_beam_damage = add(new Entry("holy_beam_damage"));
    public static final Entry holy_beam_heal = add(new Entry("holy_beam_heal"));
    public static final Entry holy_beam_release = add(new Entry("holy_beam_release"));

    public static void init(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    public static void playSoundEvent(Level level, Entity entity, SoundEvent soundEvent) {
        playSoundEvent(level, entity, soundEvent, 1, 1);
    }

    public static void playSoundEvent(Level level, Entity entity, SoundEvent soundEvent, float volume, float pitch) {
        level.playSound(
                (Player) null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                soundEvent,
                SoundSource.PLAYERS,
                volume,
                pitch);
    }
}
