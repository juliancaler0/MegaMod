package com.ultra.megamod.feature.adminmodules.modules.player;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Consumer;

public class PlayerModules {

    public static void register(Consumer<AdminModule> reg) {
        reg.accept(new AutoTool());
        reg.accept(new AutoClicker());
        reg.accept(new FastUse());
        reg.accept(new SpeedMine());
        reg.accept(new AntiHunger());
        reg.accept(new AutoReplenish());
        reg.accept(new FakePlayer());
        reg.accept(new FastPlace());
        reg.accept(new AutoDrop());
        reg.accept(new NoBreakDelay());
        reg.accept(new AutoMend());
        reg.accept(new AutoGap());
        reg.accept(new PortalGodMode());
        reg.accept(new ReachExtend());
        reg.accept(new MiddleClickFriend());
        reg.accept(new AutoCraft());
        reg.accept(new AutoTrade());
        reg.accept(new InventorySort());
        reg.accept(new PacketMine());
        reg.accept(new GhostHand());
        reg.accept(new AutoSign());
        reg.accept(new NoInteract());
        reg.accept(new AntiCactus());
        reg.accept(new AutoRespawn());
        reg.accept(new AutoWalkPlayer());
        reg.accept(new AutoSwitch());
        reg.accept(new InstantMine());
        reg.accept(new AutoBuild());
        reg.accept(new SmartMine());
        // New Meteor-inspired modules
        reg.accept(new ChestSwap());
        reg.accept(new EXPThrower());
        reg.accept(new Multitask());
        reg.accept(new PotionSaver());
        reg.accept(new Rotation());
        // New Meteor-inspired modules (batch 2)
        reg.accept(new BreakDelay());
        reg.accept(new LiquidInteract());
        reg.accept(new NameProtect());
        reg.accept(new NoMiningTrace());
        reg.accept(new NoRotate());
        reg.accept(new NoStatusEffects());
        reg.accept(new OffhandCrash());
        reg.accept(new Portals());
        reg.accept(new PortableCrafting());
    }

    // ---------------------------------------------------------------
    // AutoTool — every 4 ticks, raycast to find looked-at block and
    //            switch hotbar to the tool that mines it fastest.
    // ---------------------------------------------------------------
    static class AutoTool extends AdminModule {
        int tick = 0;
        AutoTool() { super("auto_tool", "AutoTool", "Switches to best tool for block", ModuleCategory.PLAYER); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 4 != 0) return;
            HitResult hit = player.pick(5.0, 0, false);
            if (hit.getType() != HitResult.Type.BLOCK) return;
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            if (state.isAir()) return;

            int bestSlot = -1;
            float bestSpeed = 1.0f;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                float speed = stack.getDestroySpeed(state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
            if (bestSlot >= 0) {
                ItemStack bestItem = player.getInventory().getItem(bestSlot);
                if (!ItemStack.isSameItemSameComponents(bestItem, player.getMainHandItem())) {
                    swapToSlot(player, bestSlot);
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // AutoClicker — auto-attack the entity being looked at.
    //               CPS setting controls frequency.
    // ---------------------------------------------------------------
    static class AutoClicker extends AdminModule {
        private ModuleSetting.IntSetting cps;
        int tick = 0;
        AutoClicker() { super("auto_clicker", "AutoClicker", "Auto left/right clicks", ModuleCategory.PLAYER); }
        @Override protected void initSettings() { cps = integer("CPS", 10, 1, 20, "Clicks per second"); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            tick++;
            int ticksPerClick = Math.max(1, 20 / cps.getValue());
            if (tick % ticksPerClick != 0) return;

            // Scan for entities along the look direction (6 block range)
            Vec3 eye = player.getEyePosition(0);
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(6.0));
            AABB searchBox = player.getBoundingBox().expandTowards(look.scale(6.0)).inflate(1.0);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != player && e.isAlive());

            LivingEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity e : nearby) {
                // Skip friends (from MiddleClickFriend module)
                if (e instanceof Player p && MiddleClickFriend.isFriend(p.getUUID())) continue;
                AABB ebb = e.getBoundingBox().inflate(0.3);
                if (ebb.clip(eye, end).isPresent()) {
                    double d = player.distanceTo(e);
                    if (d < closestDist) {
                        closestDist = d;
                        closest = e;
                    }
                }
            }
            if (closest != null) {
                player.attack(closest);
            }
        }
    }

    // ---------------------------------------------------------------
    // FastUse — apply Haste II to speed up use animations.
    // ---------------------------------------------------------------
    static class FastUse extends AdminModule {
        int tick = 0;
        FastUse() { super("fast_use", "FastUse", "No delay between item uses", ModuleCategory.PLAYER); }
        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only refresh effect every 15 ticks (Haste lasts 20 ticks)
            if (++tick % 15 != 0) return;
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20, 1, false, false));
        }
    }

    // ---------------------------------------------------------------
    // SpeedMine — multiplies break speed via event.
    // ---------------------------------------------------------------
    static class SpeedMine extends AdminModule {
        private ModuleSetting.DoubleSetting multiplier;
        SpeedMine() { super("speed_mine", "SpeedMine", "Faster block breaking speed", ModuleCategory.PLAYER); }
        @Override protected void initSettings() { multiplier = decimal("Multiplier", 2.0, 1.0, 10.0, "Break speed multiplier"); }
        @Override public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
            event.setNewSpeed(event.getOriginalSpeed() * multiplier.getValue().floatValue());
        }
    }

    // ---------------------------------------------------------------
    // AntiHunger — keeps food and saturation maxed.
    // ---------------------------------------------------------------
    static class AntiHunger extends AdminModule {
        int tick = 0;
        AntiHunger() { super("anti_hunger", "AntiHunger", "Prevents hunger depletion", ModuleCategory.PLAYER); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only check every 10 ticks to reduce network overhead
            if (++tick % 10 != 0) return;
            if (player.getFoodData().getFoodLevel() < 20) {
                player.getFoodData().setFoodLevel(20);
            }
            if (player.getFoodData().getSaturationLevel() < 20.0f) {
                player.getFoodData().setSaturation(20.0f);
            }
        }
    }

    // ---------------------------------------------------------------
    // AutoReplenish — refills hotbar stacks from inventory.
    // ---------------------------------------------------------------
    static class AutoReplenish extends AdminModule {
        AutoReplenish() { super("auto_replenish", "AutoReplenish", "Refills hotbar from inventory", ModuleCategory.PLAYER); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            for (int i = 0; i < 9; i++) {
                ItemStack hotbar = player.getInventory().getItem(i);
                if (hotbar.isEmpty()) continue;
                if (hotbar.getCount() < hotbar.getMaxStackSize()) {
                    for (int j = 9; j < player.getInventory().getContainerSize(); j++) {
                        ItemStack inv = player.getInventory().getItem(j);
                        if (ItemStack.isSameItemSameComponents(hotbar, inv)) {
                            int toMove = Math.min(inv.getCount(), hotbar.getMaxStackSize() - hotbar.getCount());
                            hotbar.grow(toMove);
                            inv.shrink(toMove);
                            if (inv.isEmpty()) player.getInventory().setItem(j, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // FakePlayer — on enable, spawns an armor stand wearing copies
    //              of the player's equipment. One-shot.
    // ---------------------------------------------------------------
    static class FakePlayer extends AdminModule {
        private ModuleSetting.BoolSetting copyArmor;
        private ModuleSetting.BoolSetting copyHeld;
        FakePlayer() { super("fake_player", "FakePlayer", "Spawns a fake player entity", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            copyArmor = bool("CopyArmor", true, "Copy armor to decoy");
            copyHeld = bool("CopyHeld", true, "Copy held items to decoy");
        }

        @Override
        public void onEnable(ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            ArmorStand stand = new ArmorStand(level, player.getX(), player.getY(), player.getZ());
            stand.setShowArms(true);
            stand.setNoGravity(false);
            stand.setYRot(player.getYRot());
            // Copy armor
            if (copyArmor.getValue()) {
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    ItemStack armor = player.getItemBySlot(slot);
                    if (!armor.isEmpty()) {
                        stand.setItemSlot(slot, armor.copy());
                    }
                }
            }
            // Copy held items
            if (copyHeld.getValue()) {
                ItemStack mainhand = player.getMainHandItem();
                if (!mainhand.isEmpty()) {
                    stand.setItemSlot(EquipmentSlot.MAINHAND, mainhand.copy());
                }
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    stand.setItemSlot(EquipmentSlot.OFFHAND, offhand.copy());
                }
            }
            stand.setCustomName(player.getDisplayName());
            stand.setCustomNameVisible(true);
            level.addFreshEntity(stand);
            // Auto-disable after placing
            setEnabled(false);
        }
    }

    // ---------------------------------------------------------------
    // FastPlace — apply Haste I for faster placement feel.
    // ---------------------------------------------------------------
    static class FastPlace extends AdminModule {
        int tick = 0;
        FastPlace() { super("fast_place", "FastPlace", "No delay between block placements", ModuleCategory.PLAYER); }
        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only refresh effect every 15 ticks (Haste lasts 20 ticks)
            if (++tick % 15 != 0) return;
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 20, 0, false, false));
        }
    }

    // ---------------------------------------------------------------
    // AutoDrop — every 40 ticks, scan inventory and drop junk items.
    // ---------------------------------------------------------------
    static class AutoDrop extends AdminModule {
        private ModuleSetting.BoolSetting dropCobble;
        private ModuleSetting.BoolSetting dropDirt;
        private ModuleSetting.BoolSetting dropGravel;
        private ModuleSetting.BoolSetting dropFlesh;
        int tick = 0;
        AutoDrop() { super("auto_drop", "AutoDrop", "Auto-drops specific items", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            dropCobble = bool("DropCobble", true, "Drop cobblestone and variants");
            dropDirt = bool("DropDirt", true, "Drop dirt");
            dropGravel = bool("DropGravel", true, "Drop gravel");
            dropFlesh = bool("DropFlesh", true, "Drop rotten flesh");
        }

        private boolean shouldDrop(ItemStack stack) {
            Item item = stack.getItem();
            if (dropCobble.getValue() && (item == Items.COBBLESTONE || item == Items.COBBLED_DEEPSLATE
                    || item == Items.ANDESITE || item == Items.DIORITE || item == Items.GRANITE || item == Items.TUFF)) return true;
            if (dropDirt.getValue() && item == Items.DIRT) return true;
            if (dropGravel.getValue() && item == Items.GRAVEL) return true;
            if (dropFlesh.getValue() && (item == Items.ROTTEN_FLESH || item == Items.POISONOUS_POTATO || item == Items.SPIDER_EYE)) return true;
            return false;
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && shouldDrop(stack)) {
                    player.drop(stack.copy(), false);
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // NoBreakDelay — multiply break speed by 3x via event.
    // ---------------------------------------------------------------
    static class NoBreakDelay extends AdminModule {
        NoBreakDelay() { super("no_break_delay", "NoBreakDelay", "Removes block break cooldown", ModuleCategory.PLAYER); }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            com.ultra.megamod.feature.adminmodules.AdminModuleState.noBreakDelayEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            com.ultra.megamod.feature.adminmodules.AdminModuleState.noBreakDelayEnabled = false;
        }
        @Override
        public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
            event.setNewSpeed(event.getOriginalSpeed() * 3.0f);
        }
    }

    // ---------------------------------------------------------------
    // AutoMend — every 60 ticks, repair 10 durability on equipment.
    // ---------------------------------------------------------------
    static class AutoMend extends AdminModule {
        private ModuleSetting.IntSetting repairAmount;
        AutoMend() { super("auto_mend", "AutoMend", "Auto-repairs items with XP", ModuleCategory.PLAYER); }
        int tick = 0;
        @Override protected void initSettings() {
            repairAmount = integer("RepairAmount", 10, 1, 50, "Durability restored per cycle");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 60 != 0) return;
            int repair = repairAmount.getValue();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = player.getItemBySlot(slot);
                if (!item.isEmpty() && item.isDamaged()) {
                    item.setDamageValue(Math.max(0, item.getDamageValue() - repair));
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // AutoGap — when low HP, apply regen + absorption (golden apple effect).
    // ---------------------------------------------------------------
    static class AutoGap extends AdminModule {
        private ModuleSetting.DoubleSetting healthThreshold;
        int tick = 0;
        AutoGap() { super("auto_gap", "AutoGap", "Auto-eats golden apples when low HP", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            healthThreshold = decimal("HealthThreshold", 10.0, 1.0, 19.0, "HP to trigger");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only check every 20 ticks to avoid spamming addEffect packets
            if (++tick % 20 != 0) return;
            if (player.getHealth() < healthThreshold.getValue().floatValue()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0, false, false));
            }
        }
    }

    // ---------------------------------------------------------------
    // PortalGodMode — cancel all damage while inside a portal.
    // ---------------------------------------------------------------
    static class PortalGodMode extends AdminModule {
        private ModuleSetting.EnumSetting mode;
        PortalGodMode() { super("portal_god_mode", "PortalGodMode", "Full invulnerability (cancels all damage)", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            mode = enumVal("Mode", "All", List.of("All", "PortalOnly"), "All=block all damage, PortalOnly=only while in portal");
        }
        @Override
        public void onDamage(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event) {
            if ("PortalOnly".equals(mode.getValue())) {
                // Only cancel damage if the player is actively in a portal transition
                // Use isOnPortalCooldown() as a reliable indicator of recent portal use
                if (player.isOnPortalCooldown()) {
                    event.setNewDamage(0);
                }
            } else {
                // Cancel all damage
                event.setNewDamage(0);
            }
        }
    }

    // ---------------------------------------------------------------
    // ReachExtend — scan for entities within extended reach and
    //               attack the closest one every few ticks.
    // ---------------------------------------------------------------
    static class ReachExtend extends AdminModule {
        private ModuleSetting.DoubleSetting reach;
        int tick = 0;
        ReachExtend() { super("reach_extend", "ReachExtend", "Extends interaction reach distance", ModuleCategory.PLAYER); }
        @Override protected void initSettings() { reach = decimal("Reach", 8.0, 5.0, 12.0, "Reach distance in blocks"); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 4 != 0) return;
            double r = reach.getValue();
            Vec3 eye = player.getEyePosition(0);
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(r));
            AABB searchBox = player.getBoundingBox().expandTowards(look.scale(r)).inflate(1.0);
            List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != player && e.isAlive());

            LivingEntity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (LivingEntity e : nearby) {
                AABB ebb = e.getBoundingBox().inflate(0.3);
                if (ebb.clip(eye, end).isPresent()) {
                    double d = player.distanceTo(e);
                    if (d < closestDist && d <= r) {
                        closestDist = d;
                        closest = e;
                    }
                }
            }
            // Only attack if beyond normal reach
            if (closest != null && closestDist > 4.5) {
                player.attack(closest);
            }
        }
    }

    // ---------------------------------------------------------------
    // MiddleClickFriend — nearby players within 4 blocks are added to
    //                     a friend set. Friends are excluded from KillAura etc.
    // ---------------------------------------------------------------
    public static class MiddleClickFriend extends AdminModule {
        public static final Set<UUID> FRIENDS = new HashSet<>();
        private ModuleSetting.IntSetting friendRange;
        int tick = 0;
        MiddleClickFriend() { super("middle_click_friend", "MiddleClickFriend", "Auto-friends nearby sneaking players (KillAura/AutoClicker skip friends)", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            friendRange = integer("Range", 4, 1, 10, "Detection range for sneaking players");
        }

        /**
         * Check if a UUID is in the friends set. Used by KillAura, AutoClicker, etc.
         */
        public static boolean isFriend(UUID uuid) {
            return FRIENDS.contains(uuid);
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            // Auto-friend nearby players within range that are sneaking (as a "request")
            int r = friendRange.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<Player> nearby = level.getEntitiesOfClass(Player.class, box, p -> p != player && p.isAlive());
            for (Player p : nearby) {
                if (p.isShiftKeyDown()) {
                    FRIENDS.add(p.getUUID());
                }
            }
        }

        @Override
        public void onDisable(ServerPlayer player) {
            FRIENDS.clear();
        }
    }

    // ---------------------------------------------------------------
    // AutoCraft — every 60 ticks, auto-craft torches from coal + sticks.
    //             Also crafts sticks from planks, planks from logs.
    // ---------------------------------------------------------------
    static class AutoCraft extends AdminModule {
        private ModuleSetting.BoolSetting craftTorches;
        private ModuleSetting.BoolSetting craftSticks;
        private ModuleSetting.BoolSetting craftPlanks;
        int tick = 0;
        AutoCraft() { super("auto_craft", "AutoCraft", "Automatically crafts items", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            craftTorches = bool("Torches", true, "Craft torches from coal+sticks");
            craftSticks = bool("Sticks", true, "Craft sticks from planks");
            craftPlanks = bool("Planks", true, "Craft planks from logs");
        }

        // Log items that can be converted to planks
        private static final Item[] LOG_ITEMS = {
                Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG,
                Items.ACACIA_LOG, Items.DARK_OAK_LOG, Items.MANGROVE_LOG, Items.CHERRY_LOG,
                Items.CRIMSON_STEM, Items.WARPED_STEM
        };
        // Corresponding plank outputs
        private static final Item[] PLANK_ITEMS = {
                Items.OAK_PLANKS, Items.SPRUCE_PLANKS, Items.BIRCH_PLANKS, Items.JUNGLE_PLANKS,
                Items.ACACIA_PLANKS, Items.DARK_OAK_PLANKS, Items.MANGROVE_PLANKS, Items.CHERRY_PLANKS,
                Items.CRIMSON_PLANKS, Items.WARPED_PLANKS
        };

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 60 != 0) return;

            // Craft planks from logs: 1 log -> 4 planks
            if (craftPlanks.getValue()) {
                for (int li = 0; li < LOG_ITEMS.length; li++) {
                    int logSlot = findItem(player, LOG_ITEMS[li]);
                    if (logSlot >= 0) {
                        consumeFromSlot(player, logSlot, 1);
                        player.getInventory().add(new ItemStack(PLANK_ITEMS[li], 4));
                        break; // One craft per cycle
                    }
                }
            }

            // Craft sticks: 2 planks -> 4 sticks (from any plank type)
            if (craftSticks.getValue()) {
                int stickSlot = findItem(player, Items.STICK);
                if (stickSlot < 0) { // Only craft if no sticks exist
                    for (Item plank : PLANK_ITEMS) {
                        int plankSlot = findItemCount(player, plank, 2);
                        if (plankSlot >= 0) {
                            consumeFromSlot(player, plankSlot, 2);
                            player.getInventory().add(new ItemStack(Items.STICK, 4));
                            break;
                        }
                    }
                    // Also check oak planks specifically (backward compat)
                    // Already covered in PLANK_ITEMS loop above
                }
            }

            // Craft torches: 1 coal/charcoal + 1 stick -> 4 torches
            if (craftTorches.getValue()) {
                int stickSlot = findItem(player, Items.STICK);
                if (stickSlot >= 0) {
                    int coalSlot = findItem(player, Items.COAL);
                    if (coalSlot < 0) coalSlot = findItem(player, Items.CHARCOAL);
                    if (coalSlot >= 0) {
                        consumeFromSlot(player, coalSlot, 1);
                        consumeFromSlot(player, stickSlot, 1);
                        player.getInventory().add(new ItemStack(Items.TORCH, 4));
                    }
                }
            }
        }

        private void consumeFromSlot(ServerPlayer player, int slot, int amount) {
            ItemStack stack = player.getInventory().getItem(slot);
            stack.shrink(amount);
            if (stack.isEmpty()) player.getInventory().setItem(slot, ItemStack.EMPTY);
        }

        private int findItem(ServerPlayer player, Item item) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(item)) return i;
            }
            return -1;
        }

        private int findItemCount(ServerPlayer player, Item item, int minCount) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(item) && stack.getCount() >= minCount) return i;
            }
            return -1;
        }
    }

    // ---------------------------------------------------------------
    // AutoTrade — every 100 ticks, find nearest villager within 3 blocks
    //             and simulate a trade (give emeralds + XP).
    // ---------------------------------------------------------------
    static class AutoTrade extends AdminModule {
        int tick = 0;
        AutoTrade() { super("auto_trade", "AutoTrade", "Auto-trades with villagers", ModuleCategory.PLAYER); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;
            AABB box = player.getBoundingBox().inflate(3.0);
            List<Villager> villagers = level.getEntitiesOfClass(Villager.class, box, Entity::isAlive);
            if (villagers.isEmpty()) return;
            // Simulate a basic trade reward
            player.getInventory().add(new ItemStack(Items.EMERALD, 1));
            player.giveExperiencePoints(5);
        }
    }

    // ---------------------------------------------------------------
    // InventorySort — every 200 ticks, sort inventory slots 9-35 by
    //                 item ID alphabetically.
    // ---------------------------------------------------------------
    static class InventorySort extends AdminModule {
        private ModuleSetting.IntSetting interval;
        int tick = 0;
        InventorySort() { super("inventory_sort", "InventorySort", "Sorts inventory automatically", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            interval = integer("Interval", 200, 20, 600, "Ticks between sorts");
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % interval.getValue() != 0) return;

            // Collect all non-empty stacks from inventory slots 9-35
            List<ItemStack> items = new ArrayList<>();
            for (int i = 9; i < 36; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    items.add(stack.copy());
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }

            // Sort by item registry name alphabetically
            items.sort((a, b) -> {
                String idA = BuiltInRegistries.ITEM.getKey(a.getItem()).toString();
                String idB = BuiltInRegistries.ITEM.getKey(b.getItem()).toString();
                int cmp = idA.compareTo(idB);
                if (cmp != 0) return cmp;
                return Integer.compare(b.getCount(), a.getCount()); // Larger stacks first
            });

            // Merge same-type stacks
            List<ItemStack> merged = new ArrayList<>();
            for (ItemStack stack : items) {
                boolean didMerge = false;
                for (ItemStack existing : merged) {
                    if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                        int toMove = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                        existing.grow(toMove);
                        stack.shrink(toMove);
                        if (stack.isEmpty()) { didMerge = true; break; }
                    }
                }
                if (!didMerge && !stack.isEmpty()) {
                    merged.add(stack);
                }
            }

            // Place sorted items back
            int slot = 9;
            for (ItemStack stack : merged) {
                if (slot >= 36) break;
                player.getInventory().setItem(slot++, stack);
            }
        }
    }

    // ---------------------------------------------------------------
    // PacketMine — set break speed to 999 (near-instant mine).
    // ---------------------------------------------------------------
    static class PacketMine extends AdminModule {
        private ModuleSetting.DoubleSetting multiplier;
        PacketMine() { super("packet_mine", "PacketMine", "Near-instant mining (999x speed, lighter than InstantMine)", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            multiplier = decimal("Multiplier", 999.0, 100.0, 9999.0, "Break speed value (lower = less disruptive)");
        }
        @Override
        public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
            event.setNewSpeed(multiplier.getValue().floatValue());
        }
    }

    // ---------------------------------------------------------------
    // GhostHand — every 5 ticks, place cobblestone 2 blocks past
    //             the first solid block in look direction.
    // ---------------------------------------------------------------
    static class GhostHand extends AdminModule {
        private ModuleSetting.IntSetting ghostRange;
        int tick = 0;
        GhostHand() { super("ghost_hand", "GhostHand", "Interact with blocks through walls", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            ghostRange = integer("Range", 2, 1, 5, "Blocks behind wall to place");
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            Vec3 eye = player.getEyePosition(0);
            Vec3 look = player.getLookAngle();

            // Find the first solid block
            HitResult hit = player.pick(5.0, 0, false);
            if (hit.getType() != HitResult.Type.BLOCK) return;
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos solidPos = blockHit.getBlockPos();

            // Go N blocks further in look direction past the solid block
            Vec3 center = Vec3.atCenterOf(solidPos);
            Vec3 behind = center.add(look.scale(ghostRange.getValue()));
            BlockPos targetPos = BlockPos.containing(behind);

            if (level.getBlockState(targetPos).isAir()) {
                // Check if player has cobblestone in inventory
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(Items.COBBLESTONE)) {
                        level.setBlock(targetPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                        stack.shrink(1);
                        if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // AutoSign — find nearby sign block entities within 3 blocks and
    //            apply text to their front.
    // ---------------------------------------------------------------
    static class AutoSign extends AdminModule {
        private ModuleSetting.EnumSetting text;
        int tick = 0;
        private final Set<BlockPos> writtenSigns = new HashSet<>();
        AutoSign() { super("auto_sign", "AutoSign", "Auto-fills signs with text (each sign written once)", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            text = enumVal("Text", "MegaMod", List.of("MegaMod", "Admin", "Warning", "Keep Out", "Welcome"), "Text to write on signs");
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            BlockPos center = player.blockPosition();
            for (int x = -3; x <= 3; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -3; z <= 3; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (writtenSigns.contains(pos)) continue;
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof SignBlockEntity sign) {
                            writtenSigns.add(pos.immutable());
                            String msg = text.getValue();
                            // Use server command to set sign text
                            String cmd = String.format("data merge block %d %d %d {front_text:{messages:['{\"text\":\"%s\"}','{\"text\":\"\"}','{\"text\":\"\"}','{\"text\":\"\"}']}}",
                                    pos.getX(), pos.getY(), pos.getZ(), msg);
                            level.getServer().getCommands().performPrefixedCommand(
                                    player.createCommandSourceStack().withSuppressedOutput(), cmd);
                        }
                    }
                }
            }
        }

        @Override
        public void onDisable(ServerPlayer player) {
            writtenSigns.clear();
        }
    }

    // ---------------------------------------------------------------
    // NoInteract — close any open container menu to prevent
    //              accidental container interactions. Checks every 5 ticks.
    // ---------------------------------------------------------------
    static class NoInteract extends AdminModule {
        int tick = 0;
        NoInteract() { super("no_interact", "NoInteract", "Prevents accidental container interactions (auto-closes menus)", ModuleCategory.PLAYER); }
        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Check every 5 ticks instead of every tick to reduce overhead
            if (++tick % 5 != 0) return;
            if (player.containerMenu != player.inventoryMenu) {
                player.closeContainer();
            }
        }
    }

    // ---------------------------------------------------------------
    // AntiCactus — cancel cactus damage.
    // ---------------------------------------------------------------
    static class AntiCactus extends AdminModule {
        AntiCactus() { super("anti_cactus", "AntiCactus", "Prevents cactus damage", ModuleCategory.PLAYER); }
        @Override public void onDamage(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event) {
            if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.CACTUS)) {
                event.setNewDamage(0);
            }
        }
    }

    // ---------------------------------------------------------------
    // AutoRespawn — if the player is dead, respawn them immediately.
    // ---------------------------------------------------------------
    static class AutoRespawn extends AdminModule {
        private ModuleSetting.IntSetting respawnDelay;
        private int deathTicks = 0;
        AutoRespawn() { super("auto_respawn", "AutoRespawn", "Auto-respawns after death", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            respawnDelay = integer("Delay", 5, 1, 40, "Ticks after death before respawn");
        }
        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.isDeadOrDying()) {
                deathTicks++;
                if (deathTicks >= respawnDelay.getValue()) {
                    // Use the proper server respawn mechanism.
                    // NOTE: respawn() returns a NEW ServerPlayer instance. The old 'player'
                    // reference becomes stale. The module system must re-resolve the player
                    // by UUID on next tick. This is handled by the module tick dispatcher
                    // which gets players from the server's player list each tick.
                    try {
                        level.getServer().getPlayerList().respawn(player, false, Entity.RemovalReason.KILLED);
                    } catch (Exception e) {
                        // Fallback: force alive state if respawn method signature changed
                        player.setHealth(player.getMaxHealth());
                        player.getFoodData().setFoodLevel(20);
                        player.getFoodData().setSaturation(5.0f);
                        player.removeAllEffects();
                    }
                    deathTicks = 0;
                }
            } else {
                deathTicks = 0;
            }
        }
    }

    // ---------------------------------------------------------------
    // AutoWalkPlayer — apply forward velocity along look direction.
    // ---------------------------------------------------------------
    static class AutoWalkPlayer extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        private ModuleSetting.EnumSetting direction;
        AutoWalkPlayer() { super("auto_walk_player", "AutoWalkPlayer", "Player walks automatically", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 0.2, 0.05, 1.0, "Walk speed");
            direction = enumVal("Direction", "Forward", List.of("Forward", "Backward", "Left", "Right"), "Walk direction relative to look");
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            Vec3 look = player.getLookAngle();
            double spd = speed.getValue();
            Vec3 current = player.getDeltaMovement();

            double moveX, moveZ;
            switch (direction.getValue()) {
                case "Backward" -> {
                    moveX = -look.x * spd;
                    moveZ = -look.z * spd;
                }
                case "Left" -> {
                    // Rotate look 90 degrees counterclockwise
                    moveX = look.z * spd;
                    moveZ = -look.x * spd;
                }
                case "Right" -> {
                    // Rotate look 90 degrees clockwise
                    moveX = -look.z * spd;
                    moveZ = look.x * spd;
                }
                default -> { // Forward
                    moveX = look.x * spd;
                    moveZ = look.z * spd;
                }
            }
            player.setDeltaMovement(moveX, current.y, moveZ);
            player.hurtMarked = true;
        }
    }

    // ---------------------------------------------------------------
    // AutoSwitch — context-aware tool switching. If looking at mob,
    //              switch to sword. If looking at block, switch to
    //              best tool. Every 4 ticks.
    // ---------------------------------------------------------------
    static class AutoSwitch extends AdminModule {
        int tick = 0;
        AutoSwitch() { super("auto_switch", "AutoSwitch", "Auto-switches tools per task", ModuleCategory.PLAYER); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 4 != 0) return;

            // Check entity first (sword priority)
            Vec3 eye = player.getEyePosition(0);
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(5.0));
            AABB searchBox = player.getBoundingBox().expandTowards(look.scale(5.0)).inflate(1.0);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != player && e.isAlive());

            boolean entityInSight = false;
            for (LivingEntity e : entities) {
                if (e.getBoundingBox().inflate(0.3).clip(eye, end).isPresent()) {
                    entityInSight = true;
                    break;
                }
            }

            if (entityInSight) {
                // Switch to best weapon: prefer swords, then axes
                int swordSlot = -1;
                int axeSlot = -1;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("sword") && swordSlot < 0) {
                        swordSlot = i;
                    } else if (stack.getItem() instanceof AxeItem && axeSlot < 0) {
                        axeSlot = i;
                    }
                }
                int bestSlot = swordSlot >= 0 ? swordSlot : axeSlot;
                if (bestSlot >= 0) {
                    ItemStack bestItem = player.getInventory().getItem(bestSlot);
                    if (!ItemStack.isSameItemSameComponents(bestItem, player.getMainHandItem())) {
                        swapToSlot(player, bestSlot);
                    }
                }
            } else {
                // Check block — switch to best tool
                HitResult hit = player.pick(5.0, 0, false);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHit = (BlockHitResult) hit;
                    BlockState state = level.getBlockState(blockHit.getBlockPos());
                    if (!state.isAir()) {
                        int bestSlot = -1;
                        float bestSpeed = 1.0f;
                        for (int i = 0; i < 9; i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            float spd = stack.getDestroySpeed(state);
                            if (spd > bestSpeed) {
                                bestSpeed = spd;
                                bestSlot = i;
                            }
                        }
                        if (bestSlot >= 0) {
                            ItemStack bestItem = player.getInventory().getItem(bestSlot);
                            if (!ItemStack.isSameItemSameComponents(bestItem, player.getMainHandItem())) {
                                swapToSlot(player, bestSlot);
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // InstantMine — break speed set to 9999.
    // ---------------------------------------------------------------
    static class InstantMine extends AdminModule {
        InstantMine() { super("instant_mine", "InstantMine", "True instant-mine (9999x speed, breaks any block in 1 tick)", ModuleCategory.PLAYER); }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            com.ultra.megamod.feature.adminmodules.AdminModuleState.instantMineEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            com.ultra.megamod.feature.adminmodules.AdminModuleState.instantMineEnabled = false;
        }
        @Override public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
            event.setNewSpeed(9999.0f);
        }
    }

    // ---------------------------------------------------------------
    // AutoBuild — auto-extend blocks in movement direction. Places a
    //             3-wide path of blocks ahead and below. Every 2 ticks.
    // ---------------------------------------------------------------
    static class AutoBuild extends AdminModule {
        private ModuleSetting.IntSetting width;
        int tick = 0;
        AutoBuild() { super("auto_build", "AutoBuild", "Auto-places building schematics", ModuleCategory.PLAYER); }
        @Override protected void initSettings() { width = integer("Width", 3, 1, 5, "Path width"); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return;

            float yaw = player.getYRot();
            int dx = 0, dz = 0;
            // Determine primary movement direction from yaw
            if (yaw >= -45 && yaw < 45) dz = 1;       // South
            else if (yaw >= 45 && yaw < 135) dx = -1;  // West
            else if (yaw >= -135 && yaw < -45) dx = 1;  // East
            else dz = -1;                                // North

            BlockPos base = player.blockPosition().below();
            int w = width.getValue();
            int halfW = w / 2;

            // Perpendicular direction for width
            int px = (dz != 0) ? 1 : 0;
            int pz = (dx != 0) ? 1 : 0;

            // Place blocks 1-2 blocks ahead, width-wide
            for (int ahead = 1; ahead <= 2; ahead++) {
                for (int side = -halfW; side <= halfW; side++) {
                    BlockPos pos = base.offset(dx * ahead + px * side, 0, dz * ahead + pz * side);
                    if (level.getBlockState(pos).isAir()) {
                        // Find a block in inventory to place
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                                level.setBlock(pos, blockItem.getBlock().defaultBlockState(), 3);
                                stack.shrink(1);
                                if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // SmartMine — context-aware: ore = 5x speed, stone = 3x, normal.
    // ---------------------------------------------------------------
    static class SmartMine extends AdminModule {
        private ModuleSetting.DoubleSetting oreMulti;
        private ModuleSetting.DoubleSetting woodMulti;
        private ModuleSetting.DoubleSetting stoneMulti;
        private ModuleSetting.DoubleSetting dirtMulti;
        SmartMine() { super("smart_mine", "SmartMine", "Context-aware mining speed boosts per block type", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            oreMulti = decimal("OreSpeed", 5.0, 1.0, 20.0, "Ore speed multiplier");
            woodMulti = decimal("WoodSpeed", 4.0, 1.0, 20.0, "Wood speed multiplier");
            stoneMulti = decimal("StoneSpeed", 3.0, 1.0, 20.0, "Stone speed multiplier");
            dirtMulti = decimal("DirtSpeed", 3.0, 1.0, 20.0, "Dirt/sand/gravel speed multiplier");
        }
        @Override
        public void onBreakSpeed(ServerPlayer player, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
            BlockPos pos = event.getPosition().orElse(null);
            if (pos == null) return;
            BlockState state = player.level().getBlockState(pos);
            String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();

            if (blockId.contains("_ore") || blockId.contains("ancient_debris") || blockId.contains("gilded_blackstone")) {
                // Ore: highest priority
                event.setNewSpeed(event.getOriginalSpeed() * oreMulti.getValue().floatValue());
            } else if (blockId.contains("_log") || blockId.contains("_wood") || blockId.contains("_stem")
                    || blockId.contains("_hyphae") || blockId.contains("_planks") || blockId.contains("bamboo_block")) {
                // Wood types
                event.setNewSpeed(event.getOriginalSpeed() * woodMulti.getValue().floatValue());
            } else if (blockId.contains("stone") || blockId.contains("deepslate") || blockId.contains("netherrack")
                    || blockId.contains("basalt") || blockId.contains("tuff") || blockId.contains("calcite")
                    || blockId.contains("obsidian") || blockId.contains("end_stone") || blockId.contains("blackstone")) {
                // Stone types
                event.setNewSpeed(event.getOriginalSpeed() * stoneMulti.getValue().floatValue());
            } else if (blockId.contains("dirt") || blockId.contains("sand") || blockId.contains("gravel")
                    || blockId.contains("clay") || blockId.contains("mud") || blockId.contains("soul_soil")
                    || blockId.contains("mycelium") || blockId.contains("podzol") || blockId.contains("grass_block")) {
                // Dirt/terrain types
                event.setNewSpeed(event.getOriginalSpeed() * dirtMulti.getValue().floatValue());
            }
            // Otherwise, no modification (normal speed)
        }
    }

    // ===============================================================
    // NEW MODULES (5 Meteor-inspired additions)
    // ===============================================================

    // ---------------------------------------------------------------
    // ChestSwap — one-shot: swap all armor pieces with inventory
    //             equivalents for quick equip/dequip.
    // ---------------------------------------------------------------
    static class ChestSwap extends AdminModule {
        ChestSwap() { super("chest_swap", "ChestSwap", "Swaps armor with inventory equivalents", ModuleCategory.PLAYER); }

        @Override
        public void onEnable(ServerPlayer player) {
            EquipmentSlot[] armorSlots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

            for (EquipmentSlot slot : armorSlots) {
                ItemStack worn = player.getItemBySlot(slot);

                // Find a replacement in inventory
                int replSlot = findArmorForSlot(player, slot);
                if (replSlot >= 0) {
                    ItemStack replacement = player.getInventory().getItem(replSlot);
                    // Swap: put worn into inventory slot, equip replacement
                    player.getInventory().setItem(replSlot, worn.copy());
                    player.setItemSlot(slot, replacement.copy());
                } else if (!worn.isEmpty()) {
                    // No replacement, just unequip to inventory
                    if (player.getInventory().add(worn.copy())) {
                        player.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
            // Auto-disable after swap
            setEnabled(false);
        }

        private int findArmorForSlot(ServerPlayer player, EquipmentSlot targetSlot) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                // Check EQUIPPABLE component (used by standard armor in 1.21.11)
                if (stack.has(net.minecraft.core.component.DataComponents.EQUIPPABLE)
                    && stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE).slot() == targetSlot) {
                    return i;
                }
                // Fallback: check item's custom getEquipmentSlot (for modded items)
                EquipmentSlot slot = stack.getItem().getEquipmentSlot(stack);
                if (slot == targetSlot) {
                    return i;
                }
            }
            return -1;
        }
    }

    // ---------------------------------------------------------------
    // EXPThrower — every 20 ticks, find XP bottles in inventory,
    //              consume one, and give player 10 XP points.
    // ---------------------------------------------------------------
    static class EXPThrower extends AdminModule {
        int tick = 0;
        EXPThrower() { super("exp_thrower", "EXPThrower", "Auto-uses XP bottles from inventory", ModuleCategory.PLAYER); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(Items.EXPERIENCE_BOTTLE)) {
                    stack.shrink(1);
                    if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                    player.giveExperiencePoints(10);
                    break;
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Multitask — if enemy is nearby AND player is mining (looking
    //             at a block), also attack the nearest enemy.
    //             Allows simultaneous combat + mining.
    // ---------------------------------------------------------------
    static class Multitask extends AdminModule {
        int tick = 0;
        Multitask() { super("multitask", "Multitask", "Attack mobs while mining simultaneously", ModuleCategory.PLAYER); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;

            // Check if looking at a block (mining)
            HitResult blockHit = player.pick(5.0, 0, false);
            if (blockHit.getType() != HitResult.Type.BLOCK) return;

            // Find nearest hostile mob
            AABB box = player.getBoundingBox().inflate(4.0);
            List<Monster> hostiles = level.getEntitiesOfClass(Monster.class, box, e -> e.isAlive() && player.distanceTo(e) <= 4.0);
            if (hostiles.isEmpty()) return;

            hostiles.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            player.attack(hostiles.get(0));
        }
    }

    // ---------------------------------------------------------------
    // PotionSaver — every 100 ticks, refresh all active potion effects
    //               by re-applying them with extended duration.
    // ---------------------------------------------------------------
    static class PotionSaver extends AdminModule {
        int tick = 0;
        PotionSaver() { super("potion_saver", "PotionSaver", "Extends active potion effect durations", ModuleCategory.PLAYER); }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;

            // Copy the collection to avoid ConcurrentModificationException when re-applying
            List<MobEffectInstance> snapshot = new ArrayList<>(player.getActiveEffects());
            List<MobEffectInstance> toReapply = new ArrayList<>();
            for (MobEffectInstance effect : snapshot) {
                // Re-apply with +600 ticks (30 seconds) more than remaining
                toReapply.add(new MobEffectInstance(
                        effect.getEffect(),
                        effect.getDuration() + 600,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible()
                ));
            }
            for (MobEffectInstance e : toReapply) {
                player.addEffect(e);
            }
        }
    }

    // ---------------------------------------------------------------
    // Rotation — slowly spin the player each tick. Speed setting
    //            controls degrees per tick.
    // ---------------------------------------------------------------
    static class Rotation extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        private ModuleSetting.EnumSetting direction;
        Rotation() { super("rotation", "Rotation", "Slowly spins the player continuously", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            speed = decimal("Speed", 1.0, 0.1, 10.0, "Degrees per tick");
            direction = enumVal("Direction", "Right", List.of("Right", "Left"), "Spin direction");
        }

        @Override
        public void onServerTick(ServerPlayer player, ServerLevel level) {
            float delta = speed.getValue().floatValue();
            if ("Left".equals(direction.getValue())) delta = -delta;
            float newYaw = player.getYRot() + delta;
            // Normalize to -180..180
            while (newYaw > 180) newYaw -= 360;
            while (newYaw < -180) newYaw += 360;
            player.setYRot(newYaw);
            player.hurtMarked = true; // Force sync to client
        }
    }

    // ===============================================================
    // NEW MODULES (7 Meteor-inspired additions — batch 2)
    // ===============================================================

    // ---------------------------------------------------------------
    // BreakDelay — configurable block break delay via Haste effect.
    // ---------------------------------------------------------------
    static class BreakDelay extends AdminModule {
        private ModuleSetting.IntSetting delay;
        int tick = 0;
        BreakDelay() { super("break_delay", "BreakDelay", "Configurable block break delay via Haste level", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            delay = integer("Delay", 0, 0, 10, "Break delay (0 = fastest, applies Haste scaled inversely)");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: refresh every 30 ticks (Haste lasts 40 ticks)
            if (++tick % 30 != 0) return;
            // Haste amplifier: 0 delay = Haste 10, 10 delay = no haste
            int val = delay.getValue();
            int hasteLevel = Math.max(0, 10 - val);
            if (hasteLevel > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, hasteLevel - 1, false, false));
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            player.removeEffect(MobEffects.HASTE);
        }
    }

    // ---------------------------------------------------------------
    // LiquidInteract — when sneaking, place blocks on liquid surfaces
    //                   in front of the player.
    // ---------------------------------------------------------------
    static class LiquidInteract extends AdminModule {
        int tick = 0;
        LiquidInteract() { super("liquid_interact", "LiquidInteract", "Places blocks on liquid surfaces when sneaking", ModuleCategory.PLAYER); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 4 != 0) return;
            if (!player.isCrouching()) return;
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            // Scan forward for liquid blocks
            for (double d = 1.0; d <= 5.0; d += 0.5) {
                BlockPos target = BlockPos.containing(eye.add(look.scale(d)));
                Block block = level.getBlockState(target).getBlock();
                if (block == Blocks.WATER || block == Blocks.LAVA) {
                    // Check if player has cobblestone in inventory
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.is(Items.COBBLESTONE)) {
                            level.setBlock(target, Blocks.COBBLESTONE.defaultBlockState(), 3);
                            stack.shrink(1);
                            if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                            return;
                        }
                    }
                    return; // Found liquid but no cobblestone
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // NameProtect — sets custom display name to "???" to hide identity.
    // ---------------------------------------------------------------
    static class NameProtect extends AdminModule {
        NameProtect() { super("name_protect", "NameProtect", "Hides your name by setting display name to ???", ModuleCategory.PLAYER); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (player.getCustomName() == null || !"???".equals(player.getCustomName().getString())) {
                player.setCustomName(Component.literal("???"));
                player.setCustomNameVisible(true);
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            player.setCustomName(null);
            player.setCustomNameVisible(false);
        }
    }

    // ---------------------------------------------------------------
    // NoMiningTrace — applies Invisibility while mining so other
    //                 players can't see you mine.
    // ---------------------------------------------------------------
    static class NoMiningTrace extends AdminModule {
        private boolean wasMining = false;
        NoMiningTrace() { super("no_mining_trace", "NoMiningTrace", "Applies Invisibility while mining blocks", ModuleCategory.PLAYER); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Detect mining: player is swinging hand (attacking a block)
            boolean isMining = player.swinging;
            if (isMining) {
                if (!player.hasEffect(MobEffects.INVISIBILITY)) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
                }
            }
            wasMining = isMining;
        }
        @Override public void onDisable(ServerPlayer player) {
            player.removeEffect(MobEffects.INVISIBILITY);
            wasMining = false;
        }
    }

    // ---------------------------------------------------------------
    // NoRotate — locks player rotation to prevent server rotation
    //            overrides. Saves rotation on enable, restores each tick.
    // ---------------------------------------------------------------
    static class NoRotate extends AdminModule {
        private float savedYaw = 0;
        private float savedPitch = 0;
        NoRotate() { super("no_rotate", "NoRotate", "Locks player rotation to prevent server rotation overrides", ModuleCategory.PLAYER); }
        @Override public void onEnable(ServerPlayer player) {
            savedYaw = player.getYRot();
            savedPitch = player.getXRot();
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            player.setYRot(savedYaw);
            player.setXRot(savedPitch);
        }
    }

    // ---------------------------------------------------------------
    // NoStatusEffects — removes ALL active effects every tick for
    //                   total effect immunity.
    // ---------------------------------------------------------------
    static class NoStatusEffects extends AdminModule {
        int tick = 0;
        NoStatusEffects() { super("no_status_effects", "NoStatusEffects", "Removes all status effects every tick (total immunity)", ModuleCategory.PLAYER); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return; // every 2 ticks is enough
            player.removeAllEffects();
        }
    }

    // ---------------------------------------------------------------
    // OffhandCrash — rapidly swaps offhand items to stress-test
    //                entity equipment tracking. On admin servers this
    //                forces rapid equipment update packets.
    // ---------------------------------------------------------------
    static class OffhandCrash extends AdminModule {
        private ModuleSetting.IntSetting speed;
        int tick = 0;
        OffhandCrash() { super("offhand_crash", "OffhandCrash", "Rapid offhand item cycling to stress equipment sync", ModuleCategory.PLAYER); }
        @Override protected void initSettings() {
            speed = integer("Speed", 1, 1, 10, "Ticks between offhand swaps");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % speed.getValue() != 0) return;
            // Cycle through inventory items into offhand rapidly
            int invSize = player.getInventory().getContainerSize();
            int sourceSlot = tick % invSize;
            ItemStack source = player.getInventory().getItem(sourceSlot);
            ItemStack currentOffhand = player.getOffhandItem();
            // Swap offhand with a random inventory slot
            player.setItemSlot(EquipmentSlot.OFFHAND, source.copy());
            player.getInventory().setItem(sourceSlot, currentOffhand.copy());
        }
    }

    // ---------------------------------------------------------------
    // Portals — cancel damage while near portal blocks and prevent
    //           portal teleportation cooldown.
    // ---------------------------------------------------------------
    static class Portals extends AdminModule {
        int tick = 0;
        Portals() { super("portals", "Portals", "Cancel damage near portals and prevent portal cooldown", ModuleCategory.PLAYER); }
        @Override public void onDamage(ServerPlayer player, net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event) {
            // Cancel damage if near a portal
            BlockPos center = player.blockPosition();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        Block block = player.level().getBlockState(center.offset(dx, dy, dz)).getBlock();
                        if (block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL) {
                            event.setNewDamage(0);
                            return;
                        }
                    }
                }
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            // Reset portal cooldown to prevent being teleported
            if (player.isOnPortalCooldown()) {
                player.setPortalCooldown(0);
            }
        }
    }

    // ---------------------------------------------------------------
    // Helper: swap the item in the given hotbar slot with mainhand.
    // Since Inventory.selected is private in 1.21.11, we swap the
    // ItemStacks directly between the target slot and the current
    // mainhand slot.
    // ---------------------------------------------------------------
    private static void swapToSlot(ServerPlayer player, int targetSlot) {
        // Find which hotbar slot currently holds the mainhand item
        ItemStack mainHand = player.getMainHandItem();
        int currentSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == mainHand) {
                currentSlot = i;
                break;
            }
        }
        if (currentSlot < 0 || currentSlot == targetSlot) return;

        // Swap the two hotbar slots
        ItemStack targetItem = player.getInventory().getItem(targetSlot);
        ItemStack currentItem = player.getInventory().getItem(currentSlot);
        player.getInventory().setItem(currentSlot, targetItem);
        player.getInventory().setItem(targetSlot, currentItem);
    }

    // ---------------------------------------------------------------
    // PortableCrafting — keybind opens crafting/smithing/anvil anywhere
    // ---------------------------------------------------------------
    static class PortableCrafting extends AdminModule {
        PortableCrafting() {
            super("portable_crafting", "PortableCraft", "Open crafting/smithing/anvil anywhere with a keybind", ModuleCategory.PLAYER);
        }
    }
}
