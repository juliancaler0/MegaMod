package com.ultra.megamod.feature.shouldersurfing.api.client;

import net.minecraft.world.entity.Entity;

public interface ICrosshairRenderer
{
	boolean doRenderCrosshair();

	boolean doRenderObstructionCrosshair();

	boolean doRenderObstructionIndicator();

	boolean isCrosshairDynamic(Entity entity);
}
