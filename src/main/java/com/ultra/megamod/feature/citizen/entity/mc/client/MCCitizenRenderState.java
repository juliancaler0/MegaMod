package com.ultra.megamod.feature.citizen.entity.mc.client;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for MCEntityCitizen.
 * Carries data from the entity to the renderer each frame.
 */
public class MCCitizenRenderState extends HumanoidRenderState {
    public boolean isFemale = false;
    public int textureId = 0;
    public String textureSuffix = "_a";
    public String jobName = "";
    public boolean isChild = false;
    public String citizenName = "Citizen";
}
