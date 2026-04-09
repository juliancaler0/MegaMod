package com.ultra.megamod.feature.citizen.building.module;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A block selection setting for building modules.
 * Persists the block as its registry name string in NBT.
 */
public class BlockSetting implements ISetting<Block> {

    private final String key;
    private Block value;
    private final Block defaultValue;

    public BlockSetting(String key, Block defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Block getValue() {
        return value;
    }

    @Override
    public void setValue(Block value) {
        this.value = value;
    }

    @Override
    public Block getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void loadFromNbt(CompoundTag tag) {
        String blockId = tag.getStringOr(key, "");
        if (blockId.isEmpty()) {
            this.value = defaultValue;
            return;
        }
        Identifier id = Identifier.tryParse(blockId);
        if (id != null && BuiltInRegistries.BLOCK.containsKey(id)) {
            this.value = BuiltInRegistries.BLOCK.getValue(id);
        } else {
            this.value = defaultValue;
        }
    }

    @Override
    public void saveToNbt(CompoundTag tag) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(value);
        tag.putString(key, id != null ? id.toString() : "");
    }

    @Override
    public String toString() {
        Identifier id = BuiltInRegistries.BLOCK.getKey(value);
        return "BlockSetting{key='" + key + "', value=" + (id != null ? id : "unknown") + "}";
    }
}
