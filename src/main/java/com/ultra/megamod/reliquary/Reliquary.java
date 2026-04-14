package com.ultra.megamod.reliquary;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import com.ultra.megamod.reliquary.client.init.ModParticles;
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
		ModParticles.registerListeners(modBus);
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
}
