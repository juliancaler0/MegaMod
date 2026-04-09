package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry for ALL 95+ colony food items across 3 tiers, plus ingredients, breads, and bottles.
 */
public class ColonyFoodRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create((ResourceKey) Registries.CREATIVE_MODE_TAB, "megamod");

    // ==================== Helper Methods ====================

    private static Item.Properties foodProps(int nutrition, float saturation) {
        return new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(nutrition).saturationModifier(saturation).build());
    }

    private static Item.Properties tier1() { return foodProps(5, 0.6f); }
    private static Item.Properties tier2() { return foodProps(7, 1.0f); }
    private static Item.Properties tier3() { return foodProps(9, 1.2f); }

    // ==================== Tier 1 Foods (nutrition=5, saturation=0.6) — 19 items ====================

    public static final DeferredItem<Item> CHEDDAR_CHEESE = ITEMS.registerSimpleItem("cheddar_cheese", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> FETA_CHEESE = ITEMS.registerSimpleItem("feta_cheese", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> COOKED_RICE = ITEMS.registerSimpleItem("cooked_rice", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> TOFU = ITEMS.registerSimpleItem("tofu", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> FLATBREAD = ITEMS.registerSimpleItem("flatbread", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> LEMBAS_SCONE = ITEMS.registerSimpleItem("lembas_scone", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> COOKED_EGG = ITEMS.registerSimpleItem("cooked_egg", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> EGGPLANT_DISH = ITEMS.registerSimpleItem("eggplant_dish", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> BELL_PEPPER_DISH = ITEMS.registerSimpleItem("bell_pepper_dish", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> CABBAGE_DISH = ITEMS.registerSimpleItem("cabbage_dish", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> ONION_SOUP = ITEMS.registerSimpleItem("onion_soup", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> TOMATO_SOUP = ITEMS.registerSimpleItem("tomato_soup", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> PEA_SOUP = ITEMS.registerSimpleItem("pea_soup", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> CORN_ON_THE_COB = ITEMS.registerSimpleItem("corn_on_the_cob", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> ROASTED_GARLIC = ITEMS.registerSimpleItem("roasted_garlic", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> MINT_TEA = ITEMS.registerSimpleItem("mint_tea", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> RICE_BALL = ITEMS.registerSimpleItem("rice_ball", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> SUSHI_ROLL = ITEMS.registerSimpleItem("sushi_roll", ColonyFoodRegistry::tier1);
    public static final DeferredItem<Item> CHICKPEA_HUMMUS = ITEMS.registerSimpleItem("chickpea_hummus", ColonyFoodRegistry::tier1);

    // ==================== Tier 2 Foods (nutrition=7, saturation=1.0) — 24 items ====================

    public static final DeferredItem<Item> MANCHET_BREAD = ITEMS.registerSimpleItem("manchet_bread", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> MUFFIN = ITEMS.registerSimpleItem("muffin", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> POTTAGE = ITEMS.registerSimpleItem("pottage", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> APPLE_PIE = ITEMS.registerSimpleItem("apple_pie", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> CABBAGE_STEW = ITEMS.registerSimpleItem("cabbage_stew", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> PEPPER_STEW = ITEMS.registerSimpleItem("pepper_stew", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> TOMATO_SAUCE_PASTA = ITEMS.registerSimpleItem("tomato_sauce_pasta", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> GARLIC_BREAD = ITEMS.registerSimpleItem("garlic_bread", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> CORN_CHOWDER = ITEMS.registerSimpleItem("corn_chowder", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> STUFFED_PEPPER = ITEMS.registerSimpleItem("stuffed_pepper", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> RATATOUILLE = ITEMS.registerSimpleItem("ratatouille", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> FALAFEL = ITEMS.registerSimpleItem("falafel", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> RICE_PILAF = ITEMS.registerSimpleItem("rice_pilaf", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> SOY_NOODLES = ITEMS.registerSimpleItem("soy_noodles", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> TOFU_STIR_FRY = ITEMS.registerSimpleItem("tofu_stir_fry", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> MINESTRONE = ITEMS.registerSimpleItem("minestrone", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> SQUASH_SOUP = ITEMS.registerSimpleItem("squash_soup", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> POTATO_GRATIN = ITEMS.registerSimpleItem("potato_gratin", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> VEGGIE_WRAP = ITEMS.registerSimpleItem("veggie_wrap", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> NOODLE_BOWL = ITEMS.registerSimpleItem("noodle_bowl", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> FRIED_RICE = ITEMS.registerSimpleItem("fried_rice", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> MUSHROOM_RISOTTO = ITEMS.registerSimpleItem("mushroom_risotto", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> BERRY_CRUMBLE = ITEMS.registerSimpleItem("berry_crumble", ColonyFoodRegistry::tier2);
    public static final DeferredItem<Item> PUMPKIN_PIE_SLICE = ITEMS.registerSimpleItem("pumpkin_pie_slice", ColonyFoodRegistry::tier2);

    // ==================== Tier 3 Foods (nutrition=9, saturation=1.2) �� 18 items ====================

    public static final DeferredItem<Item> HAND_PIE = ITEMS.registerSimpleItem("hand_pie", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> SCHNITZEL = ITEMS.registerSimpleItem("schnitzel", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> STEAK_DINNER = ITEMS.registerSimpleItem("steak_dinner", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> LAMB_CHOP_MEAL = ITEMS.registerSimpleItem("lamb_chop_meal", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> CHICKEN_DINNER = ITEMS.registerSimpleItem("chicken_dinner", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> PORK_ROAST = ITEMS.registerSimpleItem("pork_roast", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> FISH_AND_CHIPS = ITEMS.registerSimpleItem("fish_and_chips", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> FEAST_PLATE = ITEMS.registerSimpleItem("feast_plate", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> SHEPHERDS_PIE = ITEMS.registerSimpleItem("shepherds_pie", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> BEEF_STEW = ITEMS.registerSimpleItem("beef_stew", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> CHICKEN_POT_PIE = ITEMS.registerSimpleItem("chicken_pot_pie", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> BBQ_RIBS = ITEMS.registerSimpleItem("bbq_ribs", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> LOBSTER_TAIL = ITEMS.registerSimpleItem("lobster_tail", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> SEARED_SALMON = ITEMS.registerSimpleItem("seared_salmon", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> STUFFED_TURKEY = ITEMS.registerSimpleItem("stuffed_turkey", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> HONEY_GLAZED_HAM = ITEMS.registerSimpleItem("honey_glazed_ham", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> ROYAL_FEAST = ITEMS.registerSimpleItem("royal_feast", ColonyFoodRegistry::tier3);
    public static final DeferredItem<Item> NETHER_PEPPER_STEAK = ITEMS.registerSimpleItem("nether_pepper_steak", ColonyFoodRegistry::tier3);

    // ==================== Ingredient Items (9) — not edible ====================

    public static final DeferredItem<Item> BREAD_DOUGH = ITEMS.registerSimpleItem("bread_dough", () -> new Item.Properties());
    public static final DeferredItem<Item> COOKIE_DOUGH = ITEMS.registerSimpleItem("cookie_dough", () -> new Item.Properties());
    public static final DeferredItem<Item> CAKE_BATTER = ITEMS.registerSimpleItem("cake_batter", () -> new Item.Properties());
    public static final DeferredItem<Item> RAW_PUMPKIN_PIE = ITEMS.registerSimpleItem("raw_pumpkin_pie", () -> new Item.Properties());
    public static final DeferredItem<Item> BUTTER = ITEMS.registerSimpleItem("butter", () -> new Item.Properties());
    public static final DeferredItem<Item> CORNMEAL = ITEMS.registerSimpleItem("cornmeal", () -> new Item.Properties());
    public static final DeferredItem<Item> CREAM_CHEESE = ITEMS.registerSimpleItem("cream_cheese", () -> new Item.Properties());
    public static final DeferredItem<Item> SOY_SAUCE = ITEMS.registerSimpleItem("soy_sauce", () -> new Item.Properties());
    public static final DeferredItem<Item> RAW_NOODLE = ITEMS.registerSimpleItem("raw_noodle", () -> new Item.Properties());

    // ==================== Breads (4) — edible ====================

    public static final DeferredItem<Item> MILKY_BREAD = ITEMS.registerSimpleItem("milky_bread",
        () -> foodProps(6, 0.8f));
    public static final DeferredItem<Item> SUGARY_BREAD = ITEMS.registerSimpleItem("sugary_bread",
        () -> foodProps(6, 0.8f));
    public static final DeferredItem<Item> GOLDEN_BREAD = ITEMS.registerSimpleItem("golden_bread",
        () -> foodProps(8, 1.2f));
    public static final DeferredItem<Item> CHORUS_BREAD = ITEMS.registerSimpleItem("chorus_bread",
        () -> foodProps(7, 1.0f));

    // ==================== Bottles (4) ====================

    public static final DeferredItem<Item> LARGE_WATER_BOTTLE = ITEMS.registerSimpleItem("large_water_bottle",
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> LARGE_MILK_BOTTLE = ITEMS.registerSimpleItem("large_milk_bottle",
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> LARGE_SOY_MILK_BOTTLE = ITEMS.registerSimpleItem("large_soy_milk_bottle",
        () -> new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> LARGE_EMPTY_BOTTLE = ITEMS.registerSimpleItem("large_empty_bottle",
        () -> new Item.Properties().stacksTo(16));

    // ==================== Creative Tab ====================

    public static final Supplier<CreativeModeTab> COLONY_FOOD_TAB = CREATIVE_MODE_TABS.register("megamod_colony_food_tab",
        () -> CreativeModeTab.builder()
            .title((Component) Component.literal((String) "MegaMod - Colony Food"))
            .icon(() -> new ItemStack((ItemLike) Items.BREAD))
            .displayItems((parameters, output) -> {
                // Tier 1
                output.accept((ItemLike) CHEDDAR_CHEESE.get());
                output.accept((ItemLike) FETA_CHEESE.get());
                output.accept((ItemLike) COOKED_RICE.get());
                output.accept((ItemLike) TOFU.get());
                output.accept((ItemLike) FLATBREAD.get());
                output.accept((ItemLike) LEMBAS_SCONE.get());
                output.accept((ItemLike) COOKED_EGG.get());
                output.accept((ItemLike) EGGPLANT_DISH.get());
                output.accept((ItemLike) BELL_PEPPER_DISH.get());
                output.accept((ItemLike) CABBAGE_DISH.get());
                output.accept((ItemLike) ONION_SOUP.get());
                output.accept((ItemLike) TOMATO_SOUP.get());
                output.accept((ItemLike) PEA_SOUP.get());
                output.accept((ItemLike) CORN_ON_THE_COB.get());
                output.accept((ItemLike) ROASTED_GARLIC.get());
                output.accept((ItemLike) MINT_TEA.get());
                output.accept((ItemLike) RICE_BALL.get());
                output.accept((ItemLike) SUSHI_ROLL.get());
                output.accept((ItemLike) CHICKPEA_HUMMUS.get());
                // Tier 2
                output.accept((ItemLike) MANCHET_BREAD.get());
                output.accept((ItemLike) MUFFIN.get());
                output.accept((ItemLike) POTTAGE.get());
                output.accept((ItemLike) APPLE_PIE.get());
                output.accept((ItemLike) CABBAGE_STEW.get());
                output.accept((ItemLike) PEPPER_STEW.get());
                output.accept((ItemLike) TOMATO_SAUCE_PASTA.get());
                output.accept((ItemLike) GARLIC_BREAD.get());
                output.accept((ItemLike) CORN_CHOWDER.get());
                output.accept((ItemLike) STUFFED_PEPPER.get());
                output.accept((ItemLike) RATATOUILLE.get());
                output.accept((ItemLike) FALAFEL.get());
                output.accept((ItemLike) RICE_PILAF.get());
                output.accept((ItemLike) SOY_NOODLES.get());
                output.accept((ItemLike) TOFU_STIR_FRY.get());
                output.accept((ItemLike) MINESTRONE.get());
                output.accept((ItemLike) SQUASH_SOUP.get());
                output.accept((ItemLike) POTATO_GRATIN.get());
                output.accept((ItemLike) VEGGIE_WRAP.get());
                output.accept((ItemLike) NOODLE_BOWL.get());
                output.accept((ItemLike) FRIED_RICE.get());
                output.accept((ItemLike) MUSHROOM_RISOTTO.get());
                output.accept((ItemLike) BERRY_CRUMBLE.get());
                output.accept((ItemLike) PUMPKIN_PIE_SLICE.get());
                // Tier 3
                output.accept((ItemLike) HAND_PIE.get());
                output.accept((ItemLike) SCHNITZEL.get());
                output.accept((ItemLike) STEAK_DINNER.get());
                output.accept((ItemLike) LAMB_CHOP_MEAL.get());
                output.accept((ItemLike) CHICKEN_DINNER.get());
                output.accept((ItemLike) PORK_ROAST.get());
                output.accept((ItemLike) FISH_AND_CHIPS.get());
                output.accept((ItemLike) FEAST_PLATE.get());
                output.accept((ItemLike) SHEPHERDS_PIE.get());
                output.accept((ItemLike) BEEF_STEW.get());
                output.accept((ItemLike) CHICKEN_POT_PIE.get());
                output.accept((ItemLike) BBQ_RIBS.get());
                output.accept((ItemLike) LOBSTER_TAIL.get());
                output.accept((ItemLike) SEARED_SALMON.get());
                output.accept((ItemLike) STUFFED_TURKEY.get());
                output.accept((ItemLike) HONEY_GLAZED_HAM.get());
                output.accept((ItemLike) ROYAL_FEAST.get());
                output.accept((ItemLike) NETHER_PEPPER_STEAK.get());
                // Ingredients
                output.accept((ItemLike) BREAD_DOUGH.get());
                output.accept((ItemLike) COOKIE_DOUGH.get());
                output.accept((ItemLike) CAKE_BATTER.get());
                output.accept((ItemLike) RAW_PUMPKIN_PIE.get());
                output.accept((ItemLike) BUTTER.get());
                output.accept((ItemLike) CORNMEAL.get());
                output.accept((ItemLike) CREAM_CHEESE.get());
                output.accept((ItemLike) SOY_SAUCE.get());
                output.accept((ItemLike) RAW_NOODLE.get());
                // Breads
                output.accept((ItemLike) MILKY_BREAD.get());
                output.accept((ItemLike) SUGARY_BREAD.get());
                output.accept((ItemLike) GOLDEN_BREAD.get());
                output.accept((ItemLike) CHORUS_BREAD.get());
                // Bottles
                output.accept((ItemLike) LARGE_WATER_BOTTLE.get());
                output.accept((ItemLike) LARGE_MILK_BOTTLE.get());
                output.accept((ItemLike) LARGE_SOY_MILK_BOTTLE.get());
                output.accept((ItemLike) LARGE_EMPTY_BOTTLE.get());
            }).build());

    // ==================== Init ====================

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
