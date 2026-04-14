package com.ultra.megamod.lib.etf.features.property_reading.properties.etf_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * {@code items} / {@code item} predicate. Supports the "magic" values {@code none},
 * {@code any}, {@code holding}, {@code wearing}, as well as named items.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ItemProperty extends StringArrayOrRegexProperty {


    protected ItemProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "items", "item").replaceAll("(?<=(^| ))minecraft:", ""));
    }

    public static ItemProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ItemProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return true;
    }

    @Override
    public boolean testEntityInternal(ETFEntityRenderState entity) {
        if (ARRAY.size() == 1
                && (ARRAY.stream().anyMatch((string) ->
                "none".equals(string) || "any".equals(string) || "holding".equals(string) || "wearing".equals(string)))) {
            if (ARRAY.contains("none")) {
                Iterable<ItemStack> equipped = entity.itemsEquipped();
                for (ItemStack item : equipped) {
                    if (item != null && !item.isEmpty()) {
                        return false;
                    }
                }
                return true;
            } else {
                Iterable<ItemStack> items;
                if (ARRAY.contains("any")) {
                    items = entity.itemsEquipped();
                } else if (ARRAY.contains("holding")) {
                    items = entity.handItems();
                } else {
                    items = entity.armorItems();
                }
                boolean found = false;
                for (ItemStack item : items) {
                    if (item != null && !item.isEmpty()) {
                        found = true;
                        break;
                    }
                }
                return found;
            }
        } else {
            Iterable<ItemStack> equipped = entity.itemsEquipped();
            boolean found = false;
            for (ItemStack item : equipped) {
                String itemString = item.getItem().toString().replaceFirst("^minecraft:", "");
                found = MATCHER.testString(itemString);
                if (found) break;
            }
            return found;
        }
    }

    @Override
    public @Nullable String getValueFromEntity(ETFEntityRenderState etfEntity) {
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"items", "item"};
    }
}
