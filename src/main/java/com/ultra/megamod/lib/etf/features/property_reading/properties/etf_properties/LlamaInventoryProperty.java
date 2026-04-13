package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.animal.equine.Llama;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

/**
 * {@code llamaInventory} predicate. Ported 1:1 from Entity_Texture_Features (traben).
 */
public class LlamaInventoryProperty extends SimpleIntegerArrayProperty {


    protected LlamaInventoryProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(getGenericIntegerSplitWithRanges(properties, propertyNum, "llamaInventory"));
    }

    public static LlamaInventoryProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new LlamaInventoryProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"llamaInventory"};
    }

    @Override
    protected int getValueFromEntity(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof Llama llama)
            return llama.getInventoryColumns();
        return Integer.MIN_VALUE;
    }
}
