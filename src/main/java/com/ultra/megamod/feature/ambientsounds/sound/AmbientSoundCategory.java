package com.ultra.megamod.feature.ambientsounds.sound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ultra.megamod.feature.ambientsounds.condition.AmbientCondition;
import com.ultra.megamod.feature.ambientsounds.condition.AmbientSelection;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngine;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngineLoadException;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientStackType;
import com.ultra.megamod.feature.ambientsounds.environment.AmbientEnvironment;
import com.ultra.megamod.feature.ambientsounds.util.AmbientDebugRenderer;

public class AmbientSoundCategory extends AmbientCondition {

    public String parent;
    public AmbientStackType stack = AmbientStackType.overwrite;

    public transient String name;

    public transient double volumeSetting = 1;

    public transient AmbientSoundCategory parentCategory;
    public transient List<AmbientSoundCategory> children = new ArrayList<>();

    public transient AmbientSelection selection;

    @Override
    public void init(AmbientEngine engine) throws AmbientEngineLoadException {
        super.init(engine);
        parentCategory = engine.getSoundCategory(parent);
        if (parentCategory != null)
            parentCategory.children.add(this);
        if (parentCategory == this)
            throw new AmbientEngineLoadException("Sound cateogry cannot have itself as a parent " + name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void postInit(HashSet<String> remaining) {
        remaining.remove(name);
        for (AmbientSoundCategory cat : children)
            cat.postInit(remaining);
    }

    public void tick(AmbientEnvironment env, AmbientSelection parentSelection) {
        selection = value(env);

        if (selection != null) {
            if (parentSelection != null)
                selection.last().subSelection = parentSelection;

            selection.mulSetting(volumeSetting);
        }

        for (AmbientSoundCategory child : children)
            child.tick(env, selection);
    }

    public void collectDetails(AmbientDebugRenderer text) {
        if (selection == null)
            return;
        // text.detail(name, selection.volume());
        for (AmbientSoundCategory child : children)
            child.collectDetails(text);
    }

}
