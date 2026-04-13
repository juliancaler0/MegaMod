package com.ultra.megamod.feature.combat.rogues.util;

import com.ultra.megamod.MegaMod;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Sound event references for Rogues & Warriors content.
 * Ported from net.rogues.util.RogueSounds.
 *
 * Sound events are registered via sounds.json in the megamod namespace.
 * The .ogg files live under assets/megamod/sounds/combat/.
 */
public class RogueSounds {

    public static final class Entry {
        private final Identifier id;
        private final SoundEvent soundEvent;
        private int variants = 1;

        public Entry(String name) {
            this(Identifier.fromNamespaceAndPath(MegaMod.MODID, "combat." + name));
        }

        public Entry(Identifier id) {
            this.id = id;
            this.soundEvent = SoundEvent.createVariableRangeEvent(id);
        }

        public Entry variants(int variants) {
            this.variants = variants;
            return this;
        }

        public Identifier id() { return id; }
        public SoundEvent soundEvent() { return soundEvent; }
        public int variants() { return variants; }
    }

    public static final List<Entry> entries = new ArrayList<>();
    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final Entry SLICE_AND_DICE = add(new Entry("slice_and_dice"));
    public static final Entry SHOCK_POWDER_RELEASE = add(new Entry("shock_powder_release"));
    public static final Entry SHOCK_POWDER_IMPACT = add(new Entry("shock_powder_impact"));
    public static final Entry SHADOW_STEP_ARRIVE = add(new Entry("shadow_step_arrive"));
    public static final Entry SHADOW_STEP_DEPART = add(new Entry("shadow_step_depart"));
    public static final Entry VANISH_RELEASE = add(new Entry("vanish_release"));
    public static final Entry VANISH_COMBINED = add(new Entry("vanish_combined"));
    public static final Entry THROW = add(new Entry("throw"));
    public static final Entry THROW_IMPACT = add(new Entry("throw_impact"));
    public static final Entry SHOUT_RELEASE = add(new Entry("shout_release"));
    public static final Entry DEMORALIZE_IMPACT = add(new Entry("demoralize_impact"));
    public static final Entry CHARGE_ACTIVATE = add(new Entry("charge_activate"));
    public static final Entry WHIRLWIND = add(new Entry("whirlwind"));
    public static final Entry ROGUE_ARMOR_EQUIP = add(new Entry("rogue_armor"));
    public static final Entry WARRIOR_ARMOR_EQUIP = add(new Entry("warrior_armor"));
    public static final Entry WORKBENCH = add(new Entry("arms_workbench"));
    public static final Entry STEALTH_LEAVE = add(new Entry("stealth_leave"));

    /**
     * Plays a sound event at the given entity's position.
     */
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
