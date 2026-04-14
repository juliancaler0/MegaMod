package com.ultra.megamod.feature.computer.network.handlers;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import com.ultra.megamod.reliquary.init.ModBlocks;
import com.ultra.megamod.reliquary.init.ModItems;
import com.ultra.megamod.reliquary.item.MobCharmItem;
import com.ultra.megamod.reliquary.item.MobCharmRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin handler for the Computer admin panel's "Reliquary" tab. Every action is
 * gated on {@link AdminSystem#isAdmin(ServerPlayer)} and returns a short admin_result
 * message so the client panel can surface feedback.
 *
 * <p>All Reliquary item lookups go through {@link ModItems} (compile-time supplier
 * references) rather than raw registry strings so we catch rename/remove breaks at
 * compile time.
 */
public class ReliquaryAdminHandler {

    private ReliquaryAdminHandler() {}

    public static boolean handle(ServerPlayer player, String action, String jsonData,
                                 ServerLevel level, EconomyManager eco) {
        if (!action.startsWith("reliquary_")) return false;
        if (!AdminSystem.isAdmin(player)) return false;

        switch (action) {
            case "reliquary_give_item" -> {
                handleGiveItem(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_all_relics" -> {
                handleGiveAllRelics(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_all_potions" -> {
                handleGiveAllPotions(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_all_reagents" -> {
                handleGiveAllReagents(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_all_charms" -> {
                handleGiveAllCharms(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_handgun_loadout" -> {
                handleGiveHandgunLoadout(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_all_pedestals" -> {
                handleGiveAllPedestals(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_give_apothecary_kit" -> {
                handleGiveApothecaryKit(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_refill_magazines" -> {
                handleRefillMagazines(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_refill_tome_charge" -> {
                handleRefillTomeCharge(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_reset_cooldowns" -> {
                handleResetCooldowns(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_dump_mob_charms" -> {
                handleDumpMobCharms(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_list_pedestals" -> {
                handleListPedestals(player, jsonData, level, eco);
                return true;
            }
            case "reliquary_info" -> {
                handleInfo(player, level, eco);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // Give actions
    // ──────────────────────────────────────────────────────────────────────

    private static void handleGiveItem(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        // jsonData format: "<item_id>[,count]"  — target is the acting admin.
        if (jsonData == null || jsonData.isBlank()) {
            sendResponse(admin, "admin_result", "{\"msg\":\"reliquary_give_item: empty payload (expected item_id[,count])\"}", eco);
            return;
        }
        String[] parts = jsonData.split(",", 2);
        String itemPath = parts[0].trim();
        int count = 1;
        if (parts.length > 1) {
            try { count = Math.max(1, Integer.parseInt(parts[1].trim())); } catch (NumberFormatException ignored) {}
        }

        // Accept bare path ("hero_medallion") or full id ("reliquary:hero_medallion").
        Identifier id = itemPath.contains(":") ? Identifier.parse(itemPath)
                                               : Identifier.fromNamespaceAndPath("reliquary", itemPath);
        // 1.21.11: BuiltInRegistries.ITEM.get(id) returns Optional<Reference<Item>>.
        Item item = BuiltInRegistries.ITEM.get(id).map(ref -> ref.value()).orElse(null);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            sendResponse(admin, "admin_result", "{\"msg\":\"Unknown Reliquary item: " + id + "\"}", eco);
            return;
        }

        int given = giveInBatches(admin, new ItemStack(item), count, level);
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave " + given + "x " + id + " to " + admin.getGameProfile().name() + "\"}", eco);
    }

    private static void handleGiveAllRelics(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        Item[] relics = {
            ModItems.HERO_MEDALLION.get(),
            ModItems.FORTUNE_COIN.get(),
            ModItems.TWILIGHT_CLOAK.get(),
            ModItems.LANTERN_OF_PARANOIA.get(),
            ModItems.KRAKEN_SHELL.get(),
            ModItems.PHOENIX_DOWN.get(),
            ModItems.SALAMANDER_EYE.get(),
            ModItems.RENDING_GALE.get(),
            ModItems.SERPENT_STAFF.get(),
            ModItems.ICE_MAGUS_ROD.get(),
            ModItems.GLACIAL_STAFF.get(),
            ModItems.PYROMANCER_STAFF.get(),
            ModItems.SOJOURNER_STAFF.get(),
            ModItems.ENDER_STAFF.get(),
            ModItems.DESTRUCTION_CATALYST.get(),
            ModItems.INFERNAL_CLAWS.get(),
            ModItems.MAGICBANE.get(),
            ModItems.MERCY_CROSS.get(),
            ModItems.SHEARS_OF_WINTER.get(),
            ModItems.WITCH_HAT.get(),
            ModItems.ROD_OF_LYSSA.get(),
            ModItems.HARVEST_ROD.get(),
            ModItems.MIDAS_TOUCHSTONE.get(),
            ModItems.EMPEROR_CHALICE.get(),
            ModItems.INFERNAL_CHALICE.get(),
            ModItems.VOID_TEAR.get(),
            ModItems.INFERNAL_TEAR.get(),
            ModItems.ALKAHESTRY_TOME.get(),
            ModItems.WITHERLESS_ROSE.get(),
            ModItems.ANGELIC_FEATHER.get(),
            ModItems.HOLY_HAND_GRENADE.get(),
        };
        int given = 0;
        for (Item relic : relics) {
            if (relic == null) continue;
            ItemStack stack = new ItemStack(relic, 1);
            if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
            given++;
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave " + given + " relic items to " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleGiveAllPotions(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        Item[] potions = {
            ModItems.POTION.get(),
            ModItems.SPLASH_POTION.get(),
            ModItems.LINGERING_POTION.get(),
            ModItems.ANGELHEART_VIAL.get(),
            ModItems.APHRODITE_POTION.get(),
            ModItems.FERTILE_POTION.get(),
            ModItems.GLOWING_WATER.get(),
            ModItems.EMPTY_POTION_VIAL.get(),
        };
        int given = 0;
        for (Item potion : potions) {
            if (potion == null) continue;
            ItemStack stack = new ItemStack(potion, 3);
            if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
            given++;
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave " + given + " Reliquary potion types (x3 each) to " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleGiveAllReagents(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        Item[] reagents = {
            ModItems.SLIME_PEARL.get(),
            ModItems.CATALYZING_GLAND.get(),
            ModItems.CHELICERAE.get(),
            ModItems.RIB_BONE.get(),
            ModItems.MOLTEN_CORE.get(),
            ModItems.BAT_WING.get(),
            ModItems.EYE_OF_THE_STORM.get(),
            ModItems.FROZEN_CORE.get(),
            ModItems.NEBULOUS_HEART.get(),
            ModItems.FERTILE_ESSENCE.get(),
        };
        int given = 0;
        for (Item reagent : reagents) {
            if (reagent == null) continue;
            ItemStack stack = new ItemStack(reagent, 16);
            if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
            given++;
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave " + given + " reagent types (x16 each) to " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleGiveAllCharms(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        MobCharmItem charmItem = ModItems.MOB_CHARM.get();
        int given = 0;
        for (Identifier regName : MobCharmRegistry.getRegisteredNames()) {
            ItemStack charm = charmItem.getStackFor(regName);
            if (!target.getInventory().add(charm)) target.spawnAtLocation(level, charm);
            given++;
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave " + given + " mob charms to " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleGiveHandgunLoadout(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);

        // Handgun + one magazine of each type + a stack (64) of each bullet variant.
        Item[] magazines = {
            ModItems.NEUTRAL_MAGAZINE.get(),
            ModItems.EXORCISM_MAGAZINE.get(),
            ModItems.BLAZE_MAGAZINE.get(),
            ModItems.ENDER_MAGAZINE.get(),
            ModItems.CONCUSSIVE_MAGAZINE.get(),
            ModItems.BUSTER_MAGAZINE.get(),
            ModItems.SEEKER_MAGAZINE.get(),
            ModItems.SAND_MAGAZINE.get(),
            ModItems.STORM_MAGAZINE.get(),
        };
        Item[] bullets = {
            ModItems.NEUTRAL_BULLET.get(),
            ModItems.EXORCISM_BULLET.get(),
            ModItems.BLAZE_BULLET.get(),
            ModItems.ENDER_BULLET.get(),
            ModItems.CONCUSSIVE_BULLET.get(),
            ModItems.BUSTER_BULLET.get(),
            ModItems.SEEKER_BULLET.get(),
            ModItems.SAND_BULLET.get(),
            ModItems.STORM_BULLET.get(),
        };

        tryGive(target, new ItemStack(ModItems.HANDGUN.get(), 1), level);
        int magazineCount = 0;
        for (Item mag : magazines) {
            if (mag == null) continue;
            tryGive(target, new ItemStack(mag, 1), level);
            magazineCount++;
        }
        int bulletCount = 0;
        for (Item bullet : bullets) {
            if (bullet == null) continue;
            tryGive(target, new ItemStack(bullet, 64), level);
            bulletCount++;
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Handgun loadout to " + target.getGameProfile().name() + ": 1 handgun, " +
                        magazineCount + " magazines, " + bulletCount + " bullet stacks\"}", eco);
    }

    private static void handleGiveAllPedestals(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        int activeGiven = 0;
        int passiveGiven = 0;
        for (DyeColor color : DyeColor.values()) {
            var activeSupp = ModBlocks.PEDESTAL_ITEMS.get(color);
            var passiveSupp = ModBlocks.PASSIVE_PEDESTAL_ITEMS.get(color);
            if (activeSupp != null && activeSupp.get() != null) {
                tryGive(target, new ItemStack(activeSupp.get(), 1), level);
                activeGiven++;
            }
            if (passiveSupp != null && passiveSupp.get() != null) {
                tryGive(target, new ItemStack(passiveSupp.get(), 1), level);
                passiveGiven++;
            }
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave " + activeGiven + " colored pedestals + " + passiveGiven +
                        " passive pedestals to " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleGiveApothecaryKit(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        // Cauldron + mortar
        tryGive(target, new ItemStack(ModBlocks.APOTHECARY_CAULDRON_ITEM.get(), 1), level);
        tryGive(target, new ItemStack(ModBlocks.APOTHECARY_MORTAR_ITEM.get(), 1), level);

        // Reagent sampler (x8 each)
        Item[] reagents = {
            ModItems.SLIME_PEARL.get(), ModItems.CATALYZING_GLAND.get(), ModItems.CHELICERAE.get(),
            ModItems.RIB_BONE.get(), ModItems.MOLTEN_CORE.get(), ModItems.BAT_WING.get(),
            ModItems.EYE_OF_THE_STORM.get(), ModItems.FROZEN_CORE.get(), ModItems.NEBULOUS_HEART.get(),
            ModItems.FERTILE_ESSENCE.get(),
        };
        for (Item reagent : reagents) {
            if (reagent == null) continue;
            tryGive(target, new ItemStack(reagent, 8), level);
        }

        // All potion items
        Item[] potions = {
            ModItems.POTION.get(), ModItems.SPLASH_POTION.get(), ModItems.LINGERING_POTION.get(),
            ModItems.ANGELHEART_VIAL.get(), ModItems.APHRODITE_POTION.get(), ModItems.FERTILE_POTION.get(),
            ModItems.GLOWING_WATER.get(), ModItems.EMPTY_POTION_VIAL.get(),
        };
        for (Item potion : potions) {
            if (potion == null) continue;
            tryGive(target, new ItemStack(potion, 3), level);
        }

        sendResponse(admin, "admin_result",
                "{\"msg\":\"Gave apothecary kit (cauldron, mortar, reagents, potions) to "
                        + target.getGameProfile().name() + "\"}", eco);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Maintenance actions
    // ──────────────────────────────────────────────────────────────────────

    private static void handleRefillMagazines(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        int refilled = 0;
        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof com.ultra.megamod.reliquary.item.MagazineItem) {
                // Standard ItemStack damage API — magazines encode shots as damage.
                if (stack.getDamageValue() > 0 || stack.isDamaged()) {
                    stack.setDamageValue(0);
                    refilled++;
                } else {
                    refilled++;
                }
            }
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Refilled " + refilled + " magazine(s) on " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleRefillTomeCharge(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        int refilled = 0;
        Item tomeItem = ModItems.ALKAHESTRY_TOME.get();
        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == tomeItem) {
                try {
                    com.ultra.megamod.reliquary.item.AlkahestryTomeItem.setCharge(
                            stack,
                            com.ultra.megamod.reliquary.item.AlkahestryTomeItem.getChargeLimit());
                    refilled++;
                } catch (Exception e) {
                    MegaMod.LOGGER.warn("Failed to refill Alkahestry Tome charge: {}", e.getMessage());
                }
            }
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Refilled " + refilled + " Alkahestry Tome(s) on " + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleResetCooldowns(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);

        // Known Reliquary items that gate behavior on player cooldowns. Some may not
        // actually use CooldownTracker — clearing harmlessly returns without touching them.
        Item[] cooldownItems = {
            ModItems.PHOENIX_DOWN.get(),
            ModItems.ANGELHEART_VIAL.get(),
            ModItems.HOLY_HAND_GRENADE.get(),
            ModItems.HANDGUN.get(),
            ModItems.ENDER_STAFF.get(),
            ModItems.RENDING_GALE.get(),
            ModItems.SOJOURNER_STAFF.get(),
            ModItems.PYROMANCER_STAFF.get(),
            ModItems.GLACIAL_STAFF.get(),
            ModItems.SERPENT_STAFF.get(),
            ModItems.ICE_MAGUS_ROD.get(),
            ModItems.HARVEST_ROD.get(),
            ModItems.ROD_OF_LYSSA.get(),
            ModItems.DESTRUCTION_CATALYST.get(),
            ModItems.MIDAS_TOUCHSTONE.get(),
            ModItems.INFERNAL_TEAR.get(),
            ModItems.VOID_TEAR.get(),
            ModItems.FORTUNE_COIN.get(),
            ModItems.SALAMANDER_EYE.get(),
            ModItems.TWILIGHT_CLOAK.get(),
            ModItems.LANTERN_OF_PARANOIA.get(),
        };
        int cleared = 0;
        for (Item it : cooldownItems) {
            if (it == null) continue;
            // 1.21.11: ItemCooldowns#removeCooldown takes the cooldown group Identifier.
            // When an item has no explicit cooldown group, vanilla uses its registry key.
            try {
                Identifier itemKey = BuiltInRegistries.ITEM.getKey(it);
                target.getCooldowns().removeCooldown(itemKey);
                cleared++;
            } catch (Exception ignored) {}
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Cleared " + cleared + " Reliquary cooldown entries on "
                        + target.getGameProfile().name() + "\"}", eco);
    }

    private static void handleDumpMobCharms(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        ServerPlayer target = resolveTarget(admin, jsonData);
        List<String> lines = new ArrayList<>();
        Item beltItem = ModItems.MOB_CHARM_BELT.get();
        Item charmItem = ModItems.MOB_CHARM.get();

        // Find all belts in the target's main inventory / offhand / armor.
        int beltsFound = 0;
        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != beltItem) continue;
            beltsFound++;
            com.ultra.megamod.reliquary.item.MobCharmBeltItem belt =
                    (com.ultra.megamod.reliquary.item.MobCharmBeltItem) beltItem;
            int slots = 0;
            try { slots = belt.getCharmCount(stack); } catch (Exception ignored) {}
            for (int s = 0; s < slots; s++) {
                ItemStack charm = belt.getMobCharmInSlot(stack, s);
                if (charm.isEmpty()) continue;
                Identifier regName = MobCharmItem.getEntityEggRegistryName(charm);
                int dmg = charm.getDamageValue();
                int max = charm.getMaxDamage();
                lines.add(" - [" + s + "] " + regName + " dmg=" + dmg + "/" + max);
            }
        }

        // Also stand-alone mob charms in the inventory.
        int looseCharms = 0;
        for (int i = 0; i < target.getInventory().getContainerSize(); i++) {
            ItemStack stack = target.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != charmItem) continue;
            Identifier regName = MobCharmItem.getEntityEggRegistryName(stack);
            int dmg = stack.getDamageValue();
            int max = stack.getMaxDamage();
            lines.add(" - (loose slot " + i + ") " + regName + " dmg=" + dmg + "/" + max);
            looseCharms++;
        }

        target.sendSystemMessage(Component.literal("[Reliquary] Mob charm dump for " + target.getGameProfile().name())
                .withStyle(ChatFormatting.AQUA));
        if (lines.isEmpty()) {
            target.sendSystemMessage(Component.literal("  (no mob charms found in inventory)")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            for (String line : lines) {
                target.sendSystemMessage(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Dumped " + lines.size() + " charms (" + beltsFound + " belts + "
                        + looseCharms + " loose) to " + target.getGameProfile().name() + "'s chat\"}", eco);
    }

    private static void handleListPedestals(ServerPlayer admin, String jsonData, ServerLevel level, EconomyManager eco) {
        int radius = 64;
        BlockPos center = admin.blockPosition();

        // Precompute set of pedestal blocks for fast identity check.
        java.util.Set<Block> pedestalBlocks = new java.util.HashSet<>();
        for (var supp : ModBlocks.PEDESTALS.values()) pedestalBlocks.add(supp.get());
        for (var supp : ModBlocks.PASSIVE_PEDESTALS.values()) pedestalBlocks.add(supp.get());

        int minX = center.getX() - radius, maxX = center.getX() + radius;
        int minY = Math.max(level.getMinY(), center.getY() - radius);
        int maxY = Math.min(level.getMaxY(), center.getY() + radius);
        int minZ = center.getZ() - radius, maxZ = center.getZ() + radius;

        int found = 0;
        admin.sendSystemMessage(Component.literal("[Reliquary] Pedestals within " + radius + " blocks:")
                .withStyle(ChatFormatting.AQUA));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (!pedestalBlocks.contains(level.getBlockState(cursor).getBlock())) continue;
                    BlockEntity be = level.getBlockEntity(cursor);
                    String held = "(empty)";
                    if (be instanceof com.ultra.megamod.reliquary.block.tile.PassivePedestalBlockEntity pedBe) {
                        ItemStack item = pedBe.getItem();
                        if (!item.isEmpty()) {
                            held = item.getCount() + "x " + BuiltInRegistries.ITEM.getKey(item.getItem());
                        }
                    }
                    admin.sendSystemMessage(Component.literal(
                            " - (" + x + "," + y + "," + z + ") " + held)
                            .withStyle(ChatFormatting.GRAY));
                    found++;
                    if (found >= 128) {
                        admin.sendSystemMessage(Component.literal(" ... (truncated at 128 results)")
                                .withStyle(ChatFormatting.YELLOW));
                        sendResponse(admin, "admin_result",
                                "{\"msg\":\"Listed 128+ pedestals (truncated) within " + radius + " blocks\"}", eco);
                        return;
                    }
                }
            }
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Listed " + found + " pedestals within " + radius + " blocks\"}", eco);
    }

    private static void handleInfo(ServerPlayer admin, ServerLevel level, EconomyManager eco) {
        String[] toggles = {
            "reliquary",
            "reliquary_handgun",
            "reliquary_pedestals",
            "reliquary_alkahestry",
            "reliquary_apothecary",
            "reliquary_potions_replace_alchemy",
            "reliquary_relics",
            "reliquary_mob_charms",
            "reliquary_void_tear",
            "reliquary_fragment_drops",
            "reliquary_witherless_rose",
            "reliquary_chest_loot",
        };
        FeatureToggleManager mgr = FeatureToggleManager.get(level);
        admin.sendSystemMessage(Component.literal("[Reliquary] Feature toggle state:")
                .withStyle(ChatFormatting.AQUA));
        int enabled = 0;
        for (String key : toggles) {
            boolean on = mgr.isEnabled(key);
            if (on) enabled++;
            ChatFormatting color = on ? ChatFormatting.GREEN : ChatFormatting.RED;
            admin.sendSystemMessage(Component.literal(" - " + key + " = " + (on ? "ON" : "OFF"))
                    .withStyle(color));
        }
        sendResponse(admin, "admin_result",
                "{\"msg\":\"Reliquary toggles: " + enabled + "/" + toggles.length + " enabled (dumped to chat)\"}", eco);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────

    /** Resolves the action's target player from the jsonData "name" field, defaulting to the acting admin. */
    private static ServerPlayer resolveTarget(ServerPlayer admin, String jsonData) {
        if (jsonData == null || jsonData.isBlank()) return admin;
        ServerPlayer found = admin.level().getServer().getPlayerList().getPlayerByName(jsonData.trim());
        return (found != null) ? found : admin;
    }

    /** Give the stack, splitting into inventory adds or floor drops; returns how many stacks were handed off. */
    private static void tryGive(ServerPlayer target, ItemStack stack, ServerLevel level) {
        if (stack.isEmpty()) return;
        if (!target.getInventory().add(stack)) target.spawnAtLocation(level, stack);
    }

    /** Give `totalCount` copies of the single-item stack, respecting max stack size. */
    private static int giveInBatches(ServerPlayer target, ItemStack prototype, int totalCount, ServerLevel level) {
        if (prototype.isEmpty() || totalCount <= 0) return 0;
        int maxStack = Math.max(1, prototype.getMaxStackSize());
        int remaining = totalCount;
        int given = 0;
        while (remaining > 0) {
            int batch = Math.min(maxStack, remaining);
            ItemStack copy = prototype.copy();
            copy.setCount(batch);
            if (!target.getInventory().add(copy)) target.spawnAtLocation(level, copy);
            given += batch;
            remaining -= batch;
        }
        return given;
    }

    private static void sendResponse(ServerPlayer player, String dataType, String json, EconomyManager eco) {
        int wallet = eco.getWallet(player.getUUID());
        int bank = eco.getBank(player.getUUID());
        PacketDistributor.sendToPlayer(player,
                (CustomPacketPayload) new ComputerDataPayload(dataType, json, wallet, bank));
    }
}
