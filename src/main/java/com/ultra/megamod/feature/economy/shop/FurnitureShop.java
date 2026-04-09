package com.ultra.megamod.feature.economy.shop;

import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Static furniture catalog — all furniture items available for purchase.
 * Furniture is buy-only (no recipes, no sell back).
 */
public class FurnitureShop {

    private FurnitureShop() {}

    private static final List<ShopItem> CATALOG = new ArrayList<>();
    private static String currentCategory = "All";
    private static final java.util.Map<Integer, String> ITEM_CATEGORIES = new java.util.LinkedHashMap<>();
    private static final List<String> CATEGORY_ORDER = new ArrayList<>();

    static {
        // Office
        setCategory("Office");
        add("office_board_small", "Office Board (Small)", 50);
        add("office_board_large", "Office Board (Large)", 75);
        add("office_chair", "Office Chair", 60);
        add("office_ceo_chair", "CEO Chair", 150);
        add("office_table", "Office Table", 80);
        add("office_ceo_desk", "CEO Desk", 200);
        add("office_computer", "Office Computer", 120);
        add("office_cupboard", "Office Cupboard", 90);
        add("office_bookshelf", "Office Bookshelf", 70);
        add("office_bookshelf_tall", "Office Bookshelf (Tall)", 100);
        add("office_filing_cabinet", "Filing Cabinet", 80);
        add("office_file_rack", "File Rack", 60);
        add("office_lamp", "Office Lamp", 40);
        add("office_potted_plant", "Office Plant", 30);
        add("office_printer", "Office Printer", 100);
        add("office_projector", "Projector", 150);
        add("office_projector_screen", "Projector Screen", 120);
        add("office_rubbish_bin", "Rubbish Bin", 20);
        add("office_sofa", "Office Sofa", 100);
        add("office_sofa_large", "Office Sofa (Large)", 150);
        add("office_conference_table", "Conference Table", 180);

        setCategory("Coffee Shop");
        // Coffee Shop
        add("coffee_blender", "Blender", 60);
        add("coffee_board_1", "Coffee Board 1", 40);
        add("coffee_board_2", "Coffee Board 2", 40);
        add("coffee_bread_showcase", "Bread Showcase", 80);
        add("coffee_breadcoffee_1", "Bread & Coffee 1", 50);
        add("coffee_breadcoffee_2", "Bread & Coffee 2", 50);
        add("coffee_cashier_table", "Cashier Table", 100);
        add("coffee_chair", "Coffee Chair", 50);
        add("coffee_coffee_machine", "Coffee Machine", 120);
        add("coffee_counter_table", "Counter Table", 80);
        add("coffee_glass_hanger", "Glass Hanger", 40);
        add("coffee_hanging_lamp", "Hanging Lamp", 45);
        add("coffee_picture_wallpaper", "Picture Wallpaper", 30);
        add("coffee_plant_pot", "Plant Pot", 25);
        add("coffee_shelf_1", "Coffee Shelf 1", 50);
        add("coffee_shelf_2", "Coffee Shelf 2", 50);
        add("coffee_shop_sign", "Shop Sign", 60);
        add("coffee_sign", "Coffee Sign", 35);
        add("coffee_sofa_1", "Coffee Sofa 1", 90);
        add("coffee_sofa_2", "Coffee Sofa 2", 90);
        add("coffee_table_1", "Coffee Table 1", 70);
        add("coffee_table_2", "Coffee Table 2", 70);

        setCategory("Classic");
        // Classic
        add("classic_candle", "Classic Candle", 20);
        add("classic_ceiling_fan", "Ceiling Fan", 100);
        add("classic_chair", "Classic Chair", 60);
        add("classic_cleaning_set", "Cleaning Set", 30);
        add("classic_curtain", "Curtain", 40);
        add("classic_curtain_red", "Curtain (Red)", 40);
        add("classic_door", "Classic Door", 50);
        add("classic_flower", "Classic Flower", 20);
        add("classic_golden_tree", "Golden Tree", 300);
        add("classic_harp", "Harp", 200);
        add("classic_hat_hanger", "Hat Hanger", 35);
        add("classic_jar", "Classic Jar", 25);
        add("classic_long_table", "Long Table", 120);
        add("classic_painting", "Classic Painting", 80);
        add("classic_phone", "Classic Phone", 60);
        add("classic_showcase_corner", "Showcase Corner", 90);
        add("classic_table", "Classic Table", 70);
        add("classic_table_lamp", "Table Lamp", 45);
        add("classic_wall_lamp", "Wall Lamp", 40);
        add("classic_wall_lamp_double", "Wall Lamp (Double)", 60);

        setCategory("Vintage");
        // Vintage
        add("vintage_bed", "Vintage Bed", 150);
        add("vintage_big_cupboard", "Big Cupboard", 130);
        add("vintage_book_shelf", "Vintage Bookshelf", 80);
        add("vintage_carpet", "Vintage Carpet", 40);
        add("vintage_carpet_alt", "Vintage Carpet (Alt)", 40);
        add("vintage_chair", "Vintage Chair", 60);
        add("vintage_clock", "Vintage Clock", 100);
        add("vintage_cupboard", "Vintage Cupboard", 90);
        add("vintage_desk_lamp", "Vintage Desk Lamp", 50);
        add("vintage_fireplace", "Fireplace", 200);
        add("vintage_leather_sofa", "Leather Sofa", 180);
        add("vintage_mirror", "Vintage Mirror", 70);
        add("vintage_nightstand", "Nightstand", 50);
        add("vintage_painting", "Vintage Painting", 80);
        add("vintage_piano", "Piano", 300);
        add("vintage_radio", "Vintage Radio", 60);
        add("vintage_showcase", "Vintage Showcase", 100);
        add("vintage_standing_lamp", "Standing Lamp", 55);
        add("vintage_table", "Vintage Table", 70);
        add("vintage_tabletop_lamp", "Tabletop Lamp", 45);

        setCategory("Market");
        // Market
        add("market_barrel", "Market Barrel", 30);
        add("market_barrelberry", "Berry Barrel", 40);
        add("market_barrelsword", "Sword Barrel", 50);
        add("market_boardsign", "Board Sign", 35);
        add("market_campfire_1", "Campfire 1", 45);
        add("market_campfire_2", "Campfire 2", 45);
        add("market_cargo_1", "Cargo 1", 40);
        add("market_cargo_2", "Cargo 2", 40);
        add("market_cart_1", "Market Cart 1", 80);
        add("market_cart_2", "Market Cart 2", 80);
        add("market_chair", "Market Chair", 35);
        add("market_crate", "Market Crate", 25);
        add("market_earthenware", "Earthenware", 50);
        add("market_fishtub", "Fish Tub", 60);
        add("market_marketstall_1", "Market Stall 1", 100);
        add("market_marketstall_2", "Market Stall 2", 100);
        add("market_marketstall_3", "Market Stall 3", 100);
        add("market_marketstall_4", "Market Stall 4", 100);
        add("market_pole", "Market Pole", 20);
        add("market_shark", "Shark Display", 150);
        add("market_shelf_1", "Market Shelf 1", 50);
        add("market_shelf_2", "Market Shelf 2", 50);
        add("market_table_1", "Market Table 1", 60);
        add("market_table_2", "Market Table 2", 60);
        add("market_waterwell", "Water Well", 120);

        setCategory("Market V2");
        // Market 2
        add("market2_barrel_1", "Barrel 1", 30);
        add("market2_barrel_2", "Barrel 2", 30);
        add("market2_board_1", "Board 1", 30);
        add("market2_boardsign_1", "Board Sign 1", 35);
        add("market2_box_1", "Box 1", 25);
        add("market2_cargo_1", "Cargo 1", 40);
        add("market2_cargo_2", "Cargo 2", 40);
        add("market2_chair_1", "Chair 1", 35);
        add("market2_chair_2", "Chair 2", 35);
        add("market2_crate_1", "Crate 1", 25);
        add("market2_crate_2", "Crate 2", 25);
        add("market2_crate_3", "Crate 3", 25);
        add("market2_crate_4", "Crate 4", 25);
        add("market2_marketstall_1", "Market Stall 1", 100);
        add("market2_marketstall_2", "Market Stall 2", 100);
        add("market2_marketstall_3", "Market Stall 3", 100);
        add("market2_marketstall_4", "Market Stall 4", 100);
        add("market2_shelf_1", "Shelf 1", 50);
        add("market2_table_1", "Table 1", 60);
        add("market2_table_2", "Table 2", 60);

        setCategory("Dungeon");
        // Dungeon Decorations
        add("dungeon_cage", "Dungeon Cage", 60);
        add("dungeon_cage_with_bone", "Cage with Bone", 70);
        add("dungeon_chair_bone", "Bone Chair", 50);
        add("dungeon_chair_rest", "Rest Chair", 50);
        add("dungeon_flag_bone", "Bone Flag", 40);
        add("dungeon_goldbar", "Gold Bar", 80);
        add("dungeon_goldbar_coin", "Gold Bar & Coin", 90);
        add("dungeon_goldbars", "Gold Bars", 100);
        add("dungeon_hang_flag", "Hanging Flag", 35);
        add("dungeon_heads", "Dungeon Heads", 60);
        add("dungeon_skeleton", "Skeleton Display", 70);
        add("dungeon_skeleton_head", "Skeleton Head", 40);
        add("dungeon_skeleton_sleep", "Sleeping Skeleton", 70);
        add("dungeon_sword_bone", "Sword & Bone", 55);
        add("dungeon_table_decor", "Dungeon Table Decor", 50);
        add("dungeon_table_long", "Dungeon Long Table", 80);
        add("dungeon_torch_decor", "Torch Decor", 30);
        add("dungeon_vase", "Dungeon Vase", 35);
        add("dungeon_weaponstand", "Weapon Stand", 90);
        add("dungeon_wood_barrel", "Wood Barrel", 30);
        add("dungeon_wood_box", "Wood Box", 25);
        add("dungeon_wood_chair", "Wood Chair", 40);

        setCategory("Casino");
        // Casino Decoration V1
        add("casino_barrier", "Casino Barrier", 40);
        add("casino_bush", "Casino Bush", 30);
        add("casino_card_and_token", "Casino Card & Token", 25);
        add("casino_chair_bar_brown", "Casino Bar Chair (Brown)", 60);
        add("casino_chair_bar_red", "Casino Bar Chair (Red)", 60);
        add("casino_chair_brown", "Casino Chair (Brown)", 50);
        add("casino_chips", "Casino Chips", 20);
        add("casino_game_bigwin", "Big Win Machine", 200);
        add("casino_game_slot", "Slot Machine Decor", 150);
        add("casino_monitor", "Casino Monitor", 80);
        add("casino_pot_tree", "Casino Pot Tree", 35);
        add("casino_sofa_red", "Casino Sofa (Red)", 80);
        add("casino_sofa_red_no_rest", "Casino Sofa No Rest", 70);
        add("casino_sofa_red_single", "Casino Single Sofa", 60);
        add("casino_table_billiards", "Billiards Table", 150);
        add("casino_table_billiards_stick_stand", "Billiard Stick Stand", 40);
        add("casino_table_blackjack", "Blackjack Table Decor", 120);
        add("casino_table_blank", "Casino Table (Blank)", 80);
        add("casino_table_craps", "Craps Table", 130);
        add("casino_table_roulette", "Roulette Table", 140);
        add("casino_table_wood", "Casino Wood Table", 60);
        add("casino_vending_machine", "Vending Machine", 100);

        setCategory("Casino V2");
        // Casino Decoration V2
        add("casino2_ashtray", "Casino Ashtray", 15);
        add("casino2_baccarat_machine", "Baccarat Machine", 180);
        add("casino2_buffalo_slot_machine", "Buffalo Slot Machine", 200);
        add("casino2_chair_red", "Casino Chair (Red)", 50);
        add("casino2_chair_yellow", "Casino Chair (Yellow)", 50);
        add("casino2_chip_set", "Casino Chip Set", 30);
        add("casino2_doggie_cash", "Doggie Cash Machine", 160);
        add("casino2_gambling_game_machine", "Gambling Game Machine", 180);
        add("casino2_lady_led_sign", "Lady LED Sign", 90);
        add("casino2_led_sign", "LED Sign", 70);
        add("casino2_madonna_gambling_machine", "Madonna Gambling Machine", 190);
        add("casino2_poker_sign", "Poker Sign", 50);
        add("casino2_red_armrest_chair", "Red Armrest Chair", 70);
        add("casino2_roulette", "Casino Roulette Decor", 140);
        add("casino2_stack_card", "Stack of Cards", 20);
        add("casino2_table", "Casino Table", 80);
        add("casino2_venezia_slot", "Venezia Slot Machine", 200);
        add("casino2_wheel_machine", "Wheel Machine", 170);
        add("casino2_woman_painting", "Casino Painting", 60);
        add("casino2_yellow_armrest_chair", "Yellow Armrest Chair", 70);

        setCategory("Farmer");
        // Farmer Decorations
        add("farmer_beer", "Farm Beer", 20);
        add("farmer_corn", "Farm Corn", 10);
        add("farmer_corn_basket", "Corn Basket", 25);
        add("farmer_door", "Farm Door", 30);
        add("farmer_fence", "Farm Fence", 15);
        add("farmer_fence_corner", "Farm Fence Corner", 15);
        add("farmer_firewood", "Farm Firewood", 20);
        add("farmer_flower", "Farm Flower", 10);
        add("farmer_hay_with_spade", "Hay with Spade", 25);
        add("farmer_log_axe", "Log & Axe", 30);
        add("farmer_milk_tank", "Milk Tank", 50);
        add("farmer_plate_rice", "Plate of Rice", 15);
        add("farmer_pond", "Farm Pond", 60);
        add("farmer_rice_basket", "Rice Basket", 25);
        add("farmer_scare_crow", "Scarecrow", 40);
        add("farmer_stone", "Farm Stone", 10);
        add("farmer_table", "Farm Table", 40);
        add("farmer_table_sawing", "Sawing Table", 50);
        add("farmer_tools_stand", "Tools Stand", 35);
        add("farmer_wheelbarrow", "Wheelbarrow", 45);

        setCategory("Caribbean");
        // Caribbean Vacation
        add("caribbean_arm_chair", "Caribbean Arm Chair", 35);
        add("caribbean_bed", "Caribbean Bed", 60);
        add("caribbean_counter_chair", "Caribbean Counter Chair", 30);
        add("caribbean_hanging_seat", "Caribbean Hanging Seat", 40);
        add("caribbean_beach_hanging_chair", "Beach Hanging Chair", 45);
        add("caribbean_ship_wheel_table", "Ship Wheel Table", 50);
        add("caribbean_plant_desk", "Caribbean Plant Desk", 35);
        add("caribbean_carpet", "Caribbean Carpet", 20);
        add("caribbean_hanging_lamp_carribean_style", "Caribbean Hanging Lamp", 30);
        add("caribbean_umbrella", "Beach Umbrella", 25);
        add("caribbean_palm_tree", "Palm Tree Decor", 40);
        add("caribbean_beach_sign", "Beach Sign", 20);
        add("caribbean_coconut_set", "Coconut Set", 15);
        add("caribbean_hand_washing", "Hand Washing Station", 35);
        add("caribbean_icecreambox_bike", "Ice Cream Bike", 55);
        add("caribbean_aquatrikes", "Aqua Trikes", 65);
        add("caribbean_ship_wheel_clock", "Ship Wheel Clock", 40);
        add("caribbean_sunset_painting", "Sunset Painting", 25);
        add("caribbean_swim_ring_decoration", "Swim Ring Decor", 15);
        add("caribbean_welcome_flower", "Welcome Flower", 20);

        setCategory("Bedroom");
        // Master Bedroom
        add("bedroom_bed", "Bedroom Bed", 65);
        add("bedroom_wardrobe", "Bedroom Wardrobe", 55);
        add("bedroom_dressing_table", "Dressing Table", 50);
        add("bedroom_chair_dressing_table", "Dressing Chair", 30);
        add("bedroom_drawer_cabinet", "Drawer Cabinet", 45);
        add("bedroom_bookshelf", "Bedroom Bookshelf", 40);
        add("bedroom_shelf", "Bedroom Wall Shelf", 25);
        add("bedroom_work_table", "Work Table", 45);
        add("bedroom_small_table", "Small Table", 30);
        add("bedroom_tv_stand", "TV Stand", 50);
        add("bedroom_sofa_bench", "Sofa Bench", 40);
        add("bedroom_sofa_chair", "Sofa Chair", 35);
        add("bedroom_lamp", "Bedroom Lamp", 25);
        add("bedroom_drawer_lamp", "Drawer Lamp", 35);
        add("bedroom_mirror", "Bedroom Mirror", 30);
        add("bedroom_carpet", "Bedroom Carpet", 20);
        add("bedroom_basket", "Bedroom Basket", 15);
        add("bedroom_bin", "Bedroom Bin", 10);
        add("bedroom_vase", "Bedroom Vase", 15);
        add("bedroom_picture_frame", "Picture Frame", 20);
        add("bedroom_desktop_photo_frame", "Photo Frame", 15);

        setCategory("Modern");
        // Modern Furniture
        add("modern_bed", "Modern Bed", 70);
        add("modern_sofa_01", "Modern Sofa", 55);
        add("modern_sofa_02", "Modern Sofa Alt", 55);
        add("modern_chair", "Modern Chair", 30);
        add("modern_computerchair", "Computer Chair", 35);
        add("modern_table_01", "Modern Table", 40);
        add("modern_table_02", "Modern Table Alt", 40);
        add("modern_table_03", "Modern Side Table", 30);
        add("modern_computerdesk", "Computer Desk", 55);
        add("modern_cabinet", "Modern Cabinet", 45);
        add("modern_wardrobe_01", "Modern Wardrobe", 50);
        add("modern_wardrobe_02", "Modern Wardrobe Alt", 50);
        add("modern_shelf", "Modern Shelf", 25);
        add("modern_lamp", "Modern Lamp", 30);
        add("modern_tv", "Modern TV", 60);
        add("modern_macbook", "Laptop", 65);
        add("modern_carpet", "Modern Carpet", 20);
        add("modern_picture", "Modern Picture", 20);
        add("modern_board", "Modern Board", 25);
        add("modern_plantpot", "Modern Plant Pot", 15);

        setCategory("Tavern");
        // Dungeon Tavern
        add("bl_bar_stool", "Tavern Bar Stool", 35);
        add("bl_barrel_table", "Tavern Barrel Table", 60);
        add("bl_barrel_table_2", "Tavern Barrel Table 2", 60);
        add("bl_bottle_wine_1", "Tavern Wine Bottle 1", 15);
        add("bl_bottle_wine_2", "Tavern Wine Bottle 2", 15);
        add("bl_bottle_wine_3", "Tavern Wine Bottle 3", 15);
        add("bl_bottle_wine_stack", "Tavern Wine Stack", 25);
        add("bl_glass_beer", "Tavern Beer Glass", 10);
        add("bl_glass_wine", "Tavern Wine Glass", 10);
        add("bl_table_large", "Tavern Large Table", 80);
        add("bl_table_large_2", "Tavern Large Table 2", 80);
        add("bl_tavern_bench", "Tavern Bench", 40);
        add("bl_tavern_cabinet_1", "Tavern Cabinet 1", 70);
        add("bl_tavern_cabinet_2", "Tavern Cabinet 2", 70);
        add("bl_tavern_counter", "Tavern Counter", 90);
        add("bl_wall_shelf", "Tavern Wall Shelf", 35);

        setCategory("Bank");
        // Bank
        add("bank_atm", "Bank ATM", 120);
        add("bank_barrier", "Bank Barrier", 30);
        add("bank_camera", "Bank Camera", 50);
        add("bank_case", "Bank Case", 40);
        add("bank_chair", "Bank Chair", 45);
        add("bank_chair_1", "Bank Chair Alt", 45);
        add("bank_computer", "Bank Computer", 100);
        add("bank_document", "Bank Document", 15);
        add("bank_glass", "Bank Glass Partition", 60);
        add("bank_greek_balance", "Greek Balance", 80);
        add("bank_locker", "Bank Locker", 90);
        add("bank_locker_open", "Bank Locker (Open)", 90);
        add("bank_money", "Bank Money Stack", 20);
        add("bank_money_1", "Bank Money Stack 2", 20);
        add("bank_money_case", "Bank Money Case", 50);
        add("bank_pot", "Bank Pot", 25);
        add("bank_safe_door", "Bank Safe Door", 150);
        add("bank_sign", "Bank Sign", 35);
        add("bank_table", "Bank Table", 70);
        add("bank_tall_chair", "Bank Tall Chair", 55);

        setCategory("Medieval Interior");
        // Medieval Interior
        add("med_arm_chair", "Arm Chair", 50);
        add("med_bed_bunk", "Bunk Bed", 80);
        add("med_bed_double", "Double Bed", 90);
        add("med_bed_single", "Single Bed", 60);
        add("med_book_open", "Open Book", 10);
        add("med_book_stack_horizontal_1", "Book Stack H1", 15);
        add("med_book_stack_horizontal_2", "Book Stack H2", 15);
        add("med_book_stack_vertical_1", "Book Stack V1", 15);
        add("med_book_stack_vertical_2", "Book Stack V2", 15);
        add("med_candle_dish", "Candle Dish", 20);
        add("med_chair", "Medieval Chair", 35);
        add("med_chair_cushion", "Cushioned Chair", 45);
        add("med_coffee_table", "Medieval Coffee Table", 50);
        add("med_coffee_table_cloth", "Coffee Table w/ Cloth", 55);
        add("med_desk", "Medieval Desk", 70);
        add("med_dresser_low", "Low Dresser", 60);
        add("med_dresser_tall", "Tall Dresser", 80);
        add("med_end_table", "End Table", 40);
        add("med_fireplace", "Medieval Fireplace", 120);
        add("med_lamp", "Medieval Lamp", 30);
        add("med_lamp_hanging", "Hanging Lamp", 35);
        add("med_lamp_tall", "Tall Lamp", 40);
        add("med_pot_1", "Medieval Pot 1", 20);
        add("med_pot_2", "Medieval Pot 2", 20);
        add("med_pot_3", "Medieval Pot 3", 20);
        add("med_scroll_open", "Open Scroll", 15);
        add("med_scroll_stack", "Scroll Stack", 20);
        add("med_sofa", "Medieval Sofa", 70);
        add("med_stool", "Medieval Stool", 25);
        add("med_stool_cushion", "Cushioned Stool", 30);
        add("med_table_long", "Long Table", 80);
        add("med_table_small", "Small Table", 45);
        add("med_table_small_cloth", "Small Table w/ Cloth", 50);

        setCategory("Medieval Market");
        // Medieval Market
        add("med_bulletin_board", "Bulletin Board", 40);
        add("med_bulletin_board_small", "Small Bulletin Board", 30);
        add("med_bulletin_board_wall", "Wall Bulletin Board", 35);
        add("med_canopy_flat", "Flat Canopy", 50);
        add("med_canopy_sloped", "Sloped Canopy", 55);
        add("med_chalk_sign", "Chalk Sign", 20);
        add("med_chalk_sign_hanging", "Hanging Chalk Sign", 25);
        add("med_chest", "Medieval Chest", 60);
        add("med_chest_open", "Open Chest", 60);
        add("med_coins_1", "Coin Pile Small", 15);
        add("med_coins_2", "Coin Pile Medium", 20);
        add("med_coins_3", "Coin Pile Large", 25);
        add("med_crate_bread", "Bread Crate", 30);
        add("med_crate_carrots", "Carrot Crate", 30);
        add("med_crate_display_bread", "Bread Display", 35);
        add("med_crate_display_carrots", "Carrot Display", 35);
        add("med_crate_display_empty", "Empty Display", 25);
        add("med_crate_empty", "Empty Crate", 20);
        add("med_crate_stack", "Crate Stack", 40);
        add("med_papers_hanging_1", "Hanging Papers 1", 15);
        add("med_papers_hanging_2", "Hanging Papers 2", 15);
        add("med_papers_hanging_3", "Hanging Papers 3", 15);
        add("med_sack", "Sack", 20);
        add("med_shipping_crate", "Shipping Crate", 45);
        add("med_shipping_crate_triple_stack", "Triple Crate Stack", 70);
        add("med_shop_cart", "Shop Cart", 80);
        add("med_streamer_post", "Streamer Post", 25);
        add("med_streamers_hanging", "Hanging Streamers", 20);
        add("med_wagon", "Wagon", 100);
        add("med_wagon_full", "Loaded Wagon", 120);

        setCategory("Medieval Nature");
        // Medieval Nature
        add("med_brown_mushroom_patch", "Brown Mushrooms", 10);
        add("med_brown_mushroom_patch_large", "Large Brown Mushrooms", 15);
        add("med_cattails", "Cattails", 10);
        add("med_clover_flowers", "Clover Flowers", 10);
        add("med_clovers", "Clovers", 10);
        add("med_crystal", "Crystal", 50);
        add("med_glow_mushroom_patch", "Glow Mushrooms", 15);
        add("med_glow_mushroom_patch_large", "Large Glow Mushrooms", 20);
        add("med_log_mushroom", "Log Mushroom", 15);
        add("med_log_mushroom_corner", "Log Mushroom Corner", 15);
        add("med_log_pile_large", "Large Log Pile", 25);
        add("med_log_pile_large_overgrown", "Overgrown Log Pile", 30);
        add("med_log_pile_small_1", "Small Log Pile 1", 15);
        add("med_log_pile_small_2", "Small Log Pile 2", 15);
        add("med_log_pile_small_3", "Small Log Pile 3", 15);
        add("med_orange_mushroom_patch", "Orange Mushrooms", 10);
        add("med_orange_mushroom_patch_large", "Large Orange Mushrooms", 15);
        add("med_ore", "Ore Deposit", 35);
        add("med_pebbles_1", "Pebbles 1", 5);
        add("med_pebbles_2", "Pebbles 2", 5);
        add("med_pebbles_3", "Pebbles 3", 5);
        add("med_plant", "Medieval Plant", 10);
        add("med_red_mushroom_patch", "Red Mushrooms", 10);
        add("med_red_mushroom_patch_large", "Large Red Mushrooms", 15);
        add("med_rock_1", "Rock 1", 5);
        add("med_rock_2", "Rock 2", 5);
        add("med_rock_3", "Rock 3", 5);
        add("med_rock_4", "Rock 4", 5);

        setCategory("Livingroom");
        add("lr_chair_1", "Chair 1", 55);
        add("lr_chair_2", "Chair 2", 55);
        add("lr_cupboard", "Cupboard", 80);
        add("lr_fireplace", "Fireplace", 180);
        add("lr_frame_1", "Frame 1", 25);
        add("lr_frame_2", "Frame 2", 25);
        add("lr_lamp_1", "Lamp 1", 35);
        add("lr_lamp_2", "Lamp 2", 35);
        add("lr_piano", "Piano", 250);
        add("lr_plant_1", "Plant 1", 20);
        add("lr_plant_2", "Plant 2", 20);
        add("lr_plant_3", "Plant 3", 20);
        add("lr_shelf_1", "Shelf 1", 40);
        add("lr_shelf_2", "Shelf 2", 40);
        add("lr_sofa_1", "Sofa 1", 90);
        add("lr_sofa_2", "Sofa 2", 90);
        add("lr_sofa_3", "Sofa 3", 120);
        add("lr_sofa_4", "Sofa 4", 120);
        add("lr_sound_system", "Sound System", 100);
        add("lr_table_1", "Table 1", 60);
        add("lr_table_2", "Table 2", 60);
        add("lr_tv", "TV", 80);

        setCategory("Medieval Furniture");
        add("ms2_medieval_bed_color", "Medieval Bed (Color)", 70);
        add("ms2_medieval_bed_dye", "Medieval Bed (Dye)", 70);
        add("ms2_medieval_book_1", "Medieval Book 1", 15);
        add("ms2_medieval_book_2", "Medieval Book 2", 15);
        add("ms2_medieval_book_3", "Medieval Book 3", 15);
        add("ms2_medieval_book_4", "Medieval Book 4", 15);
        add("ms2_medieval_brick_1", "Medieval Brick", 10);
        add("ms2_medieval_double_bed_color", "Medieval Double Bed (Color)", 100);
        add("ms2_medieval_double_bed_dye", "Medieval Double Bed (Dye)", 100);
        add("ms2_medieval_double_chair_color", "Medieval Double Chair (Color)", 65);
        add("ms2_medieval_double_chair_dye", "Medieval Double Chair (Dye)", 65);
        add("ms2_medieval_fan_color", "Medieval Fan", 40);
        add("ms2_medieval_fence_1", "Medieval Fence", 20);
        add("ms2_medieval_fire_stand", "Medieval Fire Stand", 55);
        add("ms2_medieval_flowerpot_1", "Medieval Flowerpot 1", 20);
        add("ms2_medieval_flowerpot_2", "Medieval Flowerpot 2", 20);
        add("ms2_medieval_flowerpot_3", "Medieval Flowerpot 3", 20);
        add("ms2_medieval_full_table_color", "Medieval Table (Color)", 70);
        add("ms2_medieval_full_table_color_1", "Medieval Table (Color Alt)", 70);
        add("ms2_medieval_oaklog_1", "Medieval Oak Log 1", 15);
        add("ms2_medieval_oaklog_2", "Medieval Oak Log 2", 15);
        add("ms2_medieval_oaklog_3", "Medieval Oak Log 3", 15);
        add("ms2_medieval_one_chair_1_color", "Medieval Chair 1 (Color)", 45);
        add("ms2_medieval_one_chair_1_dye", "Medieval Chair 1 (Dye)", 45);
        add("ms2_medieval_one_chair_color", "Medieval Chair (Color)", 45);
        add("ms2_medieval_one_chair_color_1", "Medieval Chair (Color Alt)", 45);
        add("ms2_medieval_one_chair_color_2", "Medieval Chair (Color Alt 2)", 45);
        add("ms2_medieval_one_chair_dye", "Medieval Chair (Dye)", 45);
        add("ms2_medieval_one_chair_dye_2", "Medieval Chair (Dye Alt)", 45);
        add("ms2_medieval_one_table_color_1", "Medieval Side Table", 50);
        add("ms2_medieval_scaffolding", "Medieval Scaffolding", 25);
        add("ms2_medieval_sign_bank", "Medieval Sign (Bank)", 30);
        add("ms2_medieval_sign_blacksmith", "Medieval Sign (Blacksmith)", 30);
        add("ms2_medieval_sign_cosmetic", "Medieval Sign (Cosmetic)", 30);
        add("ms2_medieval_sign_default", "Medieval Sign (Default)", 30);
        add("ms2_medieval_sign_enchanted", "Medieval Sign (Enchanted)", 30);
        add("ms2_medieval_sign_furniture", "Medieval Sign (Furniture)", 30);
        add("ms2_medieval_sign_potion", "Medieval Sign (Potion)", 30);

        setCategory("Medieval Structures");
        add("ms4_medieval_pack_v4_box_1", "Storage Box 1", 25);
        add("ms4_medieval_pack_v4_box_2", "Storage Box 2", 25);
        add("ms4_medieval_pack_v4_bridge", "Bridge Section", 80);
        add("ms4_medieval_pack_v4_bridge_mid", "Bridge Middle", 80);
        add("ms4_medieval_pack_v4_bridge_path", "Bridge Path", 70);
        add("ms4_medieval_pack_v4_bridge_stand", "Bridge Stand", 60);
        add("ms4_medieval_pack_v4_bridge_stand_1", "Bridge Stand Alt 1", 60);
        add("ms4_medieval_pack_v4_bridge_stand_2", "Bridge Stand Alt 2", 60);
        add("ms4_medieval_pack_v4_bridge_stand_mid_1", "Bridge Stand Mid 1", 60);
        add("ms4_medieval_pack_v4_bridge_stand_mid_2", "Bridge Stand Mid 2", 60);
        add("ms4_medieval_pack_v4_bridge_stand_mid_3", "Bridge Stand Mid 3", 60);
        add("ms4_medieval_pack_v4_cage_1", "Iron Cage 1", 55);
        add("ms4_medieval_pack_v4_cage_2", "Iron Cage 2", 55);
        add("ms4_medieval_pack_v4_camp_1", "Campsite", 45);
        add("ms4_medieval_pack_v4_chair_1", "Wooden Chair 1", 40);
        add("ms4_medieval_pack_v4_chair_2", "Wooden Chair 2", 40);
        add("ms4_medieval_pack_v4_gate_1", "Gate 1", 90);
        add("ms4_medieval_pack_v4_gate_2", "Gate 2", 90);
        add("ms4_medieval_pack_v4_ladder", "Ladder", 20);
        add("ms4_medieval_pack_v4_log_1", "Log 1", 10);
        add("ms4_medieval_pack_v4_log_2", "Log 2", 10);
        add("ms4_medieval_pack_v4_log_3", "Log 3", 10);
        add("ms4_medieval_pack_v4_log_4", "Log 4", 10);
        add("ms4_medieval_pack_v4_log_5", "Log 5", 10);
        add("ms4_medieval_pack_v4_log_6", "Log 6", 10);
        add("ms4_medieval_pack_v4_log_7", "Log 7", 10);
        add("ms4_medieval_pack_v4_log_8", "Log 8", 10);
        add("ms4_medieval_pack_v4_log_9", "Log 9", 10);
        add("ms4_medieval_pack_v4_rock_1", "Rock", 5);
        add("ms4_medieval_pack_v4_standlight", "Standing Light", 40);
        add("ms4_medieval_pack_v4_standlight_2", "Standing Light Alt", 40);
        add("ms4_medieval_pack_v4_support", "Support Beam", 30);
        add("ms4_medieval_pack_v4_support_h3", "Support Beam (Tall)", 35);
        add("ms4_medieval_pack_v4_tabe_1", "Table", 55);
        add("ms4_medieval_pack_v4_torch_1", "Wall Torch 1", 15);
        add("ms4_medieval_pack_v4_torch_2", "Wall Torch 2", 15);
        add("ms4_medieval_pack_v4_torch_3", "Wall Torch 3", 15);
        add("ms4_medieval_pack_v4_tower_1", "Tower Section", 120);
        add("ms4_medieval_pack_v4_wheel_1", "Cart Wheel", 35);

        setCategory("Medieval Village");
        add("mkt_medieval_market_bag", "Sack Bag", 20);
        add("mkt_medieval_market_barell", "Barrel", 30);
        add("mkt_medieval_market_bath", "Wooden Bath", 80);
        add("mkt_medieval_market_bed", "Village Bed", 65);
        add("mkt_medieval_market_bench", "Village Bench", 40);
        add("mkt_medieval_market_box", "Storage Box", 25);
        add("mkt_medieval_market_box_apples", "Apple Crate", 30);
        add("mkt_medieval_market_box_bread", "Bread Crate", 30);
        add("mkt_medieval_market_campfire", "Village Campfire", 45);
        add("mkt_medieval_market_chair", "Village Chair", 35);
        add("mkt_medieval_market_chest", "Village Chest", 60);

        setCategory("Park");
        add("park_bench", "Park Bench", 45);
        add("park_bird_house", "Bird House", 30);
        add("park_box", "Box", 20);
        add("park_box_construction", "Construction Box", 25);
        add("park_concret_pot_plant", "Concrete Pot Plant", 35);
        add("park_fence", "Park Fence", 15);
        add("park_lantern_box", "Lantern Box", 40);
        add("park_light_wooden_pallet", "Light Wooden Pallet", 15);
        add("park_little_box", "Little Box", 10);
        add("park_log_bench", "Log Bench", 40);
        add("park_picnic_table", "Picnic Table", 70);
        add("park_picnic_table_nappe", "Picnic Table (Cloth)", 80);
        add("park_stele_1", "Stone Pillar 1", 55);
        add("park_stele_2", "Stone Pillar 2", 55);
        add("park_stele_3", "Stone Pillar 3", 55);
        add("park_stele_4", "Stone Pillar 4", 55);
        add("park_stele_5", "Stone Pillar 5", 55);
        add("park_street_lamp", "Street Lamp", 45);
        add("park_street_lamp_double", "Street Lamp (Double)", 65);
        add("park_street_lamp_quadruple", "Street Lamp (Quad)", 100);
        add("park_street_lamp_upper", "Upper Street Lamp", 45);
        add("park_street_lamp_upper_double", "Upper Street Lamp (Double)", 65);
        add("park_street_lamp_upper_quadruple", "Upper Street Lamp (Quad)", 100);
        add("park_street_lamp_upper_triple", "Upper Street Lamp (Triple)", 85);
        add("park_swing", "Swing", 60);
        add("park_under_construction", "Under Construction Sign", 20);
        add("park_wooden_flower_pot", "Wooden Flower Pot", 25);
        add("park_wooden_ground_planks", "Wooden Ground Planks", 15);
        add("park_wooden_pallet", "Wooden Pallet", 15);

        setCategory("Crafting Station");
        // Alchemy
        add("craft_alchemy_station", "Alchemy Station", 120);
        add("craft_brewing_stand", "Brewing Stand", 80);
        add("craft_brewing_table", "Brewing Table", 90);
        add("craft_herb_wall_rack", "Herb Wall Rack", 35);
        add("craft_potion_shelf", "Potion Shelf", 45);
        // Carpentry
        add("craft_blueprint", "Blueprint", 15);
        add("craft_carpentry_station", "Carpentry Station", 110);
        add("craft_log_cutting_stand", "Log Cutting Stand", 70);
        add("craft_planks_leaning", "Planks Leaning", 10);
        add("craft_planks_pile", "Planks Pile", 10);
        // Cooking
        add("craft_cooking_shelf", "Cooking Shelf", 40);
        add("craft_cooking_station", "Cooking Station", 100);
        add("craft_ingredient_shelf", "Ingredient Shelf", 40);
        add("craft_spice_jars", "Spice Jars", 20);
        add("craft_spice_wall_rack", "Spice Wall Rack", 35);
        // Enchanting
        add("craft_enchanted_book_open", "Enchanted Book (Open)", 25);
        add("craft_enchanted_book_stack", "Enchanted Book Stack", 30);
        add("craft_enchanted_book_stack_tall", "Enchanted Book Stack (Tall)", 40);
        add("craft_enchanted_bookshelf", "Enchanted Bookshelf", 60);
        add("craft_enchanting_station", "Enchanting Station", 150);
        add("craft_enchanting_wall_shelf", "Enchanting Wall Shelf", 45);
        // Painting
        add("craft_easel", "Easel", 55);
        add("craft_paint_buckets", "Paint Buckets", 20);
        add("craft_paint_vials", "Paint Vials", 15);
        add("craft_paint_wall_shelf", "Paint Wall Shelf", 35);
        add("craft_painting_pegboard", "Painting Pegboard", 30);
        add("craft_painting_station", "Painting Station", 100);
        // Tailoring
        add("craft_fabric_spools", "Fabric Spools", 20);
        add("craft_fabric_stack", "Fabric Stack", 15);
        add("craft_loom", "Loom", 80);
        add("craft_mannequin", "Mannequin", 65);
        add("craft_standing_loom", "Standing Loom", 90);
        add("craft_tailoring_station", "Tailoring Station", 110);

        setCategory("Medieval Bathroom");
        add("bath_baquet", "Bath Tub", 80);
        add("bath_baquet_filled", "Filled Bath Tub", 90);
        add("bath_baquet_fabric", "Fabric Bath Tub", 90);
        add("bath_baquet_fabric_filled", "Filled Fabric Bath", 100);
        add("bath_stool", "Bathroom Stool", 25);
        add("bath_bucket", "Bathroom Bucket", 15);
        add("bath_bucket_filled", "Filled Bucket", 20);
        add("bath_toilet", "Toilet", 40);
        add("bath_toilet_double", "Double Toilet", 60);
        add("bath_table", "Bathroom Table", 50);
        add("bath_bench", "Bathroom Bench", 35);
        add("bath_towels", "Towels", 10);
        add("bath_pitcher", "Terracotta Pitcher", 15);
        add("bath_oil_lamp", "Oil Lamp", 30);
        add("bath_clothesline", "Clothesline", 20);
        add("bath_small_table", "Small Bathroom Table", 30);
        add("bath_basin", "Basin", 20);
        add("bath_soap", "Sponge & Soap", 10);
        add("bath_mirror", "Long Mirror", 40);

        setCategory("Viking");
        add("viking_axe_shield", "Viking Shield Display", 80);
        add("viking_bucket_1", "Viking Bucket 1", 20);
        add("viking_bucket_2", "Viking Bucket 2", 20);
        add("viking_cabinet_1", "Viking Cabinet 1", 70);
        add("viking_cabinet_2", "Viking Cabinet 2", 70);
        add("viking_statue_1", "Viking Statue 1", 60);
        add("viking_statue_2", "Viking Statue 2", 60);
        add("viking_statue_3", "Viking Statue 3", 60);
        add("viking_statue_4", "Viking Statue 4", 60);
        add("viking_statue_5", "Viking Statue 5", 60);
        add("viking_statue_6", "Viking Statue 6", 60);
        add("viking_statue_7", "Viking Statue 7", 60);
        add("viking_stone_chair_1", "Viking Stone Chair 1", 45);
        add("viking_stone_chair_2", "Viking Stone Chair 2", 45);
        add("viking_wooden_bed", "Viking Wooden Bed", 80);
        add("viking_wooden_chair_1", "Viking Wooden Chair 1", 40);
        add("viking_wooden_chair_2", "Viking Wooden Chair 2", 40);
        add("viking_wooden_hanging", "Viking Hanging Sign", 25);
        add("viking_wooden_pillar_1", "Viking Pillar 1", 15);
        add("viking_wooden_pillar_2", "Viking Pillar 2", 15);
        add("viking_wooden_pillar_3", "Viking Pillar 3", 15);
        add("viking_wooden_pillar_4", "Viking Pillar 4", 15);
        add("viking_wooden_pillar_5", "Viking Pillar 5", 15);
        add("viking_wooden_pillar_6", "Viking Pillar 6", 15);
        add("viking_wooden_pillar_7", "Viking Pillar 7", 15);
        add("viking_wooden_pillar_8", "Viking Pillar 8", 15);
        add("viking_wooden_pillar_9", "Viking Pillar 9", 15);
        add("viking_wooden_pillar_10", "Viking Pillar 10", 15);
        add("viking_wooden_pillar_11", "Viking Pillar 11", 15);
        add("viking_wooden_pillar_12", "Viking Pillar 12", 15);
        add("viking_wooden_table", "Viking Wooden Table", 55);

        setCategory("Quest Board");
        add("quest_board", "Quest Board", 200);
    }

    private static void setCategory(String cat) {
        currentCategory = cat;
        if (!CATEGORY_ORDER.contains(cat)) CATEGORY_ORDER.add(cat);
    }

    private static void add(String blockId, String displayName, int price) {
        int idx = CATALOG.size();
        CATALOG.add(new ShopItem("megamod:" + blockId, displayName, price, 0));
        ITEM_CATEGORIES.put(idx, currentCategory);
    }

    public static List<String> getCategories() {
        return CATEGORY_ORDER;
    }

    public static String getCategoryForIndex(int index) {
        return ITEM_CATEGORIES.getOrDefault(index, "Other");
    }

    public static List<ShopItem> getCatalog() {
        return CATALOG;
    }

    public static String getCatalogJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < CATALOG.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(CATALOG.get(i).toJson());
        }
        sb.append("]");
        return sb.toString();
    }

    public static boolean buyItem(ServerPlayer player, int index) {
        if (index < 0 || index >= CATALOG.size()) return false;
        ShopItem item = CATALOG.get(index);
        ServerLevel level = player.level();
        EconomyManager eco = EconomyManager.get(level);
        if (!eco.spendWallet(player.getUUID(), item.buyPrice())) return false;
        try {
            Identifier id = Identifier.parse(item.itemId());
            Optional<?> opt = BuiltInRegistries.ITEM.getOptional(id);
            if (opt.isPresent()) {
                ItemStack stack = new ItemStack((ItemLike) opt.get());
                if (!player.getInventory().add(stack)) {
                    player.spawnAtLocation(level, stack);
                }
                return true;
            }
        } catch (Exception ignored) {}
        // Refund if item couldn't be created
        eco.addWallet(player.getUUID(), item.buyPrice());
        return false;
    }
}
