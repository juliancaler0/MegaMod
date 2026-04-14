package com.ultra.megamod.feature.shouldersurfing;

import com.ultra.megamod.feature.shouldersurfing.client.CrosshairRenderer;
import com.ultra.megamod.feature.shouldersurfing.client.InputHandler;
import com.ultra.megamod.feature.shouldersurfing.client.ShoulderSurfingImpl;
import com.ultra.megamod.feature.shouldersurfing.config.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.FrameGraphSetupEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Wires the ShoulderSurfing feature into MegaMod's client lifecycle.
 */
public class ShoulderSurfingClientEvents
{
	static boolean pendingInit = false;

	public static void init(IEventBus modEventBus, ModContainer modContainer)
	{
		modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC, "megamod-shouldersurfing.toml");
		modEventBus.addListener(ShoulderSurfingClientEvents::onConfigLoad);
		modEventBus.addListener(ShoulderSurfingClientEvents::onConfigReload);
		modEventBus.addListener(ShoulderSurfingClientEvents::onRegisterKeyMappings);
		modEventBus.addListener(ShoulderSurfingClientEvents::onRegisterGuiLayers);
	}

	private static void onConfigLoad(ModConfigEvent.Loading event)
	{
		if(event.getConfig().getSpec() == Config.CLIENT_SPEC)
		{
			// ModConfigEvent.Loading fires before Minecraft.getInstance() is constructed.
			// Calling ShoulderSurfingImpl.getInstance().init() here NPEs inside
			// Perspective.current() / ShoulderSurfingCamera.init() when they reach for
			// the client instance. Defer the real init until the first client tick.
			pendingInit = true;
		}
	}

	private static void onConfigReload(ModConfigEvent.Reloading event)
	{
		if(event.getConfig().getSpec() == Config.CLIENT_SPEC)
		{
			Config.onConfigReload();
		}
	}

	private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
	{
		for(net.minecraft.client.KeyMapping mapping : InputHandler.ALL)
		{
			event.register(mapping);
		}
	}

	private static void onRegisterGuiLayers(RegisterGuiLayersEvent event)
	{
		event.registerBelow(VanillaGuiLayers.CROSSHAIR,
			Identifier.fromNamespaceAndPath("megamod", "shouldersurfing_pre_crosshair"),
			ShoulderSurfingClientEvents::preRenderCrosshair);
		event.registerAbove(VanillaGuiLayers.CROSSHAIR,
			Identifier.fromNamespaceAndPath("megamod", "shouldersurfing_post_crosshair"),
			ShoulderSurfingClientEvents::postRenderCrosshair);
	}

	private static void preRenderCrosshair(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker)
	{
		CrosshairRenderer renderer = ShoulderSurfingImpl.getInstance().getCrosshairRenderer();

		if(renderer.doRenderCrosshair())
		{
			renderer.preRenderCrosshair(guiGraphics);
		}
	}

	private static void postRenderCrosshair(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker)
	{
		CrosshairRenderer renderer = ShoulderSurfingImpl.getInstance().getCrosshairRenderer();

		if(renderer.doRenderCrosshair())
		{
			renderer.postRenderCrosshair(guiGraphics);
		}
	}

	@EventBusSubscriber(modid = "megamod", value = Dist.CLIENT)
	public static class GameBusEvents
	{
		@SubscribeEvent
		public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event)
		{
			if(pendingInit && Minecraft.getInstance() != null && Minecraft.getInstance().options != null)
			{
				pendingInit = false;
				try
				{
					ShoulderSurfingImpl.getInstance().init();
				}
				catch(Throwable t)
				{
					org.slf4j.LoggerFactory.getLogger("ShoulderSurfing").error("Deferred init failed", t);
				}
			}
			if(Minecraft.getInstance().level != null)
			{
				ShoulderSurfingImpl.getInstance().tick();
			}
		}

		@SubscribeEvent
		public static void onPlayerTick(PlayerTickEvent.Post event)
		{
			if(event.getEntity() == Minecraft.getInstance().player)
			{
				ShoulderSurfingImpl.getInstance().updatePlayerRotations();
			}
		}

		@SubscribeEvent
		public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event)
		{
			ShoulderSurfingImpl.getInstance().resetState();
		}

		@SubscribeEvent
		public static void onRespawn(ClientPlayerNetworkEvent.Clone event)
		{
			ShoulderSurfingImpl.getInstance().resetState();
		}

		@SubscribeEvent
		public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event)
		{
			ShoulderSurfingImpl instance = ShoulderSurfingImpl.getInstance();

			if(instance.isShoulderSurfing() && event.getCamera().entity() != null)
			{
				float zRot = ((com.ultra.megamod.feature.shouldersurfing.mixinducks.CameraDuck) (Object) event.getCamera()).shouldersurfing$getZRot();
				event.setRoll(zRot);
			}
		}

		@SubscribeEvent
		public static void onFrameGraphSetup(FrameGraphSetupEvent event)
		{
			ShoulderSurfingImpl instance = ShoulderSurfingImpl.getInstance();

			if(instance.isShoulderSurfing())
			{
				float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(true);
				Camera camera = event.getCamera();

				if(camera.entity() != null)
				{
					instance.getCamera().renderTick(camera.entity(), partialTick);
				}

				instance.getCrosshairRenderer().updateDynamicRaytrace(
					camera,
					event.getModelViewMatrix(),
					event.getProjectionMatrix(),
					partialTick);
			}
		}

		@SubscribeEvent
		public static void onMovementInputUpdate(MovementInputUpdateEvent event)
		{
			ShoulderSurfingImpl.getInstance().getInputHandler().updateMovementInput(event.getInput());
			ShoulderSurfingImpl.getInstance().updatePlayerRotations();
		}

		@SubscribeEvent
		public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event)
		{
			if(VanillaGuiLayers.CROSSHAIR.equals(event.getName()))
			{
				if(!ShoulderSurfingImpl.getInstance().getCrosshairRenderer().doRenderCrosshair())
				{
					event.setCanceled(true);
				}
			}
		}

		@SubscribeEvent
		public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?, ?> event)
		{
			com.ultra.megamod.feature.shouldersurfing.client.CameraEntityRenderer renderer =
				ShoulderSurfingImpl.getInstance().getCameraEntityRenderer();

			if(event.getRenderState() == renderer.getCameraEntityRenderState())
			{
				net.minecraft.world.entity.Entity entity = Minecraft.getInstance().getCameraEntity();

				if(entity != null && renderer.preRenderCameraEntity(entity, event.getPartialTick()))
				{
					event.setCanceled(true);
				}
			}
		}

		@SubscribeEvent
		public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?, ?> event)
		{
			com.ultra.megamod.feature.shouldersurfing.client.CameraEntityRenderer renderer =
				ShoulderSurfingImpl.getInstance().getCameraEntityRenderer();

			if(event.getRenderState() == renderer.getCameraEntityRenderState())
			{
				net.minecraft.world.entity.Entity entity = Minecraft.getInstance().getCameraEntity();

				if(entity != null)
				{
					renderer.postRenderCameraEntity(entity, event.getPartialTick());
				}
			}
		}
	}
}
