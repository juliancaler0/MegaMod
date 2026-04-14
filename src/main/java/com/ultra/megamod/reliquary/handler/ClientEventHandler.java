package com.ultra.megamod.reliquary.handler;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.api.client.HUDPosition;
import com.ultra.megamod.reliquary.client.gui.components.Box;
import com.ultra.megamod.reliquary.client.gui.components.Component;
import com.ultra.megamod.reliquary.client.gui.components.ItemStackPane;
import com.ultra.megamod.reliquary.client.gui.components.TextPane;
import com.ultra.megamod.reliquary.client.gui.hud.ChargeableItemInfoPane;
import com.ultra.megamod.reliquary.client.gui.hud.ChargePane;
import com.ultra.megamod.reliquary.client.gui.hud.CharmPane;
import com.ultra.megamod.reliquary.client.gui.hud.DynamicChargePane;
import com.ultra.megamod.reliquary.client.gui.hud.HUDRenderrer;
import com.ultra.megamod.reliquary.client.gui.hud.HandgunPane;
import com.ultra.megamod.reliquary.client.gui.hud.HeroMedallionPane;
import com.ultra.megamod.reliquary.client.init.ModBlockColors;
import com.ultra.megamod.reliquary.client.init.ModParticles;
import com.ultra.megamod.reliquary.client.model.MobCharmBeltModel;
import com.ultra.megamod.reliquary.client.model.WitchHatModel;
import com.ultra.megamod.reliquary.client.registry.PedestalClientRegistry;
import com.ultra.megamod.reliquary.client.render.ApothecaryMortarRenderer;
import com.ultra.megamod.reliquary.client.render.PassivePedestalRenderer;
import com.ultra.megamod.reliquary.client.render.PedestalFishHookRenderer;
import com.ultra.megamod.reliquary.client.render.PedestalRenderer;
import com.ultra.megamod.reliquary.client.render.ShotRenderer;
import com.ultra.megamod.reliquary.client.render.TippedArrowRenderer;
import com.ultra.megamod.reliquary.init.ModBlocks;
import com.ultra.megamod.reliquary.init.ModEntities;
import com.ultra.megamod.reliquary.init.ModFluids;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.item.AlkahestryTomeItem;
import com.ultra.megamod.reliquary.item.DestructionCatalystItem;
import com.ultra.megamod.reliquary.item.EnderStaffItem;
import com.ultra.megamod.reliquary.item.FortuneCoinToggler;
import com.ultra.megamod.reliquary.item.GlacialStaffItem;
import com.ultra.megamod.reliquary.item.HarvestRodItem;
import com.ultra.megamod.reliquary.item.IceMagusRodItem;
import com.ultra.megamod.reliquary.item.InfernalChaliceItem;
import com.ultra.megamod.reliquary.item.MidasTouchstoneItem;
import com.ultra.megamod.reliquary.item.PyromancerStaffItem;
import com.ultra.megamod.reliquary.item.RendingGaleItem;
import com.ultra.megamod.reliquary.item.RodOfLyssaItem;
import com.ultra.megamod.reliquary.item.VoidTearItem;
import com.ultra.megamod.reliquary.item.util.IScrollableItem;
import com.ultra.megamod.reliquary.network.ScrolledItemPayload;
import com.ultra.megamod.reliquary.reference.Colors;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.InventoryHelper;
import com.ultra.megamod.reliquary.util.potions.PotionHelper;

import java.util.List;
import java.util.Map;

/**
 * Full client-side wiring for the Reliquary port: keybinds, HUD overlay,
 * mouse-scroll dispatch, entity / block-entity renderer registration, color
 * handlers, particle providers, and the witch-hat / xp-fluid client
 * extensions.
 *
 * <p>Some callback points the 1.21.x ref source hooked have moved to data-
 * driven JSON (select-item-model for the VOID_TEAR / HANDGUN / LYSSA ROD /
 * INFERNAL_TEAR empty-or-loaded swap) and are no longer wired from Java.
 * The handgun BOW_AND_ARROW arm-pose hook from {@code RenderLivingEvent.Pre}
 * is also deferred: {@code LivingEntityRenderer} now extracts arm poses from
 * a render-state during {@code extractRenderState}, not at submit time, and a
 * faithful port requires a LivingEntityRenderer mixin rather than a standard
 * NeoForge event.
 */
public final class ClientEventHandler {
	private ClientEventHandler() {
	}

	private static final int KEY_UNKNOWN = -1;
	public static final KeyMapping.Category RELIQUARY_KEY_CATEGORY = KeyMapping.Category.register(Reliquary.getRL("reliquary"));
	public static final KeyMapping FORTUNE_COIN_TOGGLE_KEYBIND = new KeyMapping(
			"keybind.reliquary.fortune_coin",
			KeyConflictContext.UNIVERSAL,
			InputConstants.Type.KEYSYM,
			KEY_UNKNOWN,
			RELIQUARY_KEY_CATEGORY);
	private static final String VOID_TEAR_MODE_TRANSLATION = "item." + Reliquary.MOD_ID + ".void_tear.mode.";
	public static final ModelLayerLocation WITCH_HAT_LAYER = new ModelLayerLocation(Reliquary.getRL("witch_hat"), "main");
	public static final ModelLayerLocation MOB_CHARM_BELT_LAYER = new ModelLayerLocation(Reliquary.getRL("mob_charm_belt"), "main");

	public static void registerHandlers(ModContainer container) {
		IEventBus modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
		if (modBus == null) {
			return;
		}

		// Mod bus: particle + renderer + color + overlay + key wiring
		ModParticles.registerListeners(modBus);

		modBus.addListener(ClientEventHandler::clientSetup);
		modBus.addListener(ClientEventHandler::registerKeyMappings);
		modBus.addListener(ClientEventHandler::loadComplete);
		modBus.addListener(ModParticles.ProviderHandler::registerProviders);
		modBus.addListener(ClientEventHandler::registerEntityRenderers);
		modBus.addListener(ClientEventHandler::registerLayer);
		modBus.addListener(ModBlockColors::registerBlockColors);
		// NB: item tint registration moved from RegisterColorHandlersEvent.Item
		// (removed in 1.21.11) to data-driven ItemTintSource entries under
		// assets/reliquary/items/<name>.json — see ModItemColors javadoc.
		modBus.addListener(ClientEventHandler::registerOverlay);
		modBus.addListener(ClientEventHandler::registerClientExtensions);

		// Game bus: input handling (FortuneCoin keybind is hooked in loadComplete
		// so the keybind singleton is already registered)
		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(ClientEventHandler::onMouseScrolled);
	}

	private static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(WITCH_HAT_LAYER, WitchHatModel::createBodyLayer);
		event.registerLayerDefinition(MOB_CHARM_BELT_LAYER, MobCharmBeltModel::createBodyLayer);
	}

	private static final List<Tuple<Component, HUDPosition>> hudComponents = Lists.newArrayList();

	private static void registerOverlay(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, Reliquary.getRL("reliquary_hud"), (guiGraphics, deltaTracker) -> {
			if (hudComponents.isEmpty()) {
				initHUDComponents();
			}
			renderHUDComponents(guiGraphics);
		});
	}

	private static void onMouseScrolled(InputEvent.MouseScrollingEvent evt) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null || !isShiftDown()) {
			return;
		}
		LocalPlayer player = mc.player;
		if (player == null) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		double scrollDelta = evt.getScrollDeltaY();
		if (stack.getItem() instanceof IScrollableItem scrollableItem && scrollableItem.onMouseScrolled(stack, player, scrollDelta) == InteractionResult.PASS) {
			ClientPacketDistributor.sendToServer(new ScrolledItemPayload(scrollDelta));
			evt.setCanceled(true);
		}
	}

	private static boolean isShiftDown() {
		try {
			com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();
			return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
					|| InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
		} catch (Throwable ignored) {
			return false;
		}
	}

	private static void renderHUDComponents(net.minecraft.client.gui.GuiGraphics guiGraphics) {
		for (Tuple<Component, HUDPosition> component : hudComponents) {
			HUDRenderrer.render(guiGraphics, component.getA(), component.getB());
		}
	}

	private static void initHUDComponents() {
		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.ALKAHESTRY_TOME.get(), Config.CLIENT.hudPositions.alkahestryTome.get(), new ItemStack(Items.REDSTONE), AlkahestryTomeItem::getCharge),
				Config.CLIENT.hudPositions.alkahestryTome.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.DESTRUCTION_CATALYST.get(), Config.CLIENT.hudPositions.destructionCatalyst.get(), new ItemStack(Items.GUNPOWDER), DestructionCatalystItem::getGunpowder),
				Config.CLIENT.hudPositions.destructionCatalyst.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.MIDAS_TOUCHSTONE.get(), Config.CLIENT.hudPositions.midasTouchstone.get(), new ItemStack(Items.GLOWSTONE_DUST), MidasTouchstoneItem::getGlowstoneCharge),
				Config.CLIENT.hudPositions.midasTouchstone.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.INFERNAL_CHALICE.get(), Config.CLIENT.hudPositions.infernalChalice.get(), new ItemStack(Items.LAVA_BUCKET), InfernalChaliceItem::getFluidBucketAmount, Colors.get(Colors.RED)),
				Config.CLIENT.hudPositions.infernalChalice.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.ICE_MAGUS_ROD.get(), Config.CLIENT.hudPositions.iceMagusRod.get(), new ItemStack(Items.SNOWBALL), IceMagusRodItem::getSnowballs),
				Config.CLIENT.hudPositions.iceMagusRod.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.GLACIAL_STAFF.get(), Config.CLIENT.hudPositions.glacialStaff.get(), new ItemStack(Items.SNOWBALL), GlacialStaffItem::getSnowballs),
				Config.CLIENT.hudPositions.glacialStaff.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.ENDER_STAFF.get(), Config.CLIENT.hudPositions.enderStaff.get(), is -> ModItems.ENDER_STAFF.get().getMode(is).getSerializedName(),
				Map.of(
						EnderStaffItem.Mode.CAST.getSerializedName(), new ChargePane(ModItems.ENDER_STAFF.get(), new ItemStack(Items.ENDER_PEARL), is -> ModItems.ENDER_STAFF.get().getPearlCount(is)),
						EnderStaffItem.Mode.NODE_WARP.getSerializedName(), new ChargePane(ModItems.ENDER_STAFF.get(), new ItemStack(ModBlocks.WRAITH_NODE.get()), is -> ModItems.ENDER_STAFF.get().getPearlCount(is)),
						EnderStaffItem.Mode.LONG_CAST.getSerializedName(), new ChargePane(ModItems.ENDER_STAFF.get(), new ItemStack(Items.ENDER_EYE), is -> ModItems.ENDER_STAFF.get().getPearlCount(is))
				)), Config.CLIENT.hudPositions.enderStaff.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.PYROMANCER_STAFF.get(), Config.CLIENT.hudPositions.pyromancerStaff.get(), is -> ModItems.PYROMANCER_STAFF.get().getMode(is).getSerializedName(),
				Map.of(
						PyromancerStaffItem.Mode.BLAZE.getSerializedName(), new ChargePane(ModItems.PYROMANCER_STAFF.get(), new ItemStack(Items.BLAZE_POWDER), staff -> ModItems.PYROMANCER_STAFF.get().getBlazePowderCount(staff)),
						PyromancerStaffItem.Mode.FIRE_CHARGE.getSerializedName(), new ChargePane(ModItems.PYROMANCER_STAFF.get(), new ItemStack(Items.FIRE_CHARGE), staff -> ModItems.PYROMANCER_STAFF.get().getFireChargeCount(staff)),
						PyromancerStaffItem.Mode.ERUPTION.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane("ERUPT"), new ChargePane(ModItems.PYROMANCER_STAFF.get(), new ItemStack(Items.BLAZE_POWDER), staff -> ModItems.PYROMANCER_STAFF.get().getBlazePowderCount(staff))),
						PyromancerStaffItem.Mode.FLINT_AND_STEEL.getSerializedName(), new ItemStackPane(Items.FLINT_AND_STEEL)
				)), Config.CLIENT.hudPositions.pyromancerStaff.get()));

		ChargePane rendingGaleFeatherPane = new ChargePane(ModItems.RENDING_GALE.get(), new ItemStack(Items.FEATHER), is -> {
			LocalPlayer player = Minecraft.getInstance().player;
			return player == null ? 0 : ModItems.RENDING_GALE.get().getFeatherCount(is);
		});
		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.RENDING_GALE.get(), Config.CLIENT.hudPositions.rendingGale.get(), is -> ModItems.RENDING_GALE.get().getMode(is).getSerializedName(),
				Map.of(
						RendingGaleItem.Mode.PUSH.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane("PUSH"), rendingGaleFeatherPane),
						RendingGaleItem.Mode.PULL.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane("PULL"), rendingGaleFeatherPane),
						RendingGaleItem.Mode.BOLT.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane("BOLT"), rendingGaleFeatherPane),
						RendingGaleItem.Mode.FLIGHT.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane("FLIGHT"), rendingGaleFeatherPane)
				)), Config.CLIENT.hudPositions.rendingGale.get()));

		Component contentsPane = new DynamicChargePane(ModItems.VOID_TEAR.get(),
				VoidTearItem::getTearContents, is -> VoidTearItem.getTearContents(is).getCount());
		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.VOID_TEAR.get(), Config.CLIENT.hudPositions.voidTear.get(), is -> ModItems.VOID_TEAR.get().getMode(is).getSerializedName(),
				Map.of(
						VoidTearItem.Mode.FULL_INVENTORY.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane(Language.getInstance().getOrDefault(VOID_TEAR_MODE_TRANSLATION + VoidTearItem.Mode.FULL_INVENTORY.getSerializedName().toLowerCase())), contentsPane),
						VoidTearItem.Mode.NO_REFILL.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane(Language.getInstance().getOrDefault(VOID_TEAR_MODE_TRANSLATION + VoidTearItem.Mode.NO_REFILL.getSerializedName().toLowerCase())), contentsPane),
						VoidTearItem.Mode.ONE_STACK.getSerializedName(), Box.createVertical(Box.Alignment.RIGHT, new TextPane(Language.getInstance().getOrDefault(VOID_TEAR_MODE_TRANSLATION + VoidTearItem.Mode.ONE_STACK.getSerializedName().toLowerCase())), contentsPane)
				)) {
			@Override
			public boolean shouldRender() {
				LocalPlayer player = Minecraft.getInstance().player;
				return player != null && !ModItems.VOID_TEAR.get().isEmpty(InventoryHelper.getCorrectItemFromEitherHand(player, ModItems.VOID_TEAR.get()));
			}
		}, Config.CLIENT.hudPositions.voidTear.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.HARVEST_ROD.get(), Config.CLIENT.hudPositions.harvestRod.get(), is -> ModItems.HARVEST_ROD.get().getMode(is).getSerializedName(),
				Map.of(
						HarvestRodItem.Mode.BONE_MEAL.getSerializedName(), new ChargePane(ModItems.HARVEST_ROD.get(), new ItemStack(Items.BONE_MEAL), is -> ModItems.HARVEST_ROD.get().getBoneMealCount(is)),
						HarvestRodItem.Mode.HOE.getSerializedName(), new ItemStackPane(Items.WOODEN_HOE),
						ChargeableItemInfoPane.DYNAMIC_PANE, new DynamicChargePane(ModItems.HARVEST_ROD.get(), is -> ModItems.HARVEST_ROD.get().getCurrentPlantable(is), is -> ModItems.HARVEST_ROD.get().getPlantableQuantity(is, ModItems.HARVEST_ROD.get().getCurrentPlantableSlot(is)))
				)), Config.CLIENT.hudPositions.harvestRod.get()));

		hudComponents.add(new Tuple<>(new ChargeableItemInfoPane(ModItems.SOJOURNER_STAFF.get(), Config.CLIENT.hudPositions.sojournerStaff.get(), is -> ChargeableItemInfoPane.DYNAMIC_PANE,
				Map.of(
						ChargeableItemInfoPane.DYNAMIC_PANE, new DynamicChargePane(ModItems.SOJOURNER_STAFF.get(), stack -> {
							ItemStack currentTorch = ModItems.SOJOURNER_STAFF.get().getCurrentTorch(stack);
							return currentTorch.isEmpty() ? new ItemStack(Items.TORCH) : currentTorch;
						}, ModItems.SOJOURNER_STAFF.get()::getTorchCount)
				)), Config.CLIENT.hudPositions.sojournerStaff.get()));

		hudComponents.add(new Tuple<>(new HeroMedallionPane(), Config.CLIENT.hudPositions.heroMedallion.get()));

		hudComponents.add(new Tuple<>(Box.createVertical(Box.Alignment.RIGHT, new HandgunPane(InteractionHand.OFF_HAND), new HandgunPane(InteractionHand.MAIN_HAND)), Config.CLIENT.hudPositions.handgun.get()));

		hudComponents.add(new Tuple<>(new CharmPane(), Config.CLIENT.hudPositions.mobCharm.get()));
	}

	private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlocks.APOTHECARY_MORTAR_TILE_TYPE.get(), ApothecaryMortarRenderer::new);
		event.registerBlockEntityRenderer(ModBlocks.PEDESTAL_TILE_TYPE.get(), PedestalRenderer::new);
		event.registerBlockEntityRenderer(ModBlocks.PASSIVE_PEDESTAL_TILE_TYPE.get(), PassivePedestalRenderer::new);

		event.registerEntityRenderer(ModEntities.LYSSA_HOOK.get(), FishingHookRenderer::new);
		event.registerEntityRenderer(ModEntities.BLAZE_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.BUSTER_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.CONCUSSIVE_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.ENDER_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.EXORCISM_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.NEUTRAL_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.SEEKER_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.SAND_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.STORM_SHOT.get(), ShotRenderer::new);
		event.registerEntityRenderer(ModEntities.TIPPED_ARROW.get(), TippedArrowRenderer::new);
		event.registerEntityRenderer(ModEntities.GLOWING_WATER.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.APHRODITE_POTION.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.FERTILE_POTION.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.HOLY_HAND_GRENADE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.KRAKEN_SLIME.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.SPECIAL_SNOWBALL.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.ENDER_STAFF_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.THROWN_POTION.get(), ThrownItemRenderer::new);
	}

	private static void clientSetup(FMLClientSetupEvent event) {
		// In 1.21.4+ item multi-state visuals are data-driven via select-item
		// models in assets/reliquary/items/<name>.json — the old
		// ItemProperties.register() predicates (empty void-tear, handgun
		// loaded, lyssa cast, etc.) are no longer needed here.
	}

	private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(FORTUNE_COIN_TOGGLE_KEYBIND);
	}

	@SuppressWarnings("unused") // PotionHelper retained so potion-attached stacks can drive the select-model predicate
	private static boolean isPotionAttached(ItemStack stack) {
		return PotionHelper.hasPotionContents(stack);
	}

	private static void loadComplete(FMLLoadCompleteEvent event) {
		event.enqueueWork(() -> {
			PedestalClientRegistry.registerItemRenderer(FishingRodItem.class, PedestalFishHookRenderer::new);
			PedestalClientRegistry.registerItemRenderer(RodOfLyssaItem.class, PedestalFishHookRenderer::new);
			NeoForge.EVENT_BUS.addListener(FortuneCoinToggler::handleKeyInputEvent);
		});
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(new IClientItemExtensions() {
			private WitchHatModel hatModel = null;

			// 1.21.11 signature: (ItemStack, EquipmentClientInfo.LayerType, Model) -> Model.
			// The LivingEntity / EquipmentSlot parameters were dropped — LayerType
			// carries the slot info now.
			@Override
			public net.minecraft.client.model.Model getHumanoidArmorModel(ItemStack itemStack, net.minecraft.client.resources.model.EquipmentClientInfo.LayerType layerType, net.minecraft.client.model.Model original) {
				if (hatModel == null) {
					EntityModelSet entityModels = Minecraft.getInstance().getEntityModels();
					hatModel = new WitchHatModel(entityModels.bakeLayer(ClientEventHandler.WITCH_HAT_LAYER));
				}
				return hatModel;
			}
		}, ModItems.WITCH_HAT.get());

		event.registerFluidType(new IClientFluidTypeExtensions() {
			private static final Identifier XP_STILL_TEXTURE = Identifier.fromNamespaceAndPath(Reliquary.MOD_ID, "block/xp_still");
			private static final Identifier XP_FLOWING_TEXTURE = Identifier.fromNamespaceAndPath(Reliquary.MOD_ID, "block/xp_flowing");

			@Override
			public Identifier getStillTexture() {
				return XP_STILL_TEXTURE;
			}

			@Override
			public Identifier getFlowingTexture() {
				return XP_FLOWING_TEXTURE;
			}
		}, ModFluids.EXPERIENCE_FLUID_TYPE.get());
	}
}
