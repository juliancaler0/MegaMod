package com.ultra.megamod.feature.combat.relics.config;

import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import net.neoforged.fml.ModList;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Ported 1:1 from Relics-1.21.1's net.relics_rpgs.config.ItemConfig.
 * Fabric's FabricLoader.isModLoaded(...) is mapped to NeoForge ModList.get().isLoaded(...).
 * The config file loader (tiny_config) is omitted — default values are built in by
 * RelicItems directly, matching the defaults shipped by the ref mod.
 */
public class ItemConfig {
    public LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();

    public record ConditionalAttributes(String required_mod, List<AttributeModifier> attributes) { }

    public static class Entry {
        public static final Entry EMPTY = new Entry();
        public int durability = 0;
        public ConditionalAttributes conditional_attributes = null;
        public List<AttributeModifier> attributes = List.of();

        public Entry withAttributes(List<AttributeModifier> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Entry withConditionalAttributes(String required_mod, List<AttributeModifier> attributes) {
            this.conditional_attributes = new ConditionalAttributes(required_mod, attributes);
            return this;
        }

        public List<AttributeModifier> selectedAttributes() {
            if (this.conditional_attributes != null
                    && this.conditional_attributes.required_mod != null
                    && ModList.get() != null
                    && ModList.get().isLoaded(this.conditional_attributes.required_mod)) {
                return this.conditional_attributes.attributes();
            }
            return this.attributes;
        }
    }
}
