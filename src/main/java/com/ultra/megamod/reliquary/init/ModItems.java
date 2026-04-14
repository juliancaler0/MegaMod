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
import com.ultra.megamod.reliquary.data.InjectLootModifier;
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
	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reliquary.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Reliquary.MOD_ID);
	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, Reliquary.MOD_ID);
	private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, Reliquary.MOD_ID);
	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Reliquary.MOD_ID);
	public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Reliquary.MOD_ID);
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Reliquary.MOD_ID);
	// Port note (1.21.11): WitchHat switched to the EQUIPPABLE data component,
	// so the custom armor-material registry is no longer needed.

	// EMPTY_POTION_VIAL is registered first because several potion/vial items call
	// ModItems.EMPTY_POTION_VIAL.get() inside their constructor (for Properties#craftRemainder).
	// In 1.21.11 NeoForge resolves DeferredRegister entries in declaration order during
	// RegisterEvent, so any downstream factory that dereferences this holder must come later.
	public static final Supplier<ItemBase> EMPTY_POTION_VIAL = ITEMS.registerItem("empty_potion_vial", props -> new ItemBase(props, Config.COMMON.disable.disablePotions));

	public static final Supplier<AlkahestryTomeItem> ALKAHESTRY_TOME = ITEMS.registerItem("alkahestry_tome", AlkahestryTomeItem::new);
	public static final Supplier<MercyCrossItem> MERCY_CROSS = ITEMS.registerItem("mercy_cross", MercyCrossItem::new);
	public static final Supplier<AngelheartVialItem> ANGELHEART_VIAL = ITEMS.registerItem("angelheart_vial", AngelheartVialItem::new);
	public static final Supplier<AngelicFeatherItem> ANGELIC_FEATHER = ITEMS.registerItem("angelic_feather", AngelicFeatherItem::new);
	public static final Supplier<AphroditePotionItem> APHRODITE_POTION = ITEMS.registerItem("aphrodite_potion", AphroditePotionItem::new);
	public static final Supplier<PotionEssenceItem> POTION_ESSENCE = ITEMS.registerItem("potion_essence", PotionEssenceItem::new);
	public static final Supplier<DestructionCatalystItem> DESTRUCTION_CATALYST = ITEMS.registerItem("destruction_catalyst", DestructionCatalystItem::new);
	public static final Supplier<EmperorChaliceItem> EMPEROR_CHALICE = ITEMS.registerItem("emperor_chalice", EmperorChaliceItem::new);
	public static final Supplier<EnderStaffItem> ENDER_STAFF = ITEMS.registerItem("ender_staff", EnderStaffItem::new);
	public static final Supplier<FertilePotionItem> FERTILE_POTION = ITEMS.registerItem("fertile_potion", FertilePotionItem::new);
	public static final Supplier<FortuneCoinItem> FORTUNE_COIN = ITEMS.registerItem("fortune_coin", FortuneCoinItem::new);
	public static final Supplier<GlacialStaffItem> GLACIAL_STAFF = ITEMS.registerItem("glacial_staff", GlacialStaffItem::new);
	public static final Supplier<GlowingWaterItem> GLOWING_WATER = ITEMS.registerItem("glowing_water", GlowingWaterItem::new);
	public static final Supplier<HolyHandGrenadeItem> HOLY_HAND_GRENADE = ITEMS.registerItem("holy_hand_grenade", HolyHandGrenadeItem::new);
	public static final Supplier<HandgunItem> HANDGUN = ITEMS.registerItem("handgun", HandgunItem::new);
	public static final Supplier<ItemBase> GRIP_ASSEMBLY = ITEMS.registerItem("grip_assembly", props -> new ItemBase(props.stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<ItemBase> BARREL_ASSEMBLY = ITEMS.registerItem("barrel_assembly", props -> new ItemBase(props.stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<ItemBase> HAMMER_ASSEMBLY = ITEMS.registerItem("hammer_assembly", props -> new ItemBase(props.stacksTo(4), Config.COMMON.disable.disableHandgun));
	public static final Supplier<HarvestRodItem> HARVEST_ROD = ITEMS.registerItem("harvest_rod", HarvestRodItem::new);
	public static final Supplier<MobCharmFragmentItem> MOB_CHARM_FRAGMENT = ITEMS.registerItem("mob_charm_fragment", MobCharmFragmentItem::new);
	public static final Supplier<HeroMedallionItem> HERO_MEDALLION = ITEMS.registerItem("hero_medallion", HeroMedallionItem::new);
	public static final Supplier<IceMagusRodItem> ICE_MAGUS_ROD = ITEMS.registerItem("ice_magus_rod", IceMagusRodItem::new);
	public static final Supplier<InfernalChaliceItem> INFERNAL_CHALICE = ITEMS.registerItem("infernal_chalice", InfernalChaliceItem::new);
	public static final Supplier<InfernalClawsItem> INFERNAL_CLAWS = ITEMS.registerItem("infernal_claws", InfernalClawsItem::new);
	public static final Supplier<InfernalTearItem> INFERNAL_TEAR = ITEMS.registerItem("infernal_tear", InfernalTearItem::new);
	public static final Supplier<KrakenShellItem> KRAKEN_SHELL = ITEMS.registerItem("kraken_shell", KrakenShellItem::new);
	public static final Supplier<MidasTouchstoneItem> MIDAS_TOUCHSTONE = ITEMS.registerItem("midas_touchstone", MidasTouchstoneItem::new);
	public static final Supplier<PhoenixDownItem> PHOENIX_DOWN = ITEMS.registerItem("phoenix_down", PhoenixDownItem::new);
	public static final Supplier<PyromancerStaffItem> PYROMANCER_STAFF = ITEMS.registerItem("pyromancer_staff", PyromancerStaffItem::new);
	public static final Supplier<RendingGaleItem> RENDING_GALE = ITEMS.registerItem("rending_gale", RendingGaleItem::new);
	public static final Supplier<RodOfLyssaItem> ROD_OF_LYSSA = ITEMS.registerItem("rod_of_lyssa", RodOfLyssaItem::new);
	public static final Supplier<SojournerStaffItem> SOJOURNER_STAFF = ITEMS.registerItem("sojourner_staff", SojournerStaffItem::new);
	public static final Supplier<TippedArrowItem> TIPPED_ARROW = ITEMS.registerItem("tipped_arrow", TippedArrowItem::new);
	public static final Supplier<VoidTearItem> VOID_TEAR = ITEMS.registerItem("void_tear", VoidTearItem::new);
	public static final Supplier<WitchHatItem> WITCH_HAT = ITEMS.registerItem("witch_hat", WitchHatItem::new);
	public static final Supplier<WitherlessRoseItem> WITHERLESS_ROSE = ITEMS.registerItem("witherless_rose", WitherlessRoseItem::new);
	public static final Supplier<PotionItemBase> POTION = ITEMS.registerItem("potion", PotionItem::new);
	public static final Supplier<PotionItemBase> SPLASH_POTION = ITEMS.registerItem("splash_potion", ThrownPotionItem::new);
	public static final Supplier<PotionItemBase> LINGERING_POTION = ITEMS.registerItem("lingering_potion", ThrownPotionItem::new);
	public static final Supplier<MobCharmBeltItem> MOB_CHARM_BELT = ITEMS.registerItem("mob_charm_belt", MobCharmBeltItem::new);
	public static final Supplier<MobCharmItem> MOB_CHARM = ITEMS.registerItem("mob_charm", MobCharmItem::new);
	public static final Supplier<MagazineItem> EMPTY_MAGAZINE = ITEMS.registerItem("magazines/empty_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.DARKEST, 16)));
	public static final Supplier<MagazineItem> NEUTRAL_MAGAZINE = ITEMS.registerItem("magazines/neutral_magazine", props ->
			new MagazineItem(props, true, Integer.parseInt(Colors.NEUTRAL_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> EXORCISM_MAGAZINE = ITEMS.registerItem("magazines/exorcism_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.EXORCISM_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> BLAZE_MAGAZINE = ITEMS.registerItem("magazines/blaze_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.BLAZE_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> ENDER_MAGAZINE = ITEMS.registerItem("magazines/ender_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.ENDER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> CONCUSSIVE_MAGAZINE = ITEMS.registerItem("magazines/concussive_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.CONCUSSIVE_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> BUSTER_MAGAZINE = ITEMS.registerItem("magazines/buster_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.BUSTER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> SEEKER_MAGAZINE = ITEMS.registerItem("magazines/seeker_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.SEEKER_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> SAND_MAGAZINE = ITEMS.registerItem("magazines/sand_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.SAND_SHOT_COLOR, 16)));
	public static final Supplier<MagazineItem> STORM_MAGAZINE = ITEMS.registerItem("magazines/storm_magazine", props ->
			new MagazineItem(props, false, Integer.parseInt(Colors.STORM_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> EMPTY_BULLET = ITEMS.registerItem("bullets/empty_bullet", props ->
			new BulletItem(props, false, false, Integer.parseInt(Colors.DARKEST, 16)));
	public static final Supplier<BulletItem> NEUTRAL_BULLET = ITEMS.registerItem("bullets/neutral_bullet", props ->
			new BulletItem(props, false, true, Integer.parseInt(Colors.NEUTRAL_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> EXORCISM_BULLET = ITEMS.registerItem("bullets/exorcism_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.EXORCISM_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> BLAZE_BULLET = ITEMS.registerItem("bullets/blaze_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.BLAZE_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> ENDER_BULLET = ITEMS.registerItem("bullets/ender_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.ENDER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> CONCUSSIVE_BULLET = ITEMS.registerItem("bullets/concussive_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.CONCUSSIVE_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> BUSTER_BULLET = ITEMS.registerItem("bullets/buster_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.BUSTER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> SEEKER_BULLET = ITEMS.registerItem("bullets/seeker_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.SEEKER_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> SAND_BULLET = ITEMS.registerItem("bullets/sand_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.SAND_SHOT_COLOR, 16)));
	public static final Supplier<BulletItem> STORM_BULLET = ITEMS.registerItem("bullets/storm_bullet", props ->
			new BulletItem(props, true, false, Integer.parseInt(Colors.STORM_SHOT_COLOR, 16)));
	public static final Supplier<MobDropItem> ZOMBIE_HEART = ITEMS.registerItem("zombie_heart", MobDropItem::new);
	public static final Supplier<MobDropItem> SQUID_BEAK = ITEMS.registerItem("squid_beak", MobDropItem::new);
	public static final Supplier<MobDropItem> RIB_BONE = ITEMS.registerItem("rib_bone", MobDropItem::new);
	public static final Supplier<MobDropItem> CATALYZING_GLAND = ITEMS.registerItem("catalyzing_gland", MobDropItem::new);
	public static final Supplier<MobDropItem> CHELICERAE = ITEMS.registerItem("chelicerae", MobDropItem::new);
	public static final Supplier<MobDropItem> SLIME_PEARL = ITEMS.registerItem("slime_pearl", MobDropItem::new);
	public static final Supplier<ItemBase> KRAKEN_SHELL_FRAGMENT = ITEMS.registerItem("kraken_shell_fragment", ItemBase::new);
	public static final Supplier<MobDropItem> BAT_WING = ITEMS.registerItem("bat_wing", MobDropItem::new);
	public static final Supplier<MobDropItem> WITHERED_RIB = ITEMS.registerItem("withered_rib", MobDropItem::new);
	public static final Supplier<MobDropItem> MOLTEN_CORE = ITEMS.registerItem("molten_core", MobDropItem::new);
	public static final Supplier<MobDropItem> EYE_OF_THE_STORM = ITEMS.registerItem("eye_of_the_storm", MobDropItem::new);
	public static final Supplier<ItemBase> FERTILE_ESSENCE = ITEMS.registerItem("fertile_essence", ItemBase::new);
	public static final Supplier<MobDropItem> FROZEN_CORE = ITEMS.registerItem("frozen_core", MobDropItem::new);
	public static final Supplier<MobDropItem> NEBULOUS_HEART = ITEMS.registerItem("nebulous_heart", MobDropItem::new);
	public static final Supplier<ItemBase> INFERNAL_CLAW = ITEMS.registerItem("infernal_claw", ItemBase::new);
	public static final Supplier<MobDropItem> GUARDIAN_SPIKE = ITEMS.registerItem("guardian_spike", MobDropItem::new);
	public static final Supplier<ItemBase> CRIMSON_CLOTH = ITEMS.registerItem("crimson_cloth", ItemBase::new);
	public static final Supplier<LanternOfParanoiaItem> LANTERN_OF_PARANOIA = ITEMS.registerItem("lantern_of_paranoia", LanternOfParanoiaItem::new);
	public static final Supplier<MagicbaneItem> MAGICBANE = ITEMS.registerItem("magicbane", MagicbaneItem::new);
	public static final Supplier<SalamanderEyeItem> SALAMANDER_EYE = ITEMS.registerItem("salamander_eye", SalamanderEyeItem::new);
	public static final Supplier<SerpentStaffItem> SERPENT_STAFF = ITEMS.registerItem("serpent_staff", SerpentStaffItem::new);
	public static final Supplier<ShearsOfWinterItem> SHEARS_OF_WINTER = ITEMS.registerItem("shears_of_winter", ShearsOfWinterItem::new);
	public static final Supplier<TwilightCloakItem> TWILIGHT_CLOAK = ITEMS.registerItem("twilight_cloak", TwilightCloakItem::new);
	public static final Supplier<ItemBase> GLOWING_BREAD = ITEMS.registerItem("glowing_bread", props ->
			// Port note (1.21.11): FoodProperties.Builder#fast() was removed — food speed is now
			// controlled through Consumable settings rather than a builder flag.
			new ItemBase(props.rarity(Rarity.RARE).food(new FoodProperties.Builder().nutrition(20).saturationModifier(1F).build())));
	public static final Supplier<Item> XP_BUCKET = ITEMS.registerItem("xp_bucket", props -> new BucketItem(ModFluids.XP_STILL.get(), props.stacksTo(1)));

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

	// Port note (1.21.11): Reliquary's original datagen provider
	// (ReliquaryLootModifierProvider) declared this modifier as a static inner
	// class. In the port it has been promoted to a top-level class at
	// com.ultra.megamod.reliquary.data.InjectLootModifier and driven entirely
	// from static JSON under data/reliquary/loot_modifier/entities/*.json —
	// re-ported from the reference generated resources. The vanilla mob
	// drops (zombie heart, rib bone, etc.) come from these modifiers merging
	// reliquary:inject/entities/<mob> into the mob's vanilla loot roll.
	public static final Supplier<MapCodec<InjectLootModifier>> INJECT_LOOT = LOOT_MODIFIERS.register("inject_loot", () -> InjectLootModifier.CODEC);

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
