package com.ultra.megamod.lib.emf.utils;

import com.ultra.megamod.lib.emf.models.parts.EMFModelPartRoot;

public interface IEMFCustomModelHolder {

    default boolean emf$hasModel() {
        return emf$getModel() != null;
    }

    EMFModelPartRoot emf$getModel();

    void emf$setModel(EMFModelPartRoot model);
}
