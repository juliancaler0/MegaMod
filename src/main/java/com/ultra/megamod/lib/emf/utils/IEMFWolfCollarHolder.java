package com.ultra.megamod.lib.emf.utils;

import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.animal.wolf.Wolf;

public interface
    IEMFWolfCollarHolder
{

    default boolean emf$hasCollarModel() {
        return emf$getCollarModel() != null;
    }

    WolfModel
        emf$getCollarModel();

    void emf$setCollarModel(
            WolfModel
            model);


}
