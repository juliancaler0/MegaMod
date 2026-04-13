package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigUtil;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Effects {
    // Shared DeferredRegister for all SpellEngine-style effects
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "megamod");

    public static final class Entry {
        public final Identifier id;
        public final String title;
        public final String description;
        public final MobEffect effect;
        public final EffectConfig defaults;
        public EffectConfig config;
        public Holder<MobEffect> entry;
        private Supplier<MobEffect> deferredSupplier;

        public Entry(Identifier id, String title, String description, MobEffect effect) {
            this(id, title, description, effect, EffectConfig.EMPTY);
        }
        public Entry(Identifier id, String title, String description, MobEffect effect, EffectConfig config) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.effect = effect;
            this.defaults = config;
            this.config = config;
        }

        public EffectConfig config() {
            return config;
        }
    }

    public static void register(List<Entry> entries, Map<String, EffectConfig> effects) {
        for (var entry: entries) {
            var key = entry.id.toString();
            var current = effects.get(key);
            if (current != null) {
                entry.config = current;
            } else {
                effects.put(key, entry.config);
            }

            var modifiers = ConfigUtil.modifiersFrom(entry.id, entry.config.attributes());
            for (var modifier : modifiers) {
                entry.effect
                        .addAttributeModifier(modifier.attribute(),
                                modifier.modifier().id(),
                                modifier.modifier().amount(),
                                modifier.modifier().operation());
            }
        }

        for (var entry: entries) {
            var holder = MOB_EFFECTS.register(entry.id.getPath(), () -> entry.effect);
            entry.deferredSupplier = holder;
            // The DeferredHolder IS a Holder<MobEffect> - use it directly
            entry.entry = (Holder<MobEffect>) (Holder<?>) holder;
        }
    }

    /**
     * Call from MegaMod constructor to register the DeferredRegister.
     */
    public static void init(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}
