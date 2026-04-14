package com.ultra.megamod.feature.relics.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Game-bus handlers that implement the behavior of the 6 ported relic mob effects.
 * Registration of the effects themselves is in RelicEffectRegistry.
 */
@EventBusSubscriber(modid = "megamod")
public class RelicEffectEvents
{
	// ===== AntiHeal: cancel healing while effect is active =====
	@SubscribeEvent
	public static void onLivingHeal(LivingHealEvent event)
	{
		LivingEntity entity = event.getEntity();
		if(entity.hasEffect(RelicEffectRegistry.ANTI_HEAL))
		{
			event.setCanceled(true);
		}
	}

	// ===== Bleeding: tick damage every 20 ticks =====
	@SubscribeEvent
	public static void onLivingTick(EntityTickEvent.Post event)
	{
		if(!(event.getEntity() instanceof LivingEntity entity)) return;

		if(entity.hasEffect(RelicEffectRegistry.BLEEDING) && entity.tickCount % 20 == 0 && entity.level() instanceof ServerLevel server)
		{
			float dmg = Math.min(10.0F, entity.getHealth() * 0.05F);
			entity.hurt(server.damageSources().magic(), dmg);
		}
	}

	// ===== Stun: cancel attacks while stunned =====
	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event)
	{
		Player player = event.getEntity();
		if(player.hasEffect(RelicEffectRegistry.STUN))
		{
			event.setCanceled(true);
		}
	}

	// ===== Stun: cancel interactions while stunned =====
	@SubscribeEvent
	public static void onInteract(PlayerInteractEvent.LeftClickBlock event)
	{
		if(event.getEntity().hasEffect(RelicEffectRegistry.STUN))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		if(event.getEntity().hasEffect(RelicEffectRegistry.STUN))
		{
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		if(event.getEntity().hasEffect(RelicEffectRegistry.STUN))
		{
			event.setCanceled(true);
		}
	}

	// ===== Client-side effects: Confusion, Paralysis =====
	@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
	public static class ClientEvents
	{
		@SubscribeEvent
		public static void onMovementInput(MovementInputUpdateEvent event)
		{
			Player player = event.getEntity();
			ClientInput input = event.getInput();
			com.ultra.megamod.mixin.shouldersurfing.ClientInputAccessor accessor =
				(com.ultra.megamod.mixin.shouldersurfing.ClientInputAccessor) input;

			// Paralysis: zero out all movement
			if(player.hasEffect(RelicEffectRegistry.PARALYSIS))
			{
				input.keyPresses = new Input(false, false, false, false, false, false, false);
				accessor.shouldersurfing$setMoveVector(Vec2.ZERO);
				return;
			}

			// Confusion: invert WASD
			if(player.hasEffect(RelicEffectRegistry.CONFUSION))
			{
				Vec2 mv = input.getMoveVector();
				accessor.shouldersurfing$setMoveVector(new Vec2(-mv.x, -mv.y));
			}

			// Stun: also zero out movement
			if(player.hasEffect(RelicEffectRegistry.STUN))
			{
				accessor.shouldersurfing$setMoveVector(Vec2.ZERO);
				Input kp = input.keyPresses;
				input.keyPresses = new Input(kp.forward(), kp.backward(), kp.left(), kp.right(), false, false, false);
			}
		}

		// Vanishing: hide player model client-side via RenderLivingEvent (partial — cannot
		// cancel other players. Falls back to MC's native invisibility for full effect.)
		@SubscribeEvent
		public static void onRenderLiving(net.neoforged.neoforge.client.event.RenderLivingEvent.Pre<?, ?, ?> event)
		{
			// Render state doesn't carry effect info directly; best-effort match on the local player.
			Player localPlayer = Minecraft.getInstance().player;
			if(localPlayer != null && localPlayer.hasEffect(RelicEffectRegistry.VANISHING))
			{
				// Only cancel if this render state represents the local player
				if(event.getRenderState().x == localPlayer.getX() && event.getRenderState().z == localPlayer.getZ())
				{
					event.setCanceled(true);
				}
			}
		}
	}
}
