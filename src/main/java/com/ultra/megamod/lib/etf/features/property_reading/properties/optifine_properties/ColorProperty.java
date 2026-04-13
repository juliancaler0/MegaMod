package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.WoolCarpetBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code colors} / {@code collarColors} predicate. Reads the collar / wool / shulker /
 * llama-carpet / tropical-fish colour off supported entities.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ColorProperty extends StringArrayOrRegexProperty {


    protected ColorProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(RandomProperty.readPropertiesOrThrow(properties, propertyNum, "colors", "collarColors"));
    }

    public static ColorProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ColorProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return true;
    }

    @Override
    @Nullable
    protected String getValueFromEntity(ETFEntityRenderState state) {
        if (state != null) {
            var entity = state.entity();

            if (entity instanceof Llama llama) {
                DyeColor str;
                if (llama.getBodyArmorItem().is(ItemTags.WOOL_CARPETS)
                        && llama.getBodyArmorItem().getItem() instanceof BlockItem blockItem
                        && blockItem.getBlock() instanceof WoolCarpetBlock woolCarpetBlock) {
                    str = woolCarpetBlock.getColor();
                } else {
                    str = null;
                }
                if (str != null) {
                    return str.getName();
                }
            }
            if (entity instanceof Cat cat) return cat.getCollarColor().getName();
            if (entity instanceof Shulker shulker) {
                DyeColor str = shulker.getColor();
                if (str != null) {
                    return str.getName();
                }
            }
            return switch (entity) {
                case Wolf wolf -> wolf.getCollarColor().getName();
                case Sheep sheep -> sheep.getColor().getName();
                case TropicalFish fishy -> fishy.getBaseColor().getName();
                default -> null;
            };
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"colors", "collarColors"};
    }
}
