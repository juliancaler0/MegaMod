package com.ultra.megamod.reliquary;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import com.ultra.megamod.reliquary.crafting.AlkahestryRecipeRegistry;
import com.ultra.megamod.reliquary.handler.ClientEventHandler;
import com.ultra.megamod.reliquary.handler.CommonEventHandler;
import com.ultra.megamod.reliquary.init.*;
import com.ultra.megamod.reliquary.item.MobCharmRegistry;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.potions.PotionMap;

/**
 * Reliquary port entry point — called from MegaMod / MegaModClient. IDs stay
 * in the "reliquary" namespace so copied assets/data resolve without rewrites.
 */
public final class Reliquary {

	public static final String MOD_ID = "reliquary";
	private static boolean commonInitialized = false;

	private Reliquary() {}

	public static void initCommon(IEventBus modBus, ModContainer container) {
		if (commonInitialized) {
			return;
		}
		commonInitialized = true;
		NeoForgeMod.enableMilkFluid();

		modBus.addListener(Reliquary::setup);
		modBus.addListener(Reliquary::loadComplete);
		modBus.addListener(Config::onFileChange);
		modBus.addListener(ModPayloads::registerPackets);

		ModFluids.registerHandlers(modBus);
		ModItems.registerListeners(modBus);
		ModBlocks.registerListeners(modBus);
		ModEntities.registerListeners(modBus);
		ModEffects.registerListeners(modBus);
		ModSounds.registerListeners(modBus);
		ModDataComponents.register(modBus);

		container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC, "megamod-reliquary-client.toml");
		container.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "megamod-reliquary-common.toml");

		IEventBus eventBus = NeoForge.EVENT_BUS;
		CommonEventHandler.registerEventBusListeners(eventBus);
		eventBus.addListener(MobCharmRegistry::handleAddingFragmentDrops);
		eventBus.addListener(AlkahestryRecipeRegistry::onResourceReload);

		ModCompat.initCompats(modBus);
	}

	public static void initClient(IEventBus modBus, ModContainer container) {
		ClientEventHandler.registerHandlers(container);
	}

	public static void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(ModItems::registerDispenseBehaviors);
		PotionMap.initPotionMap();
		ModItems.registerHandgunMagazines();
		PedestalItems.init();
	}

	public static void loadComplete(FMLLoadCompleteEvent event) {
		MobCharmRegistry.registerDynamicCharmDefinitions();
	}

	public static Identifier getRL(String regName) {
		return Identifier.fromNamespaceAndPath(MOD_ID, regName);
	}

	// ── Admin toggle bridge ──────────────────────────────────────────
	// Reliquary subsystems consult these gates to honor the FeatureToggleManager
	// flags exposed in the admin Toggles tab. Defaults to enabled; if no
	// ServerLevel context is available (early init, client-only paths) we err
	// on the side of "enabled" so the port doesn't silently no-op.

	private static boolean isToggleOn(String featureId) {
		try {
			net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
			if (server == null) {
				return true;
			}
			net.minecraft.server.level.ServerLevel overworld = server.overworld();
			return com.ultra.megamod.feature.toggles.FeatureToggleManager.get(overworld).isEnabled(featureId);
		} catch (Throwable ignored) {
			return true;
		}
	}

	public static boolean isEnabled() { return isToggleOn("reliquary"); }
	public static boolean isHandgunEnabled() { return isEnabled() && isToggleOn("reliquary_handgun"); }
	public static boolean isPedestalsEnabled() { return isEnabled() && isToggleOn("reliquary_pedestals"); }
	public static boolean isAlkahestryEnabled() { return isEnabled() && isToggleOn("reliquary_alkahestry"); }
	public static boolean isApothecaryEnabled() { return isEnabled() && isToggleOn("reliquary_apothecary"); }
	public static boolean isPotionsReplaceAlchemyEnabled() { return isEnabled() && isToggleOn("reliquary_potions_replace_alchemy"); }
	public static boolean isRelicsEnabled() { return isEnabled() && isToggleOn("reliquary_relics"); }
	public static boolean isMobCharmsEnabled() { return isEnabled() && isToggleOn("reliquary_mob_charms"); }
	public static boolean isVoidTearEnabled() { return isEnabled() && isToggleOn("reliquary_void_tear"); }
	public static boolean isFragmentDropsEnabled() { return isEnabled() && isToggleOn("reliquary_fragment_drops"); }
	public static boolean isWitherlessRoseEnabled() { return isEnabled() && isToggleOn("reliquary_witherless_rose"); }
	public static boolean isChestLootEnabled() { return isEnabled() && isToggleOn("reliquary_chest_loot"); }
}
