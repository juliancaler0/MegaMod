package com.ultra.megamod.feature.ambientsounds.condition;

import com.ultra.megamod.MegaMod;
import java.lang.reflect.Field;

import com.ultra.megamod.feature.ambientsounds.sound.AmbientSoundProperties;

public class AmbientSelection extends AmbientVolume {

    public final AmbientCondition condition;
    public AmbientSelection subSelection = null;

    public AmbientSelection(AmbientCondition condition) {
        super(1, condition.volume);
        this.condition = condition;
    }

    @Override
    protected void assign(AmbientVolume volume) {
        if (volume instanceof AmbientSelection sel)
            subSelection = sel.subSelection;
        super.assign(volume);
    }

    @Override
    public double conditionVolume() {
        return subSelection != null ? subSelection.conditionVolume() * super.conditionVolume() : super.conditionVolume();
    }

    @Override
    public double settingVolume() {
        return subSelection != null ? subSelection.settingVolume() * super.settingVolume() : super.settingVolume();
    }

    @Override
    public double volume() {
        return subSelection != null ? subSelection.volume() * super.volume() : super.volume();
    }

    public AmbientSelection last() {
        if (subSelection == null)
            return this;
        return subSelection.last();
    }

    public AmbientSoundProperties getProperties() {
        AmbientSoundProperties properties = new AmbientSoundProperties();
        assignProperties(properties);
        return properties;
    }

    protected void assignProperties(AmbientSoundProperties properties) {
        try {
            for (Field field : AmbientSoundProperties.class.getFields()) {
                Object value = field.get(condition);
                if (value != null)
                    field.set(properties, value);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            MegaMod.LOGGER.error("Failed to set ambient selection property via reflection", e);
        }

        if (subSelection != null)
            subSelection.assignProperties(properties);
    }

}
