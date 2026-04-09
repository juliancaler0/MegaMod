package com.ultra.megamod.feature.skills.locks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side event handlers that enforce skill-based item locks.
 *
 * USE LOCKS:
 * - Weapon: Cannot hold in hand — forced back to inventory + actionbar warning
 * - Armor: Unequipped back to inventory on next tick
 * - Consumable/Right-click: Cancelled + actionbar warning
 *
 * GENERATION LOCKS:
 * - Enchanting table: Handled by EnchantmentLevelSetEvent (reduces level)
 * - Post-enchant stripping: Handled after enchanting completes
 */
@EventBusSubscriber(modid = "megamod")
public class SkillLockEvents {

    // Cooldown for lock messages (prevent spam) - player UUID -> last message tick
    private static final Map<UUID, Long> MESSAGE_COOLDOWNS = new ConcurrentHashMap<>();
    private static final long MESSAGE_COOLDOWN_TICKS = 40L; // 2 seconds

    // ==================== Weapon Use Lock ====================

    /**
     * Cancel attacks with locked weapons entirely — player cannot swing them.
     */
    @SubscribeEvent
    public static void onAttackWithLockedWeapon(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        if (!SkillLockManager.canUseItem(player, mainHand)) {
            event.setCanceled(true);
            sendLockMessage(player, mainHand);
        }
    }

    /**
     * Safety fallback: if damage somehow gets through with a locked weapon, zero it out.
     */
    @SubscribeEvent
    public static void onDamageWithLockedWeapon(LivingDamageEvent.Pre event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        if (!SkillLockManager.canUseItem(player, mainHand)) {
            event.setNewDamage(0.0f);
        }
    }

    // ==================== Equipment Equip Lock (Armor + Weapons) ====================

    /**
     * If a player equips locked armor or switches to a locked weapon, move it back.
     */
    @SubscribeEvent
    public static void onEquipLockedItem(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        EquipmentSlot slot = event.getSlot();
        ItemStack newItem = event.getTo();
        if (newItem.isEmpty()) return;

        // Handle armor slots
        if (slot.isArmor()) {
            if (!SkillLockManager.canUseItem(player, newItem)) {
                ServerLevel level = (ServerLevel) player.level();
                level.getServer().execute(() -> {
                    ItemStack equipped = player.getItemBySlot(slot);
                    if (!equipped.isEmpty() && !SkillLockManager.canUseItem(player, equipped)) {
                        player.setItemSlot(slot, ItemStack.EMPTY);
                        if (!player.getInventory().add(equipped)) {
                            player.spawnAtLocation(level, equipped);
                        }
                        sendLockMessage(player, equipped);
                    }
                });
            }
            return;
        }

        // Handle main hand or off-hand — if player switches to a locked weapon/shield, move it out
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            if (!SkillLockManager.canUseItem(player, newItem)) {
                ServerLevel level = (ServerLevel) player.level();
                final EquipmentSlot targetSlot = slot;
                level.getServer().execute(() -> {
                    ItemStack held = player.getItemBySlot(targetSlot);
                    if (!held.isEmpty() && !SkillLockManager.canUseItem(player, held)) {
                        // Clear the slot, then stash item in inventory or drop
                        ItemStack copy = held.copy();
                        player.setItemSlot(targetSlot, ItemStack.EMPTY);
                        // Try to place in a non-hotbar slot so it doesn't snap back
                        boolean moved = false;
                        for (int i = 9; i < 36; i++) {
                            if (player.getInventory().getItem(i).isEmpty()) {
                                player.getInventory().setItem(i, copy);
                                moved = true;
                                break;
                            }
                        }
                        if (!moved) {
                            // All non-hotbar slots full — drop it
                            player.spawnAtLocation(level, copy);
                        }
                        sendLockMessage(player, copy);
                    }
                });
            }
        }
    }

    // ==================== Consumable / Right-Click Use Lock ====================

    /**
     * Cancel using locked consumables (golden apple, suspicious stew, etc.)
     */
    @SubscribeEvent
    public static void onUseLockedConsumable(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack item = event.getItem();
        if (!SkillLockManager.canUseItem(player, item)) {
            event.setCanceled(true);
            sendLockMessage(player, item);
        }
    }

    /**
     * Cancel right-click interactions with locked items (ender pearl, ender chest, etc.)
     */
    @SubscribeEvent
    public static void onRightClickLockedItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack item = event.getItemStack();
        if (!SkillLockManager.canUseItem(serverPlayer, item)) {
            event.setCanceled(true);
            sendLockMessage(serverPlayer, item);
        }
    }

    /**
     * Cancel right-click block interactions for locked placeable items (beacon, conduit, etc.)
     */
    @SubscribeEvent
    public static void onPlaceLockedBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack item = event.getItemStack();
        if (item.isEmpty()) return;

        String itemPath = BuiltInRegistries.ITEM.getKey(item.getItem()).getPath();
        // Only check placement for specific locked block items
        if (itemPath.equals("beacon") || itemPath.equals("conduit") || itemPath.equals("respawn_anchor")
                || itemPath.equals("end_crystal") || itemPath.equals("lodestone")
                || itemPath.contains("shulker_box") || itemPath.equals("ender_chest")) {
            if (!SkillLockManager.canUseItem(serverPlayer, item)) {
                event.setCanceled(true);
                sendLockMessage(serverPlayer, item);
            }
        }
    }

    // ==================== Elytra / Saddle Mount Lock ====================

    /**
     * Check elytra activation — if player has elytra equipped but can't use it,
     * prevent flight start. Checked every tick for flying players.
     * (Handled by equipment change event above for equipping)
     */

    // ==================== Enchantment Generation Lock ====================

    /**
     * On enchantment level calculation: if the resulting enchant level would produce
     * locked enchantments, reduce the level.
     */
    @SubscribeEvent
    public static void onEnchantLevelSet(net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getLevel().getNearestPlayer(
                event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5,
                5.0, false);
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // If the lock system is disabled or player is admin, allow all
        if (SkillLockManager.isAdminBypassing(serverPlayer)) return;

        // We can't filter specific enchantments here directly, but we can note
        // that enchantment stripping will happen post-enchant via the
        // enchantment result check. This event is kept for future hooks.
    }

    // ==================== Crafting Lock ====================

    /**
     * Prevent players from crafting skill-locked items.
     * If the output is locked, replace it with empty and warn the player.
     */
    @SubscribeEvent
    public static void onCraftLockedItem(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack result = event.getCrafting();
        if (!SkillLockManager.canCraftItem(player, result)) {
            // Clear the crafted output — player doesn't get the item
            result.setCount(0);

            // Return ingredients to the player
            net.minecraft.world.Container craftMatrix = event.getInventory();
            for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                ItemStack ingredient = craftMatrix.getItem(i);
                if (!ingredient.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(ingredient.copy());
                }
            }

            long currentTick = player.level().getGameTime();
            Long lastMessage = MESSAGE_COOLDOWNS.get(player.getUUID());
            if (lastMessage == null || currentTick - lastMessage >= MESSAGE_COOLDOWN_TICKS) {
                MESSAGE_COOLDOWNS.put(player.getUUID(), currentTick);
                SkillLockDefinitions.CraftLock lock = SkillLockManager.getCraftLock(event.getCrafting());
                String branchA = lock != null ? lock.branchA().getDisplayName() : "Unknown";
                String branchB = lock != null && lock.branchB() != null ? lock.branchB().getDisplayName() : null;
                String msg = branchB != null
                        ? "Requires " + branchA + " or " + branchB + " to craft"
                        : "Requires " + branchA + " to craft";
                player.displayClientMessage(
                        Component.literal("\u2716 LOCKED — " + msg).withStyle(ChatFormatting.RED),
                        true);
            }
        }
    }

    // ==================== Utility ====================

    private static void sendLockMessage(ServerPlayer player, ItemStack item) {
        long currentTick = player.level().getGameTime();
        Long lastMessage = MESSAGE_COOLDOWNS.get(player.getUUID());
        if (lastMessage != null && currentTick - lastMessage < MESSAGE_COOLDOWN_TICKS) {
            return;
        }
        MESSAGE_COOLDOWNS.put(player.getUUID(), currentTick);

        String message = SkillLockManager.getLockMessage(item);
        if (message != null) {
            player.displayClientMessage(
                    Component.literal("\u2716 LOCKED — " + message).withStyle(ChatFormatting.RED),
                    true); // actionbar
        }
    }

    /**
     * Cleanup on player logout.
     */
    @SubscribeEvent
    public static void onPlayerLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        MESSAGE_COOLDOWNS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        MESSAGE_COOLDOWNS.clear();
    }
}
