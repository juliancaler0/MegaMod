package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores the component-to-material mapping for a retextured block instance.
 * Each entry maps a component ID to the block whose texture should be applied.
 * Serializes to NBT for persistence and network sync.
 */
public class MaterialTextureData {

    /**
     * Empty singleton — no texture overrides.
     */
    public static final MaterialTextureData EMPTY = new MaterialTextureData(Collections.emptyMap());

    private static final String TAG_KEY = "ornament_textures";

    private final Map<Identifier, Block> texturedComponents;

    public MaterialTextureData(Map<Identifier, Block> texturedComponents) {
        this.texturedComponents = Map.copyOf(texturedComponents);
    }

    /**
     * Returns the material block for the given component, or the default block if not set.
     */
    public Block getTextureFor(Identifier componentId, Block defaultBlock) {
        return texturedComponents.getOrDefault(componentId, defaultBlock);
    }

    /**
     * Returns the material block for the given component, or Blocks.AIR if not set.
     */
    public Block getTextureFor(Identifier componentId) {
        return texturedComponents.getOrDefault(componentId, Blocks.AIR);
    }

    /**
     * Returns an unmodifiable view of all component mappings.
     */
    public Map<Identifier, Block> getTexturedComponents() {
        return texturedComponents;
    }

    public boolean isEmpty() {
        return texturedComponents.isEmpty();
    }

    // ==================== NBT Serialization ====================

    /**
     * Serialize to NBT. Each component ID maps to the block's registry name.
     */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        CompoundTag textures = new CompoundTag();
        for (Map.Entry<Identifier, Block> entry : texturedComponents.entrySet()) {
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(entry.getValue());
            textures.putString(entry.getKey().toString(), blockId.toString());
        }
        tag.put(TAG_KEY, textures);
        return tag;
    }

    /**
     * Deserialize from NBT.
     */
    public static MaterialTextureData fromNbt(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_KEY)) {
            return EMPTY;
        }
        CompoundTag textures = tag.getCompoundOrEmpty(TAG_KEY);
        Map<Identifier, Block> map = new HashMap<>();
        for (String key : textures.keySet()) {
            String blockName = textures.getStringOr(key, "");
            if (blockName.isEmpty()) continue;
            Identifier componentId = Identifier.tryParse(key);
            Identifier blockId = Identifier.tryParse(blockName);
            if (componentId == null || blockId == null) continue;
            Block block = BuiltInRegistries.BLOCK.getValue(blockId);
            if (block != null && block != Blocks.AIR) {
                map.put(componentId, block);
            }
        }
        if (map.isEmpty()) {
            return EMPTY;
        }
        return new MaterialTextureData(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaterialTextureData that = (MaterialTextureData) o;
        return Objects.equals(texturedComponents, that.texturedComponents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(texturedComponents);
    }

    @Override
    public String toString() {
        return "MaterialTextureData{" + texturedComponents + "}";
    }
}
