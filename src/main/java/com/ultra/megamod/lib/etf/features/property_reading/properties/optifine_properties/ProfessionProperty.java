package com.ultra.megamod.lib.etf.features.property_reading.properties.optifine_properties;

import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * {@code professions} predicate with optional per-profession level gating
 * (e.g. {@code librarian:1,3-4}). Ported 1:1 from Entity_Texture_Features (traben).
 */
public class ProfessionProperty extends StringArrayOrRegexProperty {


    protected ProfessionProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "professions"));
    }


    public static ProfessionProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new ProfessionProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }

    @Override
    public boolean testEntityInternal(ETFEntityRenderState entity) {
        if (entity != null && entity.entity() instanceof VillagerDataHolder villagerEntity) {
            String entityProfession = villagerEntity.getVillagerData()
                    .profession()
                    .toString().toLowerCase().replace("minecraft:", "");
            int entityProfessionLevel = villagerEntity.getVillagerData().level();
            boolean check = false;
            for (String str : ARRAY) {
                if (str != null) {
                    str = str.toLowerCase().replaceAll("\\s*", "").replace("minecraft:", "");
                    if (str.contains(":")) {
                        String[] data = str.split(":\\d");
                        if (entityProfession.contains(data[0]) || data[0].contains(entityProfession)) {
                            if (data.length == 2) {
                                String[] levels = data[1].split(",");
                                ArrayList<Integer> levelData = new ArrayList<>();
                                for (String lvls : levels) {
                                    if (lvls.contains("-")) {
                                        levelData.addAll(Arrays.asList(SimpleIntegerArrayProperty.getIntRange(lvls).getAllWithinRangeAsList()));
                                    } else {
                                        levelData.add(Integer.parseInt(lvls.replaceAll("\\D", "")));
                                    }
                                }
                                for (Integer i : levelData) {
                                    if (i == entityProfessionLevel) {
                                        check = true;
                                        break;
                                    }
                                }
                            } else {
                                check = true;
                                break;
                            }
                        }
                    } else {
                        if (entityProfession.contains(str) || str.contains(entityProfession)) {
                            check = true;
                            break;
                        }
                    }
                }
            }
            return check;
        }
        return false;
    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return false;
    }

    @Override
    protected String getValueFromEntity(ETFEntityRenderState entity) {
        return null;
    }


    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"professions"};
    }
}
