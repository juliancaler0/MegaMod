package com.ultra.megamod.reliquary.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import com.ultra.megamod.reliquary.Reliquary;
import com.ultra.megamod.reliquary.common.gui.AlkahestTomeMenu;
import com.ultra.megamod.reliquary.common.gui.MobCharmBeltMenu;
import com.ultra.megamod.reliquary.crafting.*;
import com.ultra.megamod.reliquary.crafting.conditions.*;
import com.ultra.megamod.reliquary.data.ChestLootEnabledCondition;
import com.ultra.megamod.reliquary.data.EntityLootEnabledCondition;
import com.ultra.megamod.reliquary.entity.shot.*;
import com.ultra.megamod.reliquary.item.*;
import com.ultra.megamod.reliquary.item.PotionItem;
import com.ultra.megamod.reliquary.item.TippedArrowItem;
import com.ultra.megamod.reliquary.item.util.HarvestRodCache;
import com.ultra.megamod.reliquary.item.util.fluid.FluidHandlerEmperorChalice;
import com.ultra.megamod.reliquary.item.util.fluid.FluidHandlerHeroMedallion;
import com.ultra.megamod.reliquary.item.util.fluid.FluidHandlerInfernalChalice;
import com.ultra.megamod.reliquary.reference.Colors;
import com.ultra.megamod.reliquary.reference.Config;
import com.ultra.megamod.reliquary.util.LegacyCapabilityAdapters;
import com.ultra.megamod.reliquary.util.RegistryHelper;

import java.util.function.Supplier;

public class ModItems {
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Reliquary.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reliquary.MOD_ID);
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Reliquary.MOD_ID);
	private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Reliquary.MOD_ID);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Reliquary.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Reliquary.MOD_ID);
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reliquary.MOD_ID);
	// Port note (1.21.11): WitchHat switched to the EQUIPPABLE data component,
	// so the custom armor-material registry is no longer needed.

	public static final Supplier<AlkahestryTomeItem> ALKAHESTRY_TOME = ITEMS.register("alkahestry_tome", AlkahestryTomeItem::new);
	public static final Supplier<MercyCrossItem> MERCY_CROSS = ITEMS.register("mercy_cross", MercyCrossItem::new);
	public static final Supplier<AngelheartVialItem> ANGELHEART_VIAL = ITEMS.register("angelheart_vial", AngelheartVialItem::new);
	public static final Supplier<AngelicFeatherItem> ANGELIC_FEATHER = ITEMS.register("angelic_feather", AngelicFeatherItem::new);
	public static final Supplier<AphroditePotionItem> APHRODITE_POTION = ITEMS.register("aphrodite_potion", AphroditePotionItem::new);
	public static final Supplier<PotionEssenceItem> POTION_ESSENCE = ITEMS.register("potion_essence", PotionEssenceItem::new);
	public static final Supplier<DestructionCatalystItem> DESTRUCTION_CATALYST = ITEMS.register("destruction_catalyst", DestructionCatalystItem::new);
	public static final Supplier<EmperorChaliceItem> EMPEROR_CHALICE = ITEMS.register("emperor_chalice", EmperorChaliceItem::new);
	public static final Supplier<EnderStaffItem> ENDER_STAFF = ITEMS.register("ender_staff", EnderStaffItem::new);
	public static final Supplier<FertilePotionItem> FERTILE_POTION = ITEMS.register("fertile_potion", FertilePotionItem::new);
	public static final Supplier<FortuneCoinItem> FORTUNE_COIN = ITEMS.register("fortune_coin", FortuneCoinItem::new);
	public static final Supplier<GlacialStaffItem> GLACIAL_STAFF = ITEMS.register("glacial_staff", GlacialStaffItem::new);
	public static final Supplier<GlowingWaterItem> GLOWING_WATER = ITEMS.register("glowing_water", GlowingWaterItem::new);
	public static final Supplier<HolyHandGrenadeItem> HOLY_HAND_GRENADE = ITEMS.register("holy_hand_grenade", HolyHandGrenadeItem::new);
	public static final Supplier<HandgunItem> HANDGUN = ITEMS.register("handgun", HandgunItem::new);
	public static final Supplier<ItemBase> GRIP_ASSEMBLY = ITEMS.register("grip_assembly", () -> new ItemBase(new Item.Properties().stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<ItemBase> BARREL_ASSEMBLY = ITEMS.register("barrel_assembly", () -> new ItemBase(new Item.Properties().stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<ItemBase> HAMMER_ASSEMBLY = ITEMS.register("hammer_assembly", () -> new ItemBase(new Item.Properties().stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<HarvestRodItem> HARVEST_ROD = ITEMS.register("harvest_rod", HarvestRodItem::new);
	public static final Supplier<MobCharmFragmentItem> MOB_CHARM_FRAGMENT = ITEMS.register("mob_charm_fragment", MobCharmFragmentItem::new);
	public static final Supplier<HeroMedallionItem> HERO_MEDALLION = ITEMS.register("hero_medallion", HeroMedallionItem::new);
	public static final Supplier<IceMagusRodItem> ICE_MAGUS_ROD = ITEMS.register("ice_magus_rod", IceMagusRodItem::new);
	public static final Supplier<InfernalChaliceItem> INFERNAL_CHALICE = ITEMS.register("infernal_chalice", InfernalChaliceItem::new);
	public static final Supplier<InfernalClawsItem> INFERNAL_CLAWS = ITEMS.register("infernal_claws", InfernalClawsItem::new);
	public static final Supplier<InfernalTearItem> INFERNAL_TEAR = ITEMS.register("infernal_tear", InfernalTearItem::new);
	public static final Supplier<KrakenShellItem> KRAKEN_SHELL = ITEMS.register("kraken_shell", KrakenShellItem::new);
	public static final Supplier<MidasTouchstoneItem> MIDAS_TOUCHSTONE = ITEMS.register("midas_touchstone", MidasTouchstoneItem::new);
	public static final Supplier<PhoenixDownItem> PHOENIX_DOWN = ITEMS.register("phoenix_down", PhoenixDownItem::new);
	public static final Supplier<PyromancerStaffItem> PYROMANCER_STAFF = ITEMS.register("pyromancer_staff", PyromancerStaffItem::new);
	public static final Supplier<RendingGaleItem> RENDING_GALE = ITEMS.register("rending_gale", RendingGaleItem::new);
	public static final Supplier<RodOfLyssaItem> ROD_OF_LYSSA = ITEMS.register("rod_of_lyssa", RodOfLyssaItem::new);
	public static final Supplier<SojournerStaffItem> SOJOURNER_STAFF = ITEMS.register("sojourner_staff", SojournerStaffItem::new);
	public static final Supplier<TippedArrowItem> TIPPED_ARROW = ITEMS.register("tipped_arrow", TippedArrowItem::new);
	public static final Supplier<VoidTearItem> VOID_TEAR = ITEMS.register("void_tear", VoidTearItem::new);
	public static final Supplier<WitchHatItem> WITCH_HAT = ITEMS.register("witch_hat", WitchHatItem::new);
	public static final Supplier<WitherlessRoseItem> WITHERLESS_ROSE = ITEMS.register("witherless_rose", WitherlessRoseItem::new);
	public static final Supplier<ItemBase> EMPTY_POTION_VIAL = ITEMS.register("empty_potion_vial", () -> new ItemBase(Config.COMMON.disable.disablePotions));
	public static final Supplier<PotionItemBase> POTION = ITEMS.register("potion", PotionItem::new);
	public static final Supplier<PotionItemBase> SPLASH_POTION = ITEMS.register("splash_potion", ThrownPotionItem::new);
	public static final Supplier<PotionItemBase> LINGERING_POTION = ITEMS.register("lingering_potion", ThrownPotionItem::new);
	public static final Supplier<MobCharmBeltItem> MOB_CHARM_BELT = ITEMS.register("mob_charm_belt", MobCharmBeltItem::new);
	public static final Supplier<MobCharmItem> MOB_CHARM = ITEMS.register("mob_charm", MobCharmItem::new);
	public static final Supplier<MagazineItem> EMPTY_MAGAZINE = ITEMS.register("magazines/empty_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.DARKEST, 16)));
	public static final Supplier<MagazineItem> NEUTRAL_MAGAZINE = ITEMS.register("magazines/neutral_magazine", () ->
			new MagazineItem(true, Integer.parseInt(Colors.NEUTRAL_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> EXORCISM_MAGAZINE = ITEMS.register("magazines/exorcism_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.EXORCISM_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> BLAZE_MAGAZINE = ITEMS.register("magazines/blaze_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.BLAZE_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> ENDER_MAGAZINE = ITEMS.register("magazines/ender_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.ENDER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> CONCUSSIVE_MAGAZINE = ITEMS.register("magazines/concussive_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.CONCUSSIVE_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> BUSTER_MAGAZINE = ITEMS.register("magazines/buster_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.BUSTER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> SEEKER_MAGAZINE = ITEMS.register("magazines/seeker_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.SEEKER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> SAND_MAGAZINE = ITEMS.register("magazines/sand_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.SAND_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> STORM_MAGAZINE = ITEMS.register("magazines/storm_magazine", () ->
			new MagazineItem(false, Integer.parseInt(Colors.STORM_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> EMPTY_BULLET = ITEMS.register("bullets/empty_bullet", () ->
			new BulletItem(false, false, Integer.parseInt(Colors.DARKEST, 16)));
	public static final Supplier<BulletItem> NEUTRAL_BULLET = ITEMS.register("bullets/neutral_bullet", () ->
			new BulletItem(false, true, Integer.parseInt(Colors.NEUTRAL_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> EXORCISM_BULLET = ITEMS.register("bullets/exorcism_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.EXORCISM_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> BLAZE_BULLET = ITEMS.register("bullets/blaze_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.BLAZE_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> ENDER_BULLET = ITEMS.register("bullets/ender_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.ENDER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> CONCUSSIVE_BULLET = ITEMS.register("bullets/concussive_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.CONCUSSIVE_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> BUSTER_BULLET = ITEMS.register("bullets/buster_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.BUSTER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> SEEKER_BULLET = ITEMS.register("bullets/seeker_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.SEEKER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> SAND_BULLET = ITEMS.register("bullets/sand_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.SAND_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> STORM_BULLET = ITEMS.register("bullets/storm_bullet", () ->
			new BulletItem(true, false, Integer.parseInt(Colors.STORM_SHOT_COLOR, 16)));
	public static final Supplier<ItemBase> ZOMBIE_HEART = ITEMS.register("zombie_heart", MobDropItem::new);
	public static final Supplier<ItemBase> SQUID_BEAK = ITEMS.register("squid_beak", MobDropItem::new);
	public static final Supplier<ItemBase> RIB_BONE = ITEMS.register("rib_bone", MobDropItem::new);
	public static final Supplier<ItemBase> CATALYZING_GLAND = ITEMS.register("catalyzing_gland", MobDropItem::new);
	public static final Supplier<ItemBase> CHELICERAE = ITEMS.register("chelicerae", MobDropItem::new);
	public static final Supplier<ItemBase> SLIME_PEARL = ITEMS.register("slime_pearl", MobDropItem::new);
	public static final Supplier<ItemBase> KRAKEN_SHELL_FRAGMENT = ITEMS.register("kraken_shell_fragment", () -> new ItemBase());
	public static final Supplier<ItemBase> BAT_WING = ITEMS.register("bat_wing", MobDropItem::new);
	public static final Supplier<ItemBase> WITHERED_RIB = ITEMS.register("withered_rib", MobDropItem::new);
	public static final Supplier<ItemBase> MOLTEN_CORE = ITEMS.register("molten_core", MobDropItem::new);
	public static final Supplier<ItemBase> EYE_OF_THE_STORM = ITEMS.register("eye_of_the_storm", MobDropItem::new);
	public static final Supplier<ItemBase> FERTILE_ESSENCE = ITEMS.register("fertile_essence", () -> new ItemBase());
	public static final Supplier<ItemBase> FROZEN_CORE = ITEMS.register("frozen_core", MobDropItem::new);
	public static final Supplier<ItemBase> NEBULOUS_HEART = ITEMS.register("nebulous_heart", MobDropItem::new);
	public static final Supplier<ItemBase> INFERNAL_CLAW = ITEMS.register("infernal_claw", () -> new ItemBase());
	public static final Supplier<ItemBase> GUARDIAN_SPIKE = ITEMS.register("guardian_spike", MobDropItem::new);
	public static final Supplier<ItemBase> CRIMSON_CLOTH = ITEMS.register("crimson_cloth", () -> new ItemBase());
	public static final Supplier<LanternOfParanoiaItem> LANTERN_OF_PARANOIA = ITEMS.register("lantern_of_paranoia", LanternOfParanoiaItem::new);
	public static final Supplier<MagicbaneItem> MAGICBANE = ITEMS.register("magicbane", MagicbaneItem::new);
	public static final Supplier<SalamanderEyeItem> SALAMANDER_EYE = ITEMS.register("salamander_eye", SalamanderEyeItem::new);
	public static final Supplier<SerpentStaffItem> SERPENT_STAFF = ITEMS.register("serpent_staff", SerpentStaffItem::new);
	public static final Supplier<ShearsOfWinterItem> SHEARS_OF_WINTER = ITEMS.register("shears_of_winter", ShearsOfWinterItem::new);
	public static final Supplier<TwilightCloakItem> TWILIGHT_CLOAK = ITEMS.register("twilight_cloak", TwilightCloakItem::new);
	public static final Supplier<ItemBase> GLOWING_BREAD = ITEMS.register("glowing_bread", () ->
			// Port note (1.21.11): FoodProperties.Builder#fast() was removed — food speed is now
			// controlled through Consumable settings rather than a builder flag.
			new ItemBase(new Item.Properties().rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(20).saturationModifier(1F).build())));
	public static final Supplier<Item> XP_BUCKET = ITEMS.register("xp_bucket", () -> new BucketItem(ModFluids.XP_STILL.get(), new Item.Properties().stacksTo(1)));

	public static final Supplier<MenuType<AlkahestTomeMenu>> ALKAHEST_TOME_MENU_TYPE = MENU_TYPES.register("alkahest_tome",
			() -> IMenuTypeExtension.create((windowId, inv, data) -> AlkahestTomeMenu.fromBuffer(windowId)));

	public static final Supplier<MenuType<MobCharmBeltMenu>> MOB_CHAR_BELT_MENU_TYPE = MENU_TYPES.register("mob_char_belt",
			() -> IMenuTypeExtension.create(MobCharmBeltMenu::fromBuffer));


	public static final Supplier<MapCodec<AlkahestryEnabledCondition>> ALKAHESTRY_ENABLED_CONDITION = CONDITION_CODECS.register("alkahestry_enabled", () -> AlkahestryEnabledCondition.CODEC);
	public static final Supplier<MapCodec<MobDropsCraftableCondition>> MOB_DROPS_CRAFTABLE_CONDITION = CONDITION_CODECS.register("mob_drops_craftable", () -> MobDropsCraftableCondition.CODEC);
	public static final Supplier<MapCodec<HandgunEnabledCondition>> HANDGUN_ENABLED_CONDITION = CONDITION_CODECS.register("handgun_enabled", () -> HandgunEnabledCondition.CODEC);
	public static final Supplier<MapCodec<PotionsEnabledCondition>> POTIONS_ENABLED_CONDITION = CONDITION_CODECS.register("potions_enabled", () -> PotionsEnabledCondition.CODEC);
	public static final Supplier<MapCodec<PassivePedestalEnabledCondition>> PASSIVE_PEDESTAL_ENABLED_CONDITION = CONDITION_CODECS.register("passive_pedestal_enabled", () -> PassivePedestalEnabledCondition.CODEC);
	public static final Supplier<MapCodec<PedestalEnabledCondition>> PEDESTAL_ENABLED_CONDITION = CONDITION_CODECS.register("pedestal_enabled", () -> PedestalEnabledCondition.CODEC);
	public static final Supplier<MapCodec<SpawnEggEnabledCondition>> SPAWN_EGG_ENABLED_CONDITION = CONDITION_CODECS.register("spawn_egg_enabled", () -> SpawnEggEnabledCondition.CODEC);
	public static final Supplier<MapCodec<SpawnEggEnabledCondition>> CHARM_ENABLED_CONDITION = CONDITION_CODECS.register("charm_enabled", () -> CharmEnabledCondition.CODEC);

	public static final Supplier<RecipeSerializer<?>> MOB_CHARM_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("mob_charm", MobCharmRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> FRAGMENT_TO_SPAWN_EGG_SERIALIZER = RECIPE_SERIALIZERS.register("fragment_to_spawn_egg", FragmentToSpawnEggRecipe.Serializer::new);
	// Port note (1.21.11): SimpleCraftingRecipeSerializer folded into
	// CustomRecipe.Serializer; constructor takes a (CraftingBookCategory ->
	// recipe) factory instead of a plain no-arg supplier.
	public static final Supplier<RecipeSerializer<MobCharmRepairRecipe>> MOB_CHARM_REPAIR_SERIALIZER = RECIPE_SERIALIZERS.register("mob_charm_repair", () -> new CustomRecipe.Serializer<>(MobCharmRepairRecipe::new));
	public static final Supplier<RecipeSerializer<?>> ALKAHESTRY_CHARGING_SERIALIZER = RECIPE_SERIALIZERS.register("alkahestry_charging", AlkahestryChargingRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> ALKAHESTRY_CRAFTING_SERIALIZER = RECIPE_SERIALIZERS.register("alkahestry_crafting", AlkahestryCraftingRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> ALKAHESTRY_DRAIN_SERIALIZER = RECIPE_SERIALIZERS.register("alkahestry_drain", AlkahestryDrainRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> POTION_EFFECTS_SERIALIZER = RECIPE_SERIALIZERS.register("potion_effects", PotionEffectsRecipe.Serializer::new);
	public static final Supplier<LootItemConditionType> CHEST_LOOT_ENABLED_CONDITION = LOOT_CONDITION_TYPES.register("chest_loot_enabled", () -> new LootItemConditionType(ChestLootEnabledCondition.CODEC));
	public static final Supplier<LootItemConditionType> ENTITY_LOOT_ENABLED_CONDITION = LOOT_CONDITION_TYPES.register("entity_loot_enabled", () -> new LootItemConditionType(EntityLootEnabledCondition.CODEC));
	// Port note (1.21.11): the old ReliquaryLootModifierProvider (datagen) was stripped during
	// the 1.21 prune and has not been re-ported. Global loot modifiers in 1.21 can alternatively
	// be declared as static resource files at
	//   data/reliquary/neoforge/loot_modifiers/global_loot_modifiers.json
	// referencing codec-serialised modifier JSONs under
	//   data/reliquary/loot_modifiers/*.json
	// — that path is preferred over re-porting the provider class. The LOOT_MODIFIERS
	// DeferredRegister above is still registered so future modifier codecs can be attached
	// without touching callers.

	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Reliquary.MOD_ID);
	public static final ItemCapability<HarvestRodCache, @Nullable Void> HARVEST_ROD_CACHE_CAPABILITY = ItemCapability.createVoid(Reliquary.getRL("harvest_rod_cache"), HarvestRodCache.class);
	public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
			CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.MERCY_CROSS.get()))
					.title(Component.translatable("itemGroup.reliquary"))
					.displayItems((featureFlags, output) -> {
								ITEMS.getEntries().stream().filter(i -> i.get() instanceof ICreativeTabItemGenerator)
										.forEach(i -> ((ICreativeTabItemGenerator) i.get()).addCreativeTabItems(output::accept));
								ModBlocks.ITEMS.getEntries().stream().filter(i -> i.get() instanceof ICreativeTabItemGenerator)
										.forEach(i -> ((ICreativeTabItemGenerator) i.get()).addCreativeTabItems(output::accept));
								output.accept(new ItemStack(XP_BUCKET.get()));
							}
					).build());

	public static void registerDispenseBehaviors() {
		if (Boolean.FALSE.equals(Config.COMMON.disable.disablePotions.get())) {
			DispenserBlock.registerProjectileBehavior(ModItems.SPLASH_POTION.get());
			DispenserBlock.registerProjectileBehavior(ModItems.LINGERING_POTION.get());
			DispenserBlock.registerProjectileBehavior(ModItems.APHRODITE_POTION.get());
			DispenserBlock.registerProjectileBehavior(ModItems.FERTILE_POTION.get());
			DispenserBlock.registerProjectileBehavior(ModItems.TIPPED_ARROW.get());
		}
		DispenserBlock.registerProjectileBehavior(ModItems.GLOWING_WATER.get());
		DispenserBlock.registerProjectileBehavior(ModItems.HOLY_HAND_GRENADE.get());
	}

	public static void registerHandgunMagazines() {
		HandgunItem handgun = HANDGUN.get();
		handgun.registerMagazine(RegistryHelper.getRegistryName(NEUTRAL_MAGAZINE.get()), NeutralShot::new, NEUTRAL_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(EXORCISM_MAGAZINE.get()), ExorcismShot::new, EXORCISM_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(BLAZE_MAGAZINE.get()), BlazeShot::new, BLAZE_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(ENDER_MAGAZINE.get()), EnderShot::new, ENDER_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(CONCUSSIVE_MAGAZINE.get()), ConcussiveShot::new, CONCUSSIVE_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(BUSTER_MAGAZINE.get()), BusterShot::new, BUSTER_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(SEEKER_MAGAZINE.get()), SeekerShot::new, SEEKER_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(SAND_MAGAZINE.get()), SandShot::new, SAND_BULLET);
		handgun.registerMagazine(RegistryHelper.getRegistryName(STORM_MAGAZINE.get()), StormShot::new, STORM_BULLET);
	}

	public static void registerListeners(IEventBus modBus) {
		ITEMS.register(modBus);
		MENU_TYPES.register(modBus);
		RECIPE_SERIALIZERS.register(modBus);
		CONDITION_CODECS.register(modBus);
		LOOT_CONDITION_TYPES.register(modBus);
		LOOT_MODIFIERS.register(modBus);
		CREATIVE_MODE_TABS.register(modBus);
		ATTACHMENT_TYPES.register(modBus);
		modBus.addListener(ModItems::registerCapabilities);
		NeoForge.EVENT_BUS.addListener(ModItems::onResourceReload);

		// TODO: once the client-layer agent lands ModItemsClient (or equivalent in
		// com.ultra.megamod.reliquary.client.init), re-wire the client-side registrations here
		// under a Dist.CLIENT guard. The parallel client agent already owns
		// client.init.ItemModels / ModParticles / ModItemColors, so the remaining piece is a
		// thin dispatcher class that invokes those registrations at the right lifecycle point.
	}

	private static void onResourceReload(AddServerReloadListenersEvent event) {
		// Port note (1.21.11): AddServerReloadListenersEvent#addListener(PreparableReloadListener)
		// was replaced with AddServerReloadListenersEvent#addListener(Identifier, PreparableReloadListener).
		// We attach a listener whose apply step clears the definitions cache,
		// keeping behavior parity with the old listener that only cleared state.
		event.addListener(
				Identifier.fromNamespaceAndPath(Reliquary.MOD_ID, "mob_charm_definitions"),
				(sharedState, prepareExecutor, preparationBarrier, applyExecutor) -> {
					java.util.function.Supplier<Void> prepare = () -> null;
					return java.util.concurrent.CompletableFuture
							.supplyAsync(prepare, prepareExecutor)
							.thenCompose(data -> preparationBarrier.wait(data))
							.thenRunAsync(MobCharmRecipe.REGISTERED_RECIPES::clear, applyExecutor);
				});
	}

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(HARVEST_ROD_CACHE_CAPABILITY, (itemStack, context) -> new HarvestRodCache(), HARVEST_ROD.get());

		// Fluid item capabilities: wrap the legacy IFluidHandlerItem implementations into
		// ResourceHandler<FluidResource>, propagating container changes back through the
		// supplied ItemAccess.
		event.registerItem(
				Capabilities.Fluid.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asFluidResourceHandlerItem(new FluidHandlerEmperorChalice(stack), access),
				EMPEROR_CHALICE.get());
		event.registerItem(
				Capabilities.Fluid.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asFluidResourceHandlerItem(new FluidHandlerInfernalChalice(ModDataComponents.FLUID_CONTENTS, stack), access),
				INFERNAL_CHALICE.get());
		event.registerItem(
				Capabilities.Fluid.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asFluidResourceHandlerItem(new FluidHandlerHeroMedallion(stack), access),
				HERO_MEDALLION.get());

		// Item-storage capabilities for container items: the chargeable items hold their
		// payloads in a component-backed IItemHandlerModifiable that we surface as a
		// ResourceHandler<ItemResource> so hoppers, void tears, and similar automation
		// can interact with them.
		event.registerItem(
				Capabilities.Item.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asItemResourceHandler(VOID_TEAR.get().createHandler(stack)),
				VOID_TEAR.get());
		event.registerItem(
				Capabilities.Item.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asItemResourceHandler(HARVEST_ROD.get().createHandler(stack)),
				HARVEST_ROD.get());
		event.registerItem(
				Capabilities.Item.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asItemResourceHandler(ENDER_STAFF.get().createHandler(stack)),
				ENDER_STAFF.get());
		event.registerItem(
				Capabilities.Item.ITEM,
				(stack, access) -> LegacyCapabilityAdapters.asItemResourceHandler(RENDING_GALE.get().createHandler(stack)),
				RENDING_GALE.get());
	}
}
