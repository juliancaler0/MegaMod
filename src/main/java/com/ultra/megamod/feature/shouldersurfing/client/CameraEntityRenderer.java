package com.ultra.megamod.feature.shouldersurfing.client;

import com.ultra.megamod.feature.shouldersurfing.api.client.ICameraEntityRenderer;
import com.ultra.megamod.feature.shouldersurfing.config.Config;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class CameraEntityRenderer implements ICameraEntityRenderer
{
	private final ShoulderSurfingImpl instance;
	private float cameraEntityAlpha = 1.0F;
	private boolean isRenderingCameraEntity;
	private EntityRenderState cameraEntityRenderState;

	public CameraEntityRenderer(ShoulderSurfingImpl instance)
	{
		this.instance = instance;
	}

	public boolean preRenderCameraEntity(Entity entity, float partialTick)
	{
		if(this.shouldSkipCameraEntityRendering(entity))
		{
			return true;
		}

		this.cameraEntityAlpha = computeCameraEntityAlpha(entity, partialTick);
		this.isRenderingCameraEntity = true;

		return false;
	}

	public void postRenderCameraEntity(Entity entity, float partialTick)
	{
		this.isRenderingCameraEntity = false;
	}

	private float computeCameraEntityAlpha(Entity entity, float partialTick)
	{
		if(!this.instance.isShoulderSurfing() || !Config.CLIENT.isPlayerTransparencyEnabled())
		{
			return 1.0F;
		}

		float alpha = 1.0F;
		ShoulderSurfingCamera camera = this.instance.getCamera();
		double cameraDistance = camera.getCameraDistance();
		float entityBbWidth = entity.getBbWidth();
		double minDistance = entityBbWidth * 2.0D;
		double maxDistance = entityBbWidth * 3.5D;

		if(cameraDistance < maxDistance)
		{
			float ratio = (float) Mth.clamp((cameraDistance - minDistance) / (maxDistance - minDistance), 0.0D, 1.0D);
			alpha = Math.min(alpha, ratio);
		}

		if(Config.CLIENT.turnPlayerTransparentWhenAiming() && this.instance.isAiming())
		{
			alpha = Math.min(alpha, 0.15F);
		}

		return Mth.clamp(alpha, 0.0F, 1.0F);
	}

	private boolean shouldSkipCameraEntityRendering(Entity cameraEntity)
	{
		ShoulderSurfingCamera camera = this.instance.getCamera();
		return this.instance.isShoulderSurfing() && !cameraEntity.isSpectator() &&
			(camera.getCameraDistance() < cameraEntity.getBbWidth() * Config.CLIENT.keepCameraOutOfHeadMultiplier() ||
				camera.getXRot() < Config.CLIENT.getHidePlayerWhenLookingUpAngle() - 90 ||
				cameraEntity instanceof Player player && player.isScoping());
	}

	public int applyCameraEntityAlphaContextAware(int color)
	{
		return this.isRenderingCameraEntity ? this.applyCameraEntityAlpha(color) : color;
	}

	public int applyCameraEntityAlpha(int color)
	{
		int cameraEntityAlpha = this.getCameraEntityAlphaAsInt();
		int alpha = ARGB.alpha(color);

		if(cameraEntityAlpha < alpha)
		{
			return ARGB.transparent(color) + (cameraEntityAlpha << 24);
		}

		return color;
	}

	public EntityRenderState getCameraEntityRenderState()
	{
		return this.cameraEntityRenderState;
	}

	public void setCameraEntityRenderState(EntityRenderState cameraEntityRenderState)
	{
		this.cameraEntityRenderState = cameraEntityRenderState;
	}

	@Override
	public boolean isRenderingCameraEntity()
	{
		return this.isRenderingCameraEntity;
	}

	@Override
	public float getCameraEntityAlpha()
	{
		return this.cameraEntityAlpha;
	}

	@Override
	public int getCameraEntityAlphaAsInt()
	{
		return ARGB.as8BitChannel(this.cameraEntityAlpha);
	}
}
