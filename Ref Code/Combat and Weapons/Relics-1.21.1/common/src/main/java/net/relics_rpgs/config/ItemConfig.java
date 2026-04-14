package net.relics_rpgs.config;

import net.fabricmc.loader.api.FabricLoader;
import net.spell_engine.api.config.AttributeModifier;

import java.util.LinkedHashMap;
import java.util.List;

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
                    && FabricLoader.getInstance().isModLoaded(this.conditional_attributes.required_mod)) {
                return this.conditional_attributes.attributes();
            }
            return this.attributes;
        }
    }
}