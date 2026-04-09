package com.ultra.megamod.feature.adminmodules.modules.combat;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.AdminModuleManager;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import com.ultra.megamod.feature.adminmodules.modules.player.PlayerModules;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class KillAuraModule extends AdminModule {
    // --- Existing settings ---
    private ModuleSetting.DoubleSetting range;
    private ModuleSetting.IntSetting cps;
    private ModuleSetting.EnumSetting target;
    private ModuleSetting.BoolSetting onlyHostile;
    private ModuleSetting.BoolSetting ignoreNamed;
    private ModuleSetting.BoolSetting attackPlayers;
    private ModuleSetting.BoolSetting rotateHead;

    // --- New settings ---
    private ModuleSetting.DoubleSetting wallsRange;
    private ModuleSetting.IntSetting maxTargets;
    private ModuleSetting.EnumSetting onlyWhenHolding;
    private ModuleSetting.IntSetting switchDelay;
    private ModuleSetting.BoolSetting pauseWhileEating;
    private ModuleSetting.BoolSetting pauseWhileMining;
    private ModuleSetting.BoolSetting ignoreTamed;
    private ModuleSetting.EnumSetting targetAge;
    private ModuleSetting.BoolSetting smartDelay;

    // --- State tracking ---
    private int tickCounter = 0;
    private int lastSlotChangeTick = 0;
    private ItemStack lastMainHandItem = ItemStack.EMPTY;

    // Admin names centralized in AdminSystem.ADMIN_USERNAMES

    public KillAuraModule() {
        super("kill_aura", "KillAura", "Auto-attacks nearby entities", ModuleCategory.COMBAT);
    }

    @Override
    protected void initSettings() {
        // Existing settings
        range = decimal("Range", 4.5, 1.0, 10.0, "Attack range in blocks");
        wallsRange = decimal("Walls Range", 3.5, 1.0, 6.0, "Range for targets behind blocks");
        cps = integer("CPS", 10, 1, 20, "Attacks per second (ignored if Smart Delay on)");
        smartDelay = bool("Smart Delay", true, "Use vanilla attack cooldown instead of CPS");
        maxTargets = integer("Max Targets", 1, 1, 10, "Max entities to attack per cycle");
        target = enumVal("Target", "Nearest", List.of("Nearest", "Lowest HP", "Highest HP"), "Target priority");
        onlyHostile = bool("Only Hostile", false, "Only attack hostile mobs");
        ignoreNamed = bool("Ignore Named", false, "Don't attack named entities");
        ignoreTamed = bool("Ignore Tamed", true, "Skip tamed animals");
        attackPlayers = bool("Players", false, "Also attack non-admin players");
        targetAge = enumVal("Target Age", "Both", List.of("Both", "Adult", "Baby"), "Filter mobs by age");
        onlyWhenHolding = enumVal("Only When Holding", "Any", List.of("Any", "Weapons", "None"), "Only attack when holding specific items");
        switchDelay = integer("Switch Delay", 0, 0, 20, "Ticks to wait after hotbar slot change");
        pauseWhileEating = bool("Pause While Eating", true, "Don't attack while using items");
        pauseWhileMining = bool("Pause While Mining", false, "Don't attack while swinging arm");
        rotateHead = bool("Rotate Head", true, "Rotate to face target before attacking");
    }

    @Override
    public void onServerTick(ServerPlayer player, ServerLevel level) {
        tickCounter++;

        // --- Track hotbar slot changes via item identity ---
        ItemStack currentMainHand = player.getMainHandItem();
        if (!ItemStack.matches(currentMainHand, lastMainHandItem)) {
            lastSlotChangeTick = tickCounter;
            lastMainHandItem = currentMainHand.copy();
        }

        // --- Timing gate: smart delay (vanilla cooldown) or flat CPS ---
        if (smartDelay.getValue()) {
            if (player.getAttackStrengthScale(0.0f) < 1.0f) return;
        } else {
            int ticksPerAttack = Math.max(1, 20 / cps.getValue());
            if (tickCounter % ticksPerAttack != 0) return;
        }

        // --- Pause checks ---

        // Switch delay: wait N ticks after slot change
        if (switchDelay.getValue() > 0 && (tickCounter - lastSlotChangeTick) < switchDelay.getValue()) return;

        // Pause while eating/drinking/blocking
        if (pauseWhileEating.getValue() && player.isUsingItem()) return;

        // Pause while mining (arm swing)
        if (pauseWhileMining.getValue() && player.swinging) return;

        // Inter-module: pause if AutoEat is active and player is hungry
        AdminModule autoEat = AdminModuleManager.get().getModule("auto_eat");
        if (autoEat != null && autoEat.isEnabled()) {
            // AutoEat threshold defaults to 14; if food is below that, AutoEat is actively feeding
            // Pause attacks to avoid interfering
            if (player.getFoodData().getFoodLevel() < 14) return;
        }

        // --- Only-when-holding check ---
        String holdMode = onlyWhenHolding.getValue();
        if ("Weapons".equals(holdMode)) {
            if (!isWeapon(player.getMainHandItem())) return;
        } else if ("None".equals(holdMode)) {
            // "None" = only attack with empty hand
            if (!player.getMainHandItem().isEmpty()) return;
        }

        // --- Gather targets ---
        double r = range.getValue();
        double wr = wallsRange.getValue();
        double searchRadius = Math.max(r, wr);
        AABB box = player.getBoundingBox().inflate(searchRadius);

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, e -> {
            if (e == player) return false;
            if (!e.isAlive()) return false;

            // Player filter
            if (e instanceof Player p) {
                if (!attackPlayers.getValue()) return false;
                if (AdminSystem.ADMIN_USERNAMES.contains(p.getGameProfile().name())) return false;
                if (PlayerModules.MiddleClickFriend.isFriend(p.getUUID())) return false;
            }

            // Hostile-only filter
            if (onlyHostile.getValue() && !(e instanceof Monster)) return false;

            // Named filter
            if (ignoreNamed.getValue() && e.hasCustomName()) return false;

            // Tamed filter
            if (ignoreTamed.getValue() && e instanceof TamableAnimal tamable && tamable.isTame()) return false;

            // Age filter
            String ageMode = targetAge.getValue();
            if (!"Both".equals(ageMode) && e instanceof AgeableMob ageable) {
                if ("Adult".equals(ageMode) && ageable.isBaby()) return false;
                if ("Baby".equals(ageMode) && !ageable.isBaby()) return false;
            }

            // Range check: use wallsRange if line of sight is blocked, else normal range
            double dist = player.distanceTo(e);
            double effectiveRange = hasLineOfSight(player, e) ? r : wr;
            return dist <= effectiveRange;
        });

        if (entities.isEmpty()) return;

        // --- Sort by priority ---
        switch (target.getValue()) {
            case "Lowest HP" -> entities.sort(Comparator.comparingDouble(LivingEntity::getHealth));
            case "Highest HP" -> entities.sort(Comparator.comparingDouble(e -> -e.getHealth()));
            default -> entities.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
        }

        // --- Attack up to maxTargets entities ---
        int attackCount = Math.min(maxTargets.getValue(), entities.size());
        for (int i = 0; i < attackCount; i++) {
            LivingEntity targetEntity = entities.get(i);

            // Shield-breaker: if target is blocking with a shield, try to swap to an axe
            boolean swappedForShield = false;
            if (targetEntity.isBlocking()) {
                int axeSlot = findAxeInHotbar(player);
                if (axeSlot >= 0 && !isAxe(player.getMainHandItem())) {
                    swapToHotbarSlot(player, axeSlot);
                    swappedForShield = true;
                }
            }

            // Rotate head to face target before attacking
            if (rotateHead.getValue()) {
                double dx = targetEntity.getX() - player.getX();
                double dy = targetEntity.getEyeY() - player.getEyeY();
                double dz = targetEntity.getZ() - player.getZ();
                double horizontalDist = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDist));
                player.setYRot(yaw);
                player.setXRot(pitch);
            }

            // Reset attack strength timer so we get full damage hits
            player.resetAttackStrengthTicker();
            player.attack(targetEntity);

            // Swap back if we switched to axe for shield breaking
            if (swappedForShield) {
                // The axe is now in the current slot; the original weapon was swapped out.
                // We leave it as-is since the axe is better for shield-blocking targets.
                // It will naturally swap back on next tick if target stops blocking.
            }
        }
    }

    // --- Utility methods ---

    /**
     * Check line of sight from player eyes to entity center.
     * Uses level.clip() to raycast and see if a block is between them.
     */
    private boolean hasLineOfSight(ServerPlayer player, LivingEntity target) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        double distance = eyePos.distanceTo(targetPos);

        net.minecraft.world.level.ClipContext ctx = new net.minecraft.world.level.ClipContext(
                eyePos, targetPos,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        );
        HitResult result = player.level().clip(ctx);

        // If the ray hit a block before reaching the target, LOS is blocked
        if (result.getType() == HitResult.Type.BLOCK) {
            double blockDist = result.getLocation().distanceTo(eyePos);
            return blockDist >= distance - 0.5; // small tolerance
        }
        return true; // MISS = clear line of sight
    }

    /**
     * Check if the item is a weapon (sword or axe).
     */
    private static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("sword") || (path.contains("axe") && !path.contains("pickaxe"));
    }

    /**
     * Check if the item is an axe (not pickaxe).
     */
    private static boolean isAxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return path.contains("axe") && !path.contains("pickaxe");
    }

    /**
     * Find an axe in the player's hotbar (slots 0-8).
     * Returns the slot index or -1 if no axe found.
     */
    private static int findAxeInHotbar(ServerPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (isAxe(player.getInventory().getItem(i))) return i;
        }
        return -1;
    }

    /**
     * Swap the item in the given hotbar slot with the player's current mainhand slot.
     * Inventory.selected is private in 1.21.11, so we physically swap ItemStacks.
     */
    private static void swapToHotbarSlot(ServerPlayer player, int targetSlot) {
        int currentSlot = -1;
        ItemStack mainHand = player.getMainHandItem();
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == mainHand) {
                currentSlot = i;
                break;
            }
        }
        if (currentSlot < 0 || currentSlot == targetSlot) return;
        ItemStack targetItem = player.getInventory().getItem(targetSlot);
        ItemStack currentItem = player.getInventory().getItem(currentSlot);
        player.getInventory().setItem(currentSlot, targetItem);
        player.getInventory().setItem(targetSlot, currentItem);
    }
}
