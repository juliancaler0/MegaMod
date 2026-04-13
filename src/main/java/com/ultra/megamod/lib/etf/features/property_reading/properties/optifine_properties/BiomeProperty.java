package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.google.common.base.CaseFormat;
import com.ultra.megamod.lib.etf.ETF;
import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;


/**
 * {@code biomes} / {@code biome} predicate with legacy-OptiFine camel-case biome name
 * compatibility shims (e.g. {@code ExtremeHills} -> {@code stony_peaks}).
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class BiomeProperty extends StringArrayOrRegexProperty {


    protected BiomeProperty(String data) throws RandomProperty.RandomPropertyException {
        super(data);
    }

    public static BiomeProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            String dataFromProperty = RandomProperty.readPropertiesOrThrow(properties, propertyNum, "biomes", "biome");
            if (dataFromProperty.startsWith("regex:") || dataFromProperty.startsWith("pattern:")) {
                return new BiomeProperty(dataFromProperty);
            } else {
                boolean prints = dataFromProperty.startsWith("print:");
                String[] biomeList = (prints ? dataFromProperty.substring(6) : dataFromProperty).split("\\s+");

                if (biomeList.length > 0) {
                    for (int currentIndex = 0; currentIndex < biomeList.length; currentIndex++) {
                        biomeList[currentIndex] = biomeList[currentIndex].strip().replaceAll("^minecraft:", "");

                        switch (biomeList[currentIndex]) {
                            case "ExtremeHills" -> biomeList[currentIndex] = "stony_peaks";
                            case "Forest", "ForestHills" -> biomeList[currentIndex] = "forest";
                            case "Taiga", "TaigaHills" -> biomeList[currentIndex] = "taiga";
                            case "Swampland" -> biomeList[currentIndex] = "swamp";
                            case "Hell" -> biomeList[currentIndex] = "nether_wastes";
                            case "Sky" -> biomeList[currentIndex] = "the_end";
                            case "IcePlains" -> biomeList[currentIndex] = "snowy_plains";
                            case "IceMountains" -> biomeList[currentIndex] = "snowy_slopes";
                            case "MushroomIsland", "MushroomIslandShore" -> biomeList[currentIndex] = "mushroom_fields";
                            case "DesertHills", "Desert" -> biomeList[currentIndex] = "desert";
                            case "ExtremeHillsEdge" -> biomeList[currentIndex] = "meadow";
                            case "Jungle", "JungleHills" -> biomeList[currentIndex] = "jungle";
                            default -> {
                                String currentBiome = biomeList[currentIndex];
                                if (!currentBiome.contains("_") && !currentBiome.equals(currentBiome.toLowerCase())) {
                                    biomeList[currentIndex] = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, currentBiome);
                                }
                            }
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    if (prints) builder.append("print:");
                    for (String str : biomeList) {
                        builder.append(str).append(" ");
                    }
                    return new BiomeProperty(builder.toString().trim().toLowerCase());
                }
            }
            return null;
        } catch (RandomProperty.RandomPropertyException e) {
            return null;
        }
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return true;
    }


    @Override
    public @Nullable String getValueFromEntity(ETFEntityRenderState etfEntity) {
        if (etfEntity.world() != null && etfEntity.blockPos() != null) {
            String biome = ETF.getBiomeString(etfEntity.world(), etfEntity.blockPos());
            return biome == null ? null : biome.replace("minecraft:", "");
        }
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"biomes", "biome"};
    }
}
