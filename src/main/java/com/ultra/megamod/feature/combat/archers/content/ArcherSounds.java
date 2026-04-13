package com.ultra.megamod.feature.combat.archers.content;

import com.ultra.megamod.feature.combat.archers.ArchersMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ArcherSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ArchersMod.ID);

    public static class Entry {
        private final Identifier id;
        private final SoundEvent soundEvent;
        private Supplier<SoundEvent> deferredEntry;
        private int variants = 1;

        public Entry(Identifier id, SoundEvent soundEvent) {
            this.id = id;
            this.soundEvent = soundEvent;
        }

        public Entry(String name) {
            this(Identifier.fromNamespaceAndPath(ArchersMod.ID, name));
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
            return deferredEntry != null ? deferredEntry.get() : soundEvent;
        }

        public Holder<SoundEvent> entry() {
            if (deferredEntry instanceof Holder<?>) {
                return (Holder<SoundEvent>) deferredEntry;
            }
            // Fallback - wrap in a direct holder
            return Holder.direct(soundEvent());
        }

        public int variants() {
            return variants;
        }
    }

    public static final List<Entry> entries = new ArrayList<>();
    public static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final Entry MARKER_SHOT = add(new Entry("marker_shot"));
    public static final Entry ENTANGLING_ROOTS = add(new Entry("entangling_roots"));
    public static final Entry BOW_PULL = add(new Entry("bow_pull"));
    public static final Entry MAGIC_ARROW_IMPACT = add(new Entry("magic_arrow_impact"));
    public static final Entry MAGIC_ARROW_RELEASE = add(new Entry("magic_arrow_release"));
    public static final Entry MAGIC_ARROW_START = add(new Entry("magic_arrow_start"));
    public static final Entry WORKBENCH = add(new Entry("archers_workbench"));
    public static final Entry ARCHER_ARMOR_EQUIP = add(new Entry("archer_armor"));

    public static void register(IEventBus modEventBus) {
        for (var entry : entries) {
            var holder = SOUND_EVENTS.register(entry.id().getPath(), () -> entry.soundEvent);
            entry.deferredEntry = holder;
        }
        SOUND_EVENTS.register(modEventBus);
    }
}
