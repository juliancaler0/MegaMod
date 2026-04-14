package com.ultra.megamod.mixin.shouldersurfing;

import com.ultra.megamod.feature.shouldersurfing.api.client.IClientConfig;
import com.ultra.megamod.feature.shouldersurfing.api.model.Perspective;
import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingCamera;
import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingImpl;
import com.ultra.megamod.feature.shouldersurfing.math.Vec2f;
import com.ultra.megamod.feature.shouldersurfing.mixinducks.CameraDuck;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class MixinCamera implements CameraDuck
{
	@Shadow
	@Nullable
	private Level level;

	@Shadow
	@Nullable
	private Entity entity;

	@Shadow
	private float xRot;

	@Shadow
	private float yRot;

	@Unique
	private float shouldersurfing$zRot;

	@Shadow
	protected abstract void move(float x, float y, float z);

	@Shadow
	protected abstract void setRotation(float yRot, float xRot);

	@Inject
	(
		method = "setup",
		at = @At("HEAD")
	)
	private void shouldersurfing$resetZRot(CallbackInfo ci)
	{
		this.shouldersurfing$setZRot(0.0F);
	}

	@Inject
	(
		method = "setup",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
			shift = Shift.AFTER,
			ordinal = 0
		)
	)
	private void shouldersurfing$setupRotations(Level level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci)
	{
		if(Perspective.SHOULDER_SURFING == Perspective.current() && !(this.entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()))
		{
			ShoulderSurfingCamera camera = ShoulderSurfingImpl.getInstance().getCamera();
			Vec2f rotations = camera.calcRotations(this.entity, partialTick);
			this.setRotation(rotations.y(), rotations.x());
		}
	}

	@Redirect
	(
		method = "setup",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;move(FFF)V",
			ordinal = 0
		)
	)
	private void shouldersurfing$setupPosition(Camera cameraIn, float x, float y, float z, Level level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick)
	{
		if(Perspective.SHOULDER_SURFING == Perspective.current() && !(this.entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()))
		{
			ShoulderSurfingCamera camera = ShoulderSurfingImpl.getInstance().getCamera();
			Vec3 cameraOffset = camera.calcOffset(cameraIn, this.level, partialTick, this.entity);
			this.move((float) -cameraOffset.z(), (float) cameraOffset.y(), (float) -cameraOffset.x());
			Vec2f sway = camera.calcSway(camera, this.entity, partialTick);
			this.shouldersurfing$zRot = sway.y();
			this.setRotation(this.yRot, this.xRot + sway.x());
		}
		else
		{
			this.move(x, y, z);
		}
	}

	@ModifyVariable
	(
		method = "getFov",
		at = @At
		(
			value = "TAIL",
			shift = Shift.BY,
			by = -2
		),
		ordinal = 1,
		require = 0
	)
	private float shouldersurfing$calculateFov(float lerpedFov)
	{
		ShoulderSurfingImpl instance = ShoulderSurfingImpl.getInstance();
		IClientConfig config = instance.getClientConfig();

		if(instance.isShoulderSurfing() && config.isFovOverrideEnabled())
		{
			return (config.getFovOverride() / (float) Minecraft.getInstance().options.fov().get()) * lerpedFov;
		}

		return lerpedFov;
	}

	@Override
	public float shouldersurfing$getZRot()
	{
		return this.shouldersurfing$zRot;
	}

	@Override
	public void shouldersurfing$setZRot(float zRot)
	{
		this.shouldersurfing$zRot = zRot;
	}
}
