package com.ultra.megamod.feature.computer.admin;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Event hooks for admin features: death logging, vanish, custom loot drops.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AdminFeatureEvents {

    private static final Random RANDOM = new Random();

    // ======================== DEATH LOGGING ========================

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ServerLevel level = (ServerLevel) player.level();

        // Capture inventory before it drops
        List<DeathLogManager.DeathItem> items = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            items.add(new DeathLogManager.DeathItem(
                    itemId, stack.getHoverName().getString(), stack.getCount(), i));
        }

        // Build cause string
        DamageSource source = event.getSource();
        String cause = source.getLocalizedDeathMessage(player).getString();

        String dim = player.level().dimension().identifier().toString();

        DeathLogManager.get(level).addDeath(
                player.getUUID(), player.getGameProfile().name(),
                player.getX(), player.getY(), player.getZ(),
                dim, cause, items);
        DeathLogManager.get(level).saveToDisk(level);
    }

    // ======================== VANISH ========================

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Hide vanished players from this new joiner
        VanishManager.onPlayerJoin(player);

        // Give admin players a linked phone if they don't already have one
        if (AdminSystem.isAdmin(player)) {
            giveAdminPhone(player);
        }
    }

    /**
     * Gives admin a phone pre-linked to the nearest computer, or unlinked if none found.
     * Only gives if the admin doesn't already have a phone in their inventory.
     */
    private static void giveAdminPhone(ServerPlayer player) {
        // Check if player already has a phone
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack existing = player.getInventory().getItem(i);
            if (!existing.isEmpty() && existing.getItem() == com.ultra.megamod.feature.computer.ComputerRegistry.PHONE_ITEM.get()) {
                return; // Already has a phone
            }
        }

        ItemStack phone = new ItemStack(com.ultra.megamod.feature.computer.ComputerRegistry.PHONE_ITEM.get());

        // Try to find the nearest computer block to auto-link
        ServerLevel level = (ServerLevel) player.level();
        net.minecraft.core.BlockPos playerPos = player.blockPosition();
        net.minecraft.core.BlockPos nearestComputer = null;
        double nearestDist = Double.MAX_VALUE;

        // Search in a 32-block radius for a computer
        for (int dx = -32; dx <= 32; dx++) {
            for (int dy = -16; dy <= 16; dy++) {
                for (int dz = -32; dz <= 32; dz++) {
                    net.minecraft.core.BlockPos checkPos = playerPos.offset(dx, dy, dz);
                    if (level.getBlockState(checkPos).getBlock() instanceof com.ultra.megamod.feature.computer.ComputerBlock) {
                        double dist = checkPos.distSqr(playerPos);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearestComputer = checkPos;
                        }
                    }
                }
            }
        }

        if (nearestComputer != null) {
            // Link the phone to the nearest computer
            net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
            tag.putInt("linked_x", nearestComputer.getX());
            tag.putInt("linked_y", nearestComputer.getY());
            tag.putInt("linked_z", nearestComputer.getZ());
            tag.putString("linked_dim", level.dimension().identifier().toString());
            phone.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(tag));
            phone.set(net.minecraft.core.component.DataComponents.CUSTOM_MODEL_DATA,
                    new net.minecraft.world.item.component.CustomModelData(
                            java.util.List.of(), java.util.List.of(true), java.util.List.of(), java.util.List.of()));

            player.getInventory().add(phone);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Phone linked to computer at " + nearestComputer.getX() + ", "
                    + nearestComputer.getY() + ", " + nearestComputer.getZ())
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
        } else {
            // Give unlinked phone
            player.getInventory().add(phone);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Phone given (unlinked). Right-click a Computer to link it.")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        VanishManager.onPlayerDisconnect(player.getUUID());
    }

    // ======================== CUSTOM LOOT DROPS ========================

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onMobDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity instanceof ServerPlayer) return; // Don't add custom drops to players

        ServerLevel level = (ServerLevel) entity.level();
        String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();

        List<AdminLootManager.LootDrop> drops = AdminLootManager.get(level).getDropsForMob(mobId);
        if (drops.isEmpty()) return;

        for (AdminLootManager.LootDrop drop : drops) {
            if (RANDOM.nextDouble() > drop.chance()) continue;

            var item = BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(drop.itemId()));
            if (item == null || item == Items.AIR) continue;

            int count = drop.minCount();
            if (drop.maxCount() > drop.minCount()) {
                count = drop.minCount() + RANDOM.nextInt(drop.maxCount() - drop.minCount() + 1);
            }

            ItemStack stack = new ItemStack(item, count);
            entity.spawnAtLocation(level, stack);
        }
    }

    // ======================== SERVER STOP ========================

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        if (level == null) return;
        DeathLogManager.get(level).saveToDisk(level);
        AdminLootManager.get(level).saveToDisk(level);
        CommandAliasManager.get(level).saveToDisk(level);
    }
}
