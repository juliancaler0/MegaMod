package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code variant} / {@code variants} predicate. Returns the entity type's path for
 * living entities, and a descriptive "variant" string for a selection of block
 * entities (signs/shulker boxes/beds/decorated pots/skulls).
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class VariantProperty extends StringArrayOrRegexProperty {


    protected VariantProperty(String string) throws RandomPropertyException {
        super(string);
    }

    public static VariantProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new VariantProperty(readPropertiesOrThrow(properties, propertyNum, "variant", "variants"));
        } catch (RandomProperty.RandomPropertyException var3) {
            return null;
        }
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return false;
    }


    public @Nullable String getValueFromEntity(ETFEntityRenderState state) {
        if (state == null) return null;
        var etfEntity = state.entity();
        if (etfEntity instanceof Entity) {
            return BuiltInRegistries.ENTITY_TYPE.getResourceKey(((Entity) etfEntity).getType())
                    .map(key -> key.identifier().getPath()).orElse(null);
        } else if (etfEntity instanceof BlockEntity) {
            if (etfEntity instanceof SignBlockEntity signBlockEntity
                    && signBlockEntity.getBlockState().getBlock() instanceof SignBlock abstractSignBlock) {
                return abstractSignBlock.type().name();
            }
            if (etfEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity
                    && shulkerBoxBlockEntity.getBlockState().getBlock() instanceof ShulkerBoxBlock shulkerBoxBlock) {
                return String.valueOf(shulkerBoxBlock.getColor());
            }
            if (etfEntity instanceof BedBlockEntity bedBlockEntity
                    && bedBlockEntity.getBlockState().getBlock() instanceof BedBlock bedBlock) {
                return String.valueOf(bedBlock.getColor());
            }
            if (etfEntity instanceof DecoratedPotBlockEntity pot) {
                PotDecorations sherds = pot.getDecorations();
                return (sherds.back().isPresent() ? sherds.back().get().getDescriptionId() : "none")
                        + "," +
                        (sherds.left().isPresent() ? sherds.left().get().getDescriptionId() : "none")
                        + "," +
                        (sherds.right().isPresent() ? sherds.right().get().getDescriptionId() : "none")
                        + "," +
                        (sherds.front().isPresent() ? sherds.front().get().getDescriptionId() : "none");
            }
            String suffix = "";
            if (etfEntity instanceof SkullBlockEntity skull) {
                suffix = "_direction_" + skull.getBlockState().getValue(SkullBlock.ROTATION);
            }

            return BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(((BlockEntity) etfEntity).getType())
                    .map(key -> key.identifier().getPath()).orElse(null) + suffix;
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"variant", "variants"};
    }
}
