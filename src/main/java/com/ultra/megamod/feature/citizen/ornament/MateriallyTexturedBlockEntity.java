package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * Block entity that stores texture data for retexturable ornament blocks.
 * Uses ValueOutput/ValueInput for 1.21.11 persistence.
 *
 * Texture data is stored as:
 * - "TexCount" (int): number of component mappings
 * - "TexKey0", "TexKey1", ... (string): component IDs
 * - "TexVal0", "TexVal1", ... (string): block registry names
 */
public class MateriallyTexturedBlockEntity extends BlockEntity {

    private MaterialTextureData textureData = MaterialTextureData.EMPTY;

    public MateriallyTexturedBlockEntity(BlockPos pos, BlockState state) {
        super(OrnamentRegistry.ORNAMENT_BLOCK_ENTITY.get(), pos, state);
    }

    // ==================== Texture Data ====================

    public MaterialTextureData getTextureData() {
        return textureData;
    }

    public void setTextureData(MaterialTextureData data) {
        this.textureData = data != null ? data : MaterialTextureData.EMPTY;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ==================== Persistence via ValueOutput/ValueInput ====================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        Map<Identifier, Block> components = textureData.getTexturedComponents();
        output.putInt("TexCount", components.size());
        int i = 0;
        for (Map.Entry<Identifier, Block> entry : components.entrySet()) {
            output.putString("TexKey" + i, entry.getKey().toString());
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(entry.getValue());
            output.putString("TexVal" + i, blockId.toString());
            i++;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int count = input.getIntOr("TexCount", 0);
        if (count <= 0) {
            textureData = MaterialTextureData.EMPTY;
            return;
        }
        Map<Identifier, Block> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String key = input.getStringOr("TexKey" + i, "");
            String val = input.getStringOr("TexVal" + i, "");
            if (key.isEmpty() || val.isEmpty()) continue;
            Identifier componentId = Identifier.tryParse(key);
            Identifier blockId = Identifier.tryParse(val);
            if (componentId == null || blockId == null) continue;
            Block block = BuiltInRegistries.BLOCK.getValue(blockId);
            if (block != null && block != Blocks.AIR) {
                map.put(componentId, block);
            }
        }
        textureData = map.isEmpty() ? MaterialTextureData.EMPTY : new MaterialTextureData(map);
    }

    // ==================== Client Sync ====================

    /**
     * Provides the packet for syncing block entity data to the client
     * when the chunk is sent or when sendBlockUpdated is called.
     */
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Provides the initial data sent to the client when the chunk loads.
     * In 1.21.11 this takes a HolderLookup.Provider parameter.
     * The data is written via saveAdditional which is called internally by the
     * super implementation, so our texture data is automatically included.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }
}
