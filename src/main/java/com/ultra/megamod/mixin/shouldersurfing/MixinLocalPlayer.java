package com.ultra.megamod.mixin.shouldersurfing;

import com.mojang.authlib.GameProfile;
import com.ultra.megamod.feature.shouldersurfing.api.model.PickContext;
import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(value = LocalPlayer.class, priority = 1500)
public abstract class MixinLocalPlayer extends AbstractClientPlayer
{
	public MixinLocalPlayer(ClientLevel level, GameProfile gameProfile)
	{
		super(level, gameProfile);
	}

	@Redirect
	(
		method = "pick(DFZ)Lnet/minecraft/world/phys/HitResult;",
		at = @At
		(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"
		),
		require = 0
	)
	private EntityHitResult shouldersurfing$getEntityHitResult(Entity shooter, Vec3 startPos, Vec3 endPos, AABB boundingBox, Predicate<Entity> filter, double interactionRangeSq)
	{
		ShoulderSurfingImpl instance = ShoulderSurfingImpl.getInstance();

		if(instance.isShoulderSurfing())
		{
			PickContext pickContext = new PickContext.Builder(Minecraft.getInstance().gameRenderer.getMainCamera())
				.withEntity(shooter)
				.build();
			double interactionRange = Math.sqrt(interactionRangeSq);
			float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
			return instance.getObjectPicker().pickEntities(pickContext, interactionRange, partialTick);
		}

		return ProjectileUtil.getEntityHitResult(shooter, startPos, endPos, boundingBox, filter, interactionRangeSq);
	}

	@Override
	public void turn(double yRot, double xRot)
	{
		if(!ShoulderSurfingImpl.getInstance().getCamera().turn((LocalPlayer) (Object) this, yRot, xRot))
		{
			super.turn(yRot, xRot);
		}
	}
}
