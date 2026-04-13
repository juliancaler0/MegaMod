package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@code name} / {@code names} predicate. Supports plain names, quoted multi-word names,
 * and {@code regex:} / {@code pattern:}.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class NameProperty extends StringArrayOrRegexProperty {


    protected NameProperty(String data) throws RandomPropertyException {
        super(data);
    }

    public static NameProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            String dataFromProperty = readPropertiesOrThrow(properties, propertyNum, "name", "names");

            ArrayList<String> names = new ArrayList<>();

            if (dataFromProperty.isBlank())
                throw new RandomPropertyException("Name failed");

            if (dataFromProperty.startsWith("regex:") || dataFromProperty.startsWith("pattern:")) {
                names.add(dataFromProperty);
            } else {
                Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(dataFromProperty);
                while (m.find()) {
                    names.add(m.group(1).replace("\"", "").trim());
                }
            }

            StringBuilder builder = new StringBuilder();
            for (String str : names) {
                builder.append(str).append(" ");
            }

            return new NameProperty(builder.toString().trim());
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return false;
    }

    @Override
    public @Nullable String getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity != null && etfEntity.entity() instanceof Player player) {
            return player.getName().getString();
        }
        if (etfEntity != null && etfEntity.hasCustomName()) {
            Component entityNameText = etfEntity.customName();
            if (entityNameText != null) {
                ComponentContents content = entityNameText.getContents();
                if (content instanceof PlainTextContents.LiteralContents literal) {
                    return literal.text();
                } else {
                    return entityNameText.getString();
                }
            }
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"name", "names"};
    }
}
