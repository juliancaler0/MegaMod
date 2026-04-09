package com.ultra.megamod.feature.computer.admin;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.neoforge.event.entity.player.AnvilCraftEvent;

/**
 * Admin-only enchantment overrides:
 * - Anvil: bypass all enchantment compatibility restrictions, no XP cost
 * - Grindstone: return enchanted books instead of destroying enchantments
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AdminEnchantEvents {

    // ==================== Anvil: Bypass Incompatibility ====================

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        Player player = event.getPlayer();
        if (!AdminSystem.isAdmin(player.getGameProfile().name())) return;

        // Set cost to 1 (minimum needed for anvil to allow taking output)
        // The actual level is refunded in onAnvilCraft
        event.setXpCost(1);

        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.isEmpty() || right.isEmpty()) return;

        // If vanilla already produced an output, just keep it with cost 1
        if (!event.getOutput().isEmpty()) return;

        boolean rightIsBook = right.is(Items.ENCHANTED_BOOK);

        // Get enchantments from both sides
        ItemEnchantments leftEnchants = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments rightEnchants = rightIsBook
                ? right.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                : right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Only handle if the right side actually has enchantments
        if (rightEnchants.isEmpty()) return;

        // Build merged enchantment set — take max level for shared enchantments
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(leftEnchants);
        rightEnchants.entrySet().forEach(entry -> {
            Holder<Enchantment> enchant = entry.getKey();
            int rightLevel = entry.getIntValue();
            int leftLevel = leftEnchants.getLevel(enchant);
            if (leftLevel == rightLevel) {
                merged.set(enchant, leftLevel + 1);
            } else {
                merged.set(enchant, Math.max(leftLevel, rightLevel));
            }
        });

        // Create output: copy of left with merged enchantments
        ItemStack output = left.copy();
        output.set(DataComponents.ENCHANTMENTS, merged.toImmutable());

        // Apply name if present
        String name = event.getName();
        if (name != null && !name.isEmpty()) {
            output.set(DataComponents.CUSTOM_NAME,
                    net.minecraft.network.chat.Component.literal(name));
        } else if (name != null && name.isEmpty()) {
            output.remove(DataComponents.CUSTOM_NAME);
        }

        event.setOutput(output);
        event.setXpCost(1);
        event.setMaterialCost(1);
    }

    // Refund the 1 level after the admin takes the anvil output
    @SubscribeEvent
    public static void onAnvilCraft(AnvilCraftEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;

        // Give back the 1 level that was charged
        player.giveExperienceLevels(1);
    }

    // ==================== Grindstone: Return Enchanted Books ====================

    @SubscribeEvent
    public static void onGrindstoneTake(GrindstoneEvent.OnTakeItem event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;

        // Collect enchantments from both input slots
        ItemEnchantments topEnchants = event.getTopItem().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments bottomEnchants = event.getBottomItem().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        // Create one enchanted book per enchantment
        ServerLevel level = (ServerLevel) sp.level();

        giveEnchantedBooks(sp, topEnchants, level);
        giveEnchantedBooks(sp, bottomEnchants, level);

        // No XP orbs for admins (they get books instead)
        event.setXp(0);
    }

    private static void giveEnchantedBooks(ServerPlayer player, ItemEnchantments enchantments, ServerLevel level) {
        if (enchantments.isEmpty()) return;

        enchantments.entrySet().forEach(entry -> {
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable bookEnchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            bookEnchants.set(entry.getKey(), entry.getIntValue());
            book.set(DataComponents.STORED_ENCHANTMENTS, bookEnchants.toImmutable());

            if (!player.getInventory().add(book)) {
                player.spawnAtLocation(level, book);
            }
        });
    }
}
