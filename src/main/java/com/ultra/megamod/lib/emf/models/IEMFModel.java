package com.ultra.megamod.lib.emf.models;

import com.ultra.megamod.lib.emf.models.parts.EMFModelPartRoot;

public interface IEMFModel {


    boolean emf$isEMFModel();

    EMFModelPartRoot emf$getEMFRootModel();

}
