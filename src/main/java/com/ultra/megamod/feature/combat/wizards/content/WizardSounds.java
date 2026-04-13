package com.ultra.megamod.feature.combat.wizards.content;

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

/**
 * Sound event registration for all Wizard spell sounds.
 * Ported from Wizards mod - each Entry wraps a SoundEvent registered via DeferredRegister.
 * Sound files are located at assets/megamod/sounds/combat/ and referenced in sounds.json.
 */
public class WizardSounds {

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

    // Arcane sounds
    public static final Entry ARCANE_MISSILE_RELEASE = add(new Entry("combat.arcane_missile_release"));
    public static final Entry ARCANE_MISSILE_IMPACT = add(new Entry("combat.arcane_missile_impact"));
    public static final Entry ARCANE_SHOOT_SMALL = add(new Entry("combat.arcane_shoot_small"));
    public static final Entry ARCANE_BLAST_RELEASE = add(new Entry("combat.arcane_blast_release"));
    public static final Entry ARCANE_BLAST_IMPACT = add(new Entry("combat.arcane_blast_impact"));
    public static final Entry ARCANE_BEAM_START = add(new Entry("combat.arcane_beam_start"));
    public static final Entry ARCANE_BEAM_CASTING = add(new Entry("combat.arcane_beam_casting"));
    public static final Entry ARCANE_BEAM_IMPACT = add(new Entry("combat.arcane_beam_impact"));
    public static final Entry ARCANE_BEAM_RELEASE = add(new Entry("combat.arcane_beam_release"));

    // Fire sounds
    public static final Entry FIRE_SCORCH_IMPACT = add(new Entry("combat.fire_scorch_impact"));
    public static final Entry FIREBALL_IMPACT = add(new Entry("combat.fireball_impact"));
    public static final Entry FIRE_BREATH_START = add(new Entry("combat.fire_breath_start"));
    public static final Entry FIRE_BREATH_CASTING = add(new Entry("combat.fire_breath_casting"));
    public static final Entry FIRE_BREATH_RELEASE = add(new Entry("combat.fire_breath_release"));
    public static final Entry FIRE_BREATH_IMPACT = add(new Entry("combat.fire_breath_impact"));
    public static final Entry FIRE_METEOR_RELEASE = add(new Entry("combat.fire_meteor_release").travelDistance(48F));
    public static final Entry FIRE_METEOR_IMPACT = add(new Entry("combat.fire_meteor_impact"));
    public static final Entry FIRE_WALL_IGNITE = add(new Entry("combat.fire_wall_ignite"));

    // Frost sounds
    public static final Entry FROST_SHARD_IMPACT = add(new Entry("combat.frost_shard_impact"));
    public static final Entry FROST_NOVA_RELEASE = add(new Entry("combat.frost_nova_release"));
    public static final Entry FROST_NOVA_DAMAGE_IMPACT = add(new Entry("combat.frost_nova_damage_impact"));
    public static final Entry FROST_NOVA_EFFECT_IMPACT = add(new Entry("combat.frost_nova_effect_impact"));
    public static final Entry FROST_SHIELD_RELEASE = add(new Entry("combat.frost_shield_release"));
    public static final Entry FROST_SHIELD_IMPACT = add(new Entry("combat.frost_shield_impact"));
    public static final Entry FROST_BLIZZARD_CASTING = add(new Entry("combat.frost_blizzard_casting"));

    // Armor sounds
    public static final Entry WIZARD_ROBES_EQUIP = add(new Entry("combat.wizard_robes_equip_1").variants(3));

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
