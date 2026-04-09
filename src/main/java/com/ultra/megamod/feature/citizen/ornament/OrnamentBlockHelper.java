package com.ultra.megamod.feature.citizen.ornament;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared logic for all ornament block subclasses (slab, stair, fence, etc.).
 * Centralizes OrnamentData transfer and component building.
 */
public final class OrnamentBlockHelper {

    private OrnamentBlockHelper() {}

    /**
     * Transfers OrnamentData from the placed item stack to the block entity at the given position.
     */
    public static void transferOrnamentData(Level level, BlockPos pos, ItemStack stack) {
        if (level.isClientSide()) return;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MateriallyTexturedBlockEntity texturedBE) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("OrnamentData")) {
                    MaterialTextureData data = MaterialTextureData.fromNbt(tag.getCompoundOrEmpty("OrnamentData"));
                    texturedBE.setTextureData(data);
                }
            }
        }
    }

    /**
     * Wraps the standard getDrops to preserve ornament data on dropped items.
     */
    public static List<ItemStack> getDropsWithData(List<ItemStack> drops, LootParams.Builder builder, Block block) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MateriallyTexturedBlockEntity texturedBE && !texturedBE.getTextureData().isEmpty()) {
            CompoundTag ornamentNbt = texturedBE.getTextureData().toNbt();
            for (ItemStack drop : drops) {
                if (drop.getItem() == block.asItem()) {
                    CustomData customData = drop.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();
                    tag.put("OrnamentData", ornamentNbt);
                    drop.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }
        }
        return drops;
    }

    /**
     * Builds the standard component list for an OrnamentBlockType.
     */
    public static List<IMateriallyTexturedBlockComponent> buildComponents(@Nullable OrnamentBlockType type) {
        List<IMateriallyTexturedBlockComponent> components = new ArrayList<>();
        if (type == null) {
            components.add(new SimpleRetexturableComponent(
                    Identifier.fromNamespaceAndPath("megamod", "ornament_main"),
                    Blocks.OAK_PLANKS, false));
            return components;
        }

        int count = type.getComponentCount();
        String typeId = type.getId();

        switch (count) {
            case 1:
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_main"),
                        Blocks.OAK_PLANKS, false));
                break;
            case 2:
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_frame"),
                        Blocks.OAK_PLANKS, false));
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_panel"),
                        Blocks.OAK_LOG, false));
                break;
            case 3:
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_roof"),
                        Blocks.OAK_PLANKS, false));
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_support"),
                        Blocks.OAK_LOG, false));
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_cover"),
                        Blocks.COBBLESTONE, true));
                break;
            default:
                for (int i = 0; i < count; i++) {
                    components.add(new SimpleRetexturableComponent(
                            Identifier.fromNamespaceAndPath("megamod", typeId + "_component_" + i),
                            Blocks.OAK_PLANKS, i > 0));
                }
                break;
        }
        return components;
    }
}
