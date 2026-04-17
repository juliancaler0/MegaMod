package com.ultra.megamod.lib.spellengine.api.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigUtil;
import com.ultra.megamod.lib.spellengine.api.config.EffectConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Effects {
    // Per-namespace DeferredRegisters for effect entries. Entries whose Identifier uses
    // "skill_tree_rpgs:..." must register under that namespace, not "megamod:", otherwise
    // they collide with unrelated megamod effects that happen to share a path.
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, "megamod");
    private static final Map<String, DeferredRegister<MobEffect>> REGISTERS_BY_NAMESPACE = new HashMap<>();
    private static IEventBus boundEventBus;
    static { REGISTERS_BY_NAMESPACE.put("megamod", MOB_EFFECTS); }

    private static DeferredRegister<MobEffect> registerFor(String namespace) {
        return REGISTERS_BY_NAMESPACE.computeIfAbsent(namespace, ns -> {
            var register = DeferredRegister.create(Registries.MOB_EFFECT, ns);
            if (boundEventBus != null) register.register(boundEventBus);
            return register;
        });
    }

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
            var register = registerFor(entry.id.getNamespace());
            var holder = register.register(entry.id.getPath(), () -> entry.effect);
            entry.deferredSupplier = holder;
            // The DeferredHolder IS a Holder<MobEffect> - use it directly
            entry.entry = (Holder<MobEffect>) (Holder<?>) holder;
        }
    }

    /**
     * Call from MegaMod constructor to register the DeferredRegister.
     */
    public static void init(IEventBus modEventBus) {
        boundEventBus = modEventBus;
        for (var register : REGISTERS_BY_NAMESPACE.values()) {
            register.register(modEventBus);
        }
    }
}
