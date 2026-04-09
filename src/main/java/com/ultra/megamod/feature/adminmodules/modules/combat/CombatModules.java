package com.ultra.megamod.feature.adminmodules.modules.combat;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CombatModules {

    public static void register(Consumer<AdminModule> reg) {
        reg.accept(new AutoTotem());
        reg.accept(new AutoArmor());
        reg.accept(new Criticals());
        reg.accept(new AutoWeapon());
        reg.accept(new Hitboxes());
        reg.accept(new HoleFiller());
        reg.accept(new AnchorAura());
        reg.accept(new AutoCity());
        reg.accept(new AutoTrap());
        reg.accept(new AntiAnchor());
        reg.accept(new BedAura());
        reg.accept(new Quiver());
        reg.accept(new TriggerBot());
        reg.accept(new AutoEXP());
        reg.accept(new SelfTrap());
        reg.accept(new Burrow());
        reg.accept(new AutoWeb());
        reg.accept(new OffhandExtra());
        reg.accept(new AntiAnvil());
        reg.accept(new SmartArmor());
        reg.accept(new AutoSword());
        reg.accept(new AimAssist());
        reg.accept(new InfiniteAura());
        reg.accept(new ReachAttack());
        reg.accept(new AutoBow());
        reg.accept(new AutoShield());
        // New modules
        // AntiBed removed: duplicate of AntiAnchor (both cancel BAD_RESPAWN_POINT damage)
        reg.accept(new ArrowDodge());
        reg.accept(new BowSpam());
        reg.accept(new SelfWeb());
        reg.accept(new AutoAnvil());
        reg.accept(new AttributeSwap());
        reg.accept(new SelfAnvil());
    }

    // --- Helper methods ---

    private static boolean isAdmin(Player player) {
        return AdminSystem.ADMIN_USERNAMES.contains(player.getGameProfile().name());
    }

    private static double getItemAttackDamage(ItemStack stack) {
        if (stack.isEmpty() || !stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) return 0;
        ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        // Check for attack damage attribute entries
        double damage = 0;
        for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
            if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value()) {
                AttributeModifier mod = entry.modifier();
                if (mod.operation() == AttributeModifier.Operation.ADD_VALUE) {
                    damage += mod.amount();
                }
            }
        }
        return damage;
    }

    private static int findItemInInventory(ServerPlayer player, java.util.function.Predicate<ItemStack> predicate) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) return i;
        }
        return -1;
    }

    private static boolean isSword(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().contains("sword");
    }

    private static boolean isAxe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String path = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        // "axe" also matches "pickaxe" — exclude that
        return path.contains("axe") && !path.contains("pickaxe");
    }

    private static List<ServerPlayer> getNearbyNonAdminPlayers(ServerPlayer admin, ServerLevel level, double range) {
        AABB box = admin.getBoundingBox().inflate(range);
        return level.getEntitiesOfClass(ServerPlayer.class, box, p ->
                p != admin && p.isAlive() && !isAdmin(p) && admin.distanceTo(p) <= range);
    }

    /**
     * Swap the item in the given hotbar slot with the player's current mainhand slot.
     * Inventory.selected is private in 1.21.11 and has no getSelectedSlot/setSelectedSlot
     * methods, so we physically swap the ItemStacks between slots instead.
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

    // ===================== EXISTING MODULES (IMPLEMENTED) =====================

    static class AutoTotem extends AdminModule {
        private ModuleSetting.EnumSetting priority;
        int tick = 0;
        AutoTotem() { super("auto_totem", "AutoTotem", "Keeps totem in offhand", ModuleCategory.COMBAT); }
        @Override protected void initSettings() {
            priority = enumVal("Priority", "Totem", List.of("Totem", "GapApple", "Shield"), "What to prioritize in offhand when not totem");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: check every 4 ticks (still responsive but not every single tick)
            if (++tick % 4 != 0) return;
            if (!player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                // First try totem
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.is(Items.TOTEM_OF_UNDYING)) {
                        ItemStack offhand = player.getOffhandItem().copy();
                        player.setItemSlot(EquipmentSlot.OFFHAND, stack.copy());
                        player.getInventory().setItem(i, offhand);
                        return;
                    }
                }
                // No totem found — use priority fallback
                String prio = priority.getValue();
                java.util.function.Predicate<ItemStack> fallbackPred = switch (prio) {
                    case "GapApple" -> s -> s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE);
                    case "Shield" -> s -> s.getItem() instanceof ShieldItem;
                    default -> s -> false; // Totem priority, no fallback
                };
                if (!fallbackPred.test(player.getOffhandItem())) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty() && fallbackPred.test(stack)) {
                            ItemStack offhand = player.getOffhandItem().copy();
                            player.setItemSlot(EquipmentSlot.OFFHAND, stack.copy());
                            player.getInventory().setItem(i, offhand);
                            return;
                        }
                    }
                }
            }
        }
    }

    static class AutoArmor extends AdminModule {
        private ModuleSetting.IntSetting delay;
        AutoArmor() { super("auto_armor", "AutoArmor", "Equips best armor automatically", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            delay = integer("Delay", 20, 5, 60, "Ticks between armor checks");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % delay.getValue() != 0) return;
            EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : armorSlots) {
                if (player.getItemBySlot(slot).isEmpty()) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty() && stack.has(DataComponents.EQUIPPABLE) && stack.get(DataComponents.EQUIPPABLE).slot() == slot) {
                            player.setItemSlot(slot, stack.copy());
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    static class Criticals extends AdminModule {
        private int cooldown = 0;
        Criticals() { super("criticals", "Criticals", "All melee attacks deal critical damage", ModuleCategory.COMBAT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (cooldown > 0) { cooldown--; return; }
            if (!player.onGround()) return;
            // Only micro-jump when hostiles are nearby and cooldown expired — much less jittery
            boolean inCombat = !level.getEntitiesOfClass(Monster.class,
                player.getBoundingBox().inflate(4.0), e -> e.isAlive()).isEmpty();
            if (inCombat) {
                // MC crit requires: !onGround && fallDistance > 0 && not in water/flying
                // 0.1 Y velocity is enough to leave ground and accumulate fallDistance for a crit
                // 0.05 was too small — often didn't generate enough fallDistance
                player.setDeltaMovement(player.getDeltaMovement().add(0, 0.1, 0));
                player.hurtMarked = true;
                cooldown = 10; // Only jump every 0.5 seconds, not every tick
            }
        }
    }

    // ===================== PREVIOUSLY STUBBED — NOW IMPLEMENTED =====================

    static class AutoWeapon extends AdminModule {
        private ModuleSetting.BoolSetting preferAxe;
        int tick = 0;
        AutoWeapon() { super("auto_weapon", "AutoWeapon", "Switches to best weapon for combat", ModuleCategory.COMBAT); }
        @Override protected void initSettings() {
            preferAxe = bool("Prefer Axe", false, "Prefer axes over swords");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: check every 4 ticks
            if (++tick % 4 != 0) return;
            // Only switch when a hostile mob is nearby
            AABB box = player.getBoundingBox().inflate(5.0);
            List<Monster> nearby = level.getEntitiesOfClass(Monster.class, box, e -> e.isAlive() && player.distanceTo(e) <= 5.0);
            if (nearby.isEmpty()) return;

            int bestSlot = -1;
            double bestDamage = -1;
            for (int i = 0; i < 9; i++) { // hotbar only
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                boolean sword = isSword(stack);
                boolean axe = isAxe(stack);
                if (!sword && !axe) continue;
                double dmg = getItemAttackDamage(stack);
                // Add slight preference bias
                if (preferAxe.getValue() && axe) dmg += 0.01;
                if (!preferAxe.getValue() && sword) dmg += 0.01;
                if (dmg > bestDamage) {
                    bestDamage = dmg;
                    bestSlot = i;
                }
            }
            if (bestSlot >= 0) {
                ItemStack bestItem = player.getInventory().getItem(bestSlot);
                if (!ItemStack.isSameItemSameComponents(bestItem, player.getMainHandItem())) {
                    swapToHotbarSlot(player, bestSlot);
                }
            }
        }
    }

    static class Hitboxes extends AdminModule {
        private ModuleSetting.DoubleSetting pullDistance;
        Hitboxes() { super("hitboxes", "Hitboxes", "Expands entity hitboxes server-side", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            pullDistance = decimal("Pull Distance", 0.3, 0.1, 1.0, "How far to pull mobs closer");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return;
            double range = 5.0;
            AABB box = player.getBoundingBox().inflate(range);
            List<Monster> mobs = level.getEntitiesOfClass(Monster.class, box, e -> e.isAlive() && player.distanceTo(e) <= range);
            double pull = pullDistance.getValue();
            for (Monster mob : mobs) {
                double dx = player.getX() - mob.getX();
                double dy = player.getY() - mob.getY();
                double dz = player.getZ() - mob.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 1.5) { // don't pull if already very close
                    double factor = pull / dist;
                    mob.teleportTo(mob.getX() + dx * factor, mob.getY() + dy * factor, mob.getZ() + dz * factor);
                }
            }
        }
    }

    static class HoleFiller extends AdminModule {
        private ModuleSetting.IntSetting maxPerTick;
        private ModuleSetting.EnumSetting blockType;
        private int tick = 0;
        HoleFiller() { super("hole_filler", "HoleFiller", "Fills 1x1 holes near enemies", ModuleCategory.COMBAT); }
        @Override protected void initSettings() {
            maxPerTick = integer("Max Per Tick", 2, 1, 5, "Max blocks placed per tick");
            blockType = enumVal("Block", "Obsidian", List.of("Obsidian", "Ender Chest"), "Block type to fill with");
        }
        private BlockState getPlacementBlock() {
            return switch (blockType.getValue()) {
                case "Ender Chest" -> Blocks.ENDER_CHEST.defaultBlockState();
                default -> Blocks.OBSIDIAN.defaultBlockState();
            };
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only scan every 4 ticks to reduce block state lookups
            if (++tick % 4 != 0) return;
            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, 8.0);
            if (enemies.isEmpty()) return;
            BlockState block = getPlacementBlock();
            int placed = 0;
            int max = maxPerTick.getValue();
            for (ServerPlayer enemy : enemies) {
                BlockPos center = enemy.blockPosition();
                for (int dx = -3; dx <= 3 && placed < max; dx++) {
                    for (int dz = -3; dz <= 3 && placed < max; dz++) {
                        BlockPos check = center.offset(dx, 0, dz);
                        if (!level.getBlockState(check).isAir()) continue;
                        // Check if it's a 1x1 hole: solid floor + at least 3 solid walls
                        BlockPos below = check.below();
                        if (!level.getBlockState(below).isSolidRender()) continue;
                        int walls = 0;
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            if (level.getBlockState(check.relative(dir)).isSolidRender()) walls++;
                        }
                        if (walls >= 3) {
                            level.setBlock(check, block, 3);
                            placed++;
                        }
                    }
                }
            }
        }
    }

    static class AnchorAura extends AdminModule {
        AnchorAura() { super("anchor_aura", "AnchorAura", "Auto-explodes respawn anchors near enemies", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            // Respawn anchors explode outside the Nether — we need to be NOT in Nether
            // (In the Nether, anchors work normally and don't explode)
            if (level.dimension() == Level.NETHER) return;

            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, 6.0);
            if (enemies.isEmpty()) return;
            enemies.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            ServerPlayer target = enemies.get(0);
            BlockPos targetPos = target.blockPosition();

            // Scan for existing respawn anchors near the target and detonate them
            for (int dx = -4; dx <= 4; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -4; dz <= 4; dz++) {
                        BlockPos pos = targetPos.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.is(Blocks.RESPAWN_ANCHOR)) {
                            // Remove anchor first, then explode so the explosion isn't blocked by the anchor block
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    5.0f, Level.ExplosionInteraction.BLOCK);
                            return;
                        }
                    }
                }
            }
            // No anchor found — place one near enemy (it will be detonated next cycle)
            BlockPos placePos = targetPos.offset(1, 0, 0);
            if (level.getBlockState(placePos).isAir()) {
                level.setBlock(placePos, Blocks.RESPAWN_ANCHOR.defaultBlockState(), 3);
            }
        }
    }

    static class AutoCity extends AdminModule {
        AutoCity() { super("auto_city", "AutoCity", "Breaks surround blocks near enemies", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return;
            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, 6.0);
            for (ServerPlayer enemy : enemies) {
                BlockPos base = enemy.blockPosition();
                // Look for obsidian/crying obsidian around the enemy
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos check = base.relative(dir);
                    BlockState state = level.getBlockState(check);
                    if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN)) {
                        level.destroyBlock(check, true);
                        return; // Max 1 per tick
                    }
                    // Also check one above (double-height surround)
                    BlockPos upper = check.above();
                    BlockState upperState = level.getBlockState(upper);
                    if (upperState.is(Blocks.OBSIDIAN) || upperState.is(Blocks.CRYING_OBSIDIAN)) {
                        level.destroyBlock(upper, true);
                        return;
                    }
                }
            }
        }
    }

    static class AutoTrap extends AdminModule {
        private ModuleSetting.DoubleSetting range;
        private ModuleSetting.BoolSetting topBlock;
        AutoTrap() { super("auto_trap", "AutoTrap", "Places obsidian around nearby players", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            range = decimal("Range", 4.0, 1.0, 8.0, "Target detection range");
            topBlock = bool("TopBlock", true, "Place obsidian above head");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            double r = range.getValue();
            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, r);
            if (enemies.isEmpty()) return;
            // Target closest
            enemies.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            ServerPlayer target = enemies.get(0);
            BlockPos base = target.blockPosition();
            // Place obsidian on 4 cardinal sides
            int[][] sideOffsets = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};
            for (int[] off : sideOffsets) {
                BlockPos pos = base.offset(off[0], off[1], off[2]);
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
            // Optionally place above head
            if (topBlock.getValue()) {
                BlockPos above = base.offset(0, 2, 0);
                if (level.getBlockState(above).isAir()) {
                    level.setBlock(above, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }
    }

    static class AntiAnchor extends AdminModule {
        AntiAnchor() { super("anti_anchor", "AntiAnchor", "Prevents respawn anchor damage", ModuleCategory.COMBAT); }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            if (event.getSource().is(DamageTypes.BAD_RESPAWN_POINT)) {
                event.setNewDamage(0);
            }
        }
    }

    static class BedAura extends AdminModule {
        private ModuleSetting.IntSetting scanRadius;
        BedAura() { super("bed_aura", "BedAura", "Auto-explodes beds in Nether/End", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            scanRadius = integer("Range", 6, 3, 10, "Bed scan range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            // Beds only explode outside Overworld
            if (level.dimension() == Level.OVERWORLD) return;

            // Pre-check: are any enemies even nearby? Skip expensive block scan if not
            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, 8.0);
            if (enemies.isEmpty()) return;

            BlockPos center = player.blockPosition();
            int scanRange = scanRadius.getValue();
            for (int dx = -scanRange; dx <= scanRange; dx++) {
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dz = -scanRange; dz <= scanRange; dz++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof BedBlock) {
                            // Explode first, then remove the bed block
                            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    5.0f, Level.ExplosionInteraction.BLOCK);
                            // Remove both halves of the bed
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            return; // One explosion per cycle
                        }
                    }
                }
            }
        }
    }

    static class Quiver extends AdminModule {
        Quiver() { super("quiver", "Quiver", "Auto-switches arrow types", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            // Only manage arrows if player is holding a bow
            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof BowItem)) return;

            // Scan for enemy armor to decide arrow type
            AABB box = player.getBoundingBox().inflate(16.0);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && (e instanceof Monster || (e instanceof Player && !isAdmin((Player) e))));

            boolean targetIsArmored = false;
            for (LivingEntity target : targets) {
                // Check if target has any armor
                for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                    if (!target.getItemBySlot(slot).isEmpty()) {
                        targetIsArmored = true;
                        break;
                    }
                }
                if (targetIsArmored) break;
            }

            // Find preferred arrow type in inventory and move to offhand
            int bestArrowSlot = -1;
            if (targetIsArmored) {
                // Prefer spectral arrows vs armored targets (Glowing reveals them)
                bestArrowSlot = findItemInInventory(player, s -> s.is(Items.SPECTRAL_ARROW));
            }
            if (bestArrowSlot < 0) {
                // Default: find any arrow
                bestArrowSlot = findItemInInventory(player, s -> s.getItem() instanceof ArrowItem);
            }

            if (bestArrowSlot >= 0) {
                ItemStack currentOffhand = player.getOffhandItem();
                // Only swap if offhand doesn't already have the right type
                if (currentOffhand.isEmpty() || !(currentOffhand.getItem() instanceof ArrowItem)) {
                    ItemStack arrow = player.getInventory().getItem(bestArrowSlot);
                    player.setItemSlot(EquipmentSlot.OFFHAND, arrow.copy());
                    player.getInventory().setItem(bestArrowSlot, currentOffhand.copy());
                }
            }
        }
    }

    static class TriggerBot extends AdminModule {
        private ModuleSetting.DoubleSetting reach;
        TriggerBot() { super("trigger_bot", "TriggerBot", "Auto-attacks entity under crosshair", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            reach = decimal("Reach", 4.5, 3.0, 8.0, "Attack reach distance");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return;
            // Only attack when attack cooldown is ready for full damage
            if (player.getAttackStrengthScale(0.0f) < 0.9f) return;

            double r = reach.getValue();
            // Server-side raycast: get look direction and scan for entities
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(r));

            AABB searchBox = player.getBoundingBox().inflate(r);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive());

            LivingEntity bestTarget = null;
            double bestDist = r + 1;

            for (LivingEntity entity : entities) {
                AABB entityBox = entity.getBoundingBox().inflate(0.3);
                // Check if look vector intersects entity bounding box
                var clip = entityBox.clip(eye, end);
                if (clip.isPresent()) {
                    double dist = eye.distanceTo(clip.get());
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestTarget = entity;
                    }
                }
            }

            if (bestTarget != null) {
                player.resetAttackStrengthTicker();
                player.attack(bestTarget);
            }
        }
    }

    static class AutoEXP extends AdminModule {
        private ModuleSetting.IntSetting xpAmount;
        AutoEXP() { super("auto_exp", "AutoEXP", "Auto-grants XP to repair mending gear", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            xpAmount = integer("XP Amount", 5, 1, 50, "XP points granted per cycle");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            // Only give XP if player has damaged equipment (mending will use XP to repair)
            boolean hasDamagedGear = false;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = player.getItemBySlot(slot);
                if (!item.isEmpty() && item.isDamaged()) {
                    hasDamagedGear = true;
                    break;
                }
            }
            if (hasDamagedGear) {
                player.giveExperiencePoints(xpAmount.getValue());
            }
        }
    }

    static class SelfTrap extends AdminModule {
        int tick = 0;
        SelfTrap() { super("self_trap", "SelfTrap", "Places obsidian above your head", ModuleCategory.COMBAT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only check every 5 ticks to avoid spamming setBlock
            if (++tick % 5 != 0) return;
            BlockPos above = player.blockPosition().above(2);
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
    }

    static class Burrow extends AdminModule {
        Burrow() { super("burrow", "Burrow", "Places block at your feet", ModuleCategory.COMBAT); }
        @Override public void onEnable(ServerPlayer player) {
            BlockPos feet = player.blockPosition();
            ServerLevel level = (ServerLevel) player.level();
            if (level.getBlockState(feet).isAir()) {
                level.setBlock(feet, Blocks.OBSIDIAN.defaultBlockState(), 3);
                player.teleportTo(player.getX(), player.getY() + 1.0, player.getZ());
            }
            setEnabled(false);
        }
    }

    static class AutoWeb extends AdminModule {
        private ModuleSetting.DoubleSetting range;
        AutoWeb() { super("auto_web", "AutoWeb", "Places webs near enemies", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            range = decimal("Range", 4.0, 1.0, 8.0, "Target detection range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, range.getValue());
            if (enemies.isEmpty()) return;
            enemies.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            ServerPlayer target = enemies.get(0);
            BlockPos feet = target.blockPosition();
            BlockPos head = feet.above();
            // Place cobwebs at feet and head
            if (level.getBlockState(feet).isAir()) {
                level.setBlock(feet, Blocks.COBWEB.defaultBlockState(), 3);
            }
            if (level.getBlockState(head).isAir()) {
                level.setBlock(head, Blocks.COBWEB.defaultBlockState(), 3);
            }
        }
    }

    static class OffhandExtra extends AdminModule {
        OffhandExtra() { super("offhand_extra", "OffhandExtra", "Manages offhand item swapping", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;

            float health = player.getHealth();
            boolean enemyNearby = !level.getEntitiesOfClass(Monster.class,
                    player.getBoundingBox().inflate(6.0), e -> e.isAlive()).isEmpty()
                    || !getNearbyNonAdminPlayers(player, level, 6.0).isEmpty();

            ItemStack desired = ItemStack.EMPTY;
            int desiredSlot = -1;

            if (health < 10.0f) {
                // Low health: prioritize totem
                desiredSlot = findItemInInventory(player, s -> s.is(Items.TOTEM_OF_UNDYING));
                if (desiredSlot >= 0) desired = player.getInventory().getItem(desiredSlot);
            } else if (enemyNearby) {
                // In combat: prioritize shield
                desiredSlot = findItemInInventory(player, s -> s.getItem() instanceof ShieldItem);
                if (desiredSlot >= 0) desired = player.getInventory().getItem(desiredSlot);
            }

            if (desired.isEmpty()) {
                // Default: golden apple
                desiredSlot = findItemInInventory(player, s -> s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE));
                if (desiredSlot >= 0) desired = player.getInventory().getItem(desiredSlot);
            }

            if (!desired.isEmpty() && desiredSlot >= 0) {
                ItemStack currentOffhand = player.getOffhandItem();
                // Don't swap if already holding the same type
                if (currentOffhand.getItem() == desired.getItem()) return;
                player.setItemSlot(EquipmentSlot.OFFHAND, desired.copy());
                player.getInventory().setItem(desiredSlot, currentOffhand.copy());
            }
        }
    }

    static class AntiAnvil extends AdminModule {
        AntiAnvil() { super("anti_anvil", "AntiAnvil", "Prevents falling anvil damage", ModuleCategory.COMBAT); }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            if (event.getSource().is(DamageTypes.FALLING_ANVIL)) {
                event.setNewDamage(0);
            }
        }
    }

    static class SmartArmor extends AdminModule {
        SmartArmor() { super("smart_armor", "SmartArmor", "Swaps armor based on situation", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : armorSlots) {
                ItemStack equipped = player.getItemBySlot(slot);
                if (equipped.isEmpty()) continue;
                // Check durability — if below 10%, swap with replacement from inventory
                int maxDmg = equipped.getMaxDamage();
                if (maxDmg <= 0) continue;
                int remaining = maxDmg - equipped.getDamageValue();
                double durabilityPct = (double) remaining / maxDmg;
                if (durabilityPct < 0.10) {
                    // Find replacement of same slot type
                    int bestSlot = -1;
                    int bestDurability = remaining;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack candidate = player.getInventory().getItem(i);
                        if (candidate.isEmpty()) continue;
                        if (!candidate.has(DataComponents.EQUIPPABLE)) continue;
                        if (candidate.get(DataComponents.EQUIPPABLE).slot() != slot) continue;
                        int candidateRemaining = candidate.getMaxDamage() - candidate.getDamageValue();
                        if (candidateRemaining > bestDurability) {
                            bestDurability = candidateRemaining;
                            bestSlot = i;
                        }
                    }
                    if (bestSlot >= 0) {
                        ItemStack replacement = player.getInventory().getItem(bestSlot);
                        player.setItemSlot(slot, replacement.copy());
                        player.getInventory().setItem(bestSlot, equipped.copy());
                    }
                }
            }
        }
    }

    static class AutoSword extends AdminModule {
        int tick = 0;
        AutoSword() { super("auto_sword", "AutoSword", "Auto-switches to sword in combat", ModuleCategory.COMBAT); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: only check every 4 ticks
            if (++tick % 4 != 0) return;
            AABB box = player.getBoundingBox().inflate(5.0);
            List<Monster> nearby = level.getEntitiesOfClass(Monster.class, box, e -> e.isAlive() && player.distanceTo(e) <= 5.0);
            if (nearby.isEmpty()) return;

            // Already holding a sword? No action needed
            if (isSword(player.getMainHandItem())) return;

            int bestSlot = -1;
            double bestDamage = -1;
            for (int i = 0; i < 9; i++) { // hotbar only
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty() || !isSword(stack)) continue;
                double dmg = getItemAttackDamage(stack);
                if (dmg > bestDamage) {
                    bestDamage = dmg;
                    bestSlot = i;
                }
            }
            if (bestSlot >= 0) {
                ItemStack bestItem = player.getInventory().getItem(bestSlot);
                if (!ItemStack.isSameItemSameComponents(bestItem, player.getMainHandItem())) {
                    swapToHotbarSlot(player, bestSlot);
                }
            }
        }
    }

    static class AimAssist extends AdminModule {
        private ModuleSetting.DoubleSetting range;
        private ModuleSetting.DoubleSetting rotSpeed;
        int tick = 0;
        AimAssist() { super("aim_assist", "AimAssist", "Subtly adjusts aim toward targets", ModuleCategory.COMBAT); }
        @Override protected void initSettings() {
            range = decimal("Range", 8.0, 3.0, 16.0, "Target acquisition range");
            rotSpeed = decimal("Speed", 5.0, 1.0, 20.0, "Rotation speed in degrees per tick");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: every 2 ticks to reduce entity scanning overhead
            if (++tick % 2 != 0) return;
            double r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && (e instanceof Monster || (e instanceof Player && !isAdmin((Player) e))));
            if (targets.isEmpty()) return;
            targets.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            LivingEntity target = targets.get(0);
            if (player.distanceTo(target) > r) return;

            // Calculate desired yaw/pitch
            double dx = target.getX() - player.getX();
            double dy = (target.getEyeY()) - player.getEyeY();
            double dz = target.getZ() - player.getZ();
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            float desiredYaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
            float desiredPitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDist)));

            // Gradually rotate toward target
            float speed = rotSpeed.getValue().floatValue();
            float currentYaw = player.getYRot();
            float currentPitch = player.getXRot();

            float yawDiff = desiredYaw - currentYaw;
            // Normalize yaw difference to -180..180
            while (yawDiff > 180) yawDiff -= 360;
            while (yawDiff < -180) yawDiff += 360;
            float pitchDiff = desiredPitch - currentPitch;

            float yawStep = Math.max(-speed, Math.min(speed, yawDiff));
            float pitchStep = Math.max(-speed, Math.min(speed, pitchDiff));

            player.setYRot(currentYaw + yawStep);
            player.setXRot(currentPitch + pitchStep);
        }
    }

    static class InfiniteAura extends AdminModule {
        private ModuleSetting.DoubleSetting range;
        private ModuleSetting.IntSetting maxTargets;
        private ModuleSetting.IntSetting tickRate;
        private int tick = 0;
        InfiniteAura() { super("infinite_aura", "InfiniteAura", "Attacks all entities in range", ModuleCategory.COMBAT); }
        @Override protected void initSettings() {
            range = decimal("Range", 8.0, 1.0, 32.0, "Attack range");
            maxTargets = integer("Max Targets", 10, 1, 50, "Max entities to attack per cycle");
            tickRate = integer("Tick Rate", 4, 1, 20, "Ticks between attack cycles");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % tickRate.getValue() != 0) return;
            double r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && !(e instanceof Player) && player.distanceTo(e) <= r);
            // Sort by distance so closest mobs are hit first
            targets.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            int max = maxTargets.getValue();
            int count = 0;
            for (LivingEntity e : targets) {
                if (count >= max) break;
                player.attack(e);
                count++;
            }
        }
    }

    static class ReachAttack extends AdminModule {
        private ModuleSetting.DoubleSetting range;
        ReachAttack() { super("reach_attack", "ReachAttack", "Extends melee attack range", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            range = decimal("Range", 6.0, 4.5, 10.0, "Extended attack range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return;
            // Only attack when attack strength is high enough for meaningful damage
            if (player.getAttackStrengthScale(0.0f) < 0.9f) return;

            double r = range.getValue();
            // Server-side raycast for extended range
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            Vec3 end = eye.add(look.scale(r));

            AABB searchBox = player.getBoundingBox().inflate(r);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                    e -> e != player && e.isAlive());

            LivingEntity bestTarget = null;
            double bestDist = r + 1;

            for (LivingEntity entity : entities) {
                AABB entityBox = entity.getBoundingBox().inflate(0.3);
                var clip = entityBox.clip(eye, end);
                if (clip.isPresent()) {
                    double dist = eye.distanceTo(clip.get());
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestTarget = entity;
                    }
                }
            }

            if (bestTarget != null) {
                player.resetAttackStrengthTicker();
                player.attack(bestTarget);
            }
        }
    }

    static class AutoBow extends AdminModule {
        private ModuleSetting.DoubleSetting range;
        private ModuleSetting.DoubleSetting damage;
        AutoBow() { super("auto_bow", "AutoBow", "Instant ranged damage while holding bow (no projectile)", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            range = decimal("Range", 32.0, 8.0, 64.0, "Target acquisition range");
            damage = decimal("Damage", 6.0, 1.0, 20.0, "Damage per hit (6 = full-charge bow)");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            // Only works when holding a bow
            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof BowItem)) return;

            double r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e.isAlive() && player.distanceTo(e) <= r
                         && (e instanceof Monster || (e instanceof Player && !isAdmin((Player) e))));
            if (targets.isEmpty()) return;
            targets.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            LivingEntity target = targets.get(0);

            // Ensure player has arrows (or is in creative)
            if (!player.getAbilities().instabuild) {
                int arrowSlot = findItemInInventory(player, s -> s.getItem() instanceof ArrowItem);
                boolean hasOffhandArrow = player.getOffhandItem().getItem() instanceof ArrowItem;
                if (arrowSlot < 0 && !hasOffhandArrow) return;
                // Consume an arrow
                if (hasOffhandArrow) {
                    ItemStack offhandArrow = player.getOffhandItem();
                    offhandArrow.shrink(1);
                    if (offhandArrow.isEmpty()) player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                } else {
                    ItemStack invArrow = player.getInventory().getItem(arrowSlot);
                    invArrow.shrink(1);
                    if (invArrow.isEmpty()) player.getInventory().setItem(arrowSlot, ItemStack.EMPTY);
                }
            }

            // Apply arrow-level damage directly (no projectile — hitscan style, admin-only)
            // Uses arrow damage source for proper damage type
            target.hurt(level.damageSources().arrow(null, player), damage.getValue().floatValue());
        }
    }

    static class AutoShield extends AdminModule {
        private ModuleSetting.DoubleSetting damageReduction;
        AutoShield() { super("auto_shield", "AutoShield", "Auto-blocks with shield on incoming damage", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            damageReduction = decimal("DamageReduction", 0.5, 0.1, 1.0, "Shield damage reduction multiplier");
        }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            // If shield is in offhand, reduce damage
            ItemStack offhand = player.getOffhandItem();
            if (offhand.getItem() instanceof ShieldItem) {
                event.setNewDamage(event.getNewDamage() * damageReduction.getValue().floatValue());
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            // If enemy is within 4 blocks and player has shield, apply brief Resistance
            boolean hasShield = player.getOffhandItem().getItem() instanceof ShieldItem
                    || player.getMainHandItem().getItem() instanceof ShieldItem;
            if (!hasShield) return;

            boolean enemyNearby = !level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(4.0),
                    e -> e != player && e.isAlive() && (e instanceof Monster || (e instanceof Player && !isAdmin((Player) e)))).isEmpty();

            if (enemyNearby) {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 15, 0, false, false));
            }
        }
    }

    // ===================== NEW MODULES =====================

    // AntiBed removed: was an exact duplicate of AntiAnchor (both cancelled BAD_RESPAWN_POINT).
    // AntiAnchor already covers both bed and respawn anchor explosions since they use the same damage type.

    static class ArrowDodge extends AdminModule {
        private ModuleSetting.DoubleSetting dodgeRange;
        ArrowDodge() { super("arrow_dodge", "ArrowDodge", "Teleports sideways to dodge incoming arrows", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            dodgeRange = decimal("Scan Range", 8.0, 4.0, 16.0, "Arrow detection range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return;
            double r = dodgeRange.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<AbstractArrow> arrows = level.getEntitiesOfClass(AbstractArrow.class, box,
                    a -> a.getOwner() != player && !a.onGround() && a.isAlive());

            for (AbstractArrow arrow : arrows) {
                // Check if arrow is heading toward player
                Vec3 arrowVel = arrow.getDeltaMovement();
                Vec3 toPlayer = player.position().subtract(arrow.position());
                double dot = arrowVel.normalize().dot(toPlayer.normalize());
                if (dot < 0.7) continue; // Not aimed at us

                double dist = arrow.distanceTo(player);
                if (dist > r || dist < 0.5) continue;

                // Calculate perpendicular dodge direction
                Vec3 dodgeDir = arrowVel.cross(new Vec3(0, 1, 0)).normalize();
                if (dodgeDir.lengthSqr() < 0.01) {
                    dodgeDir = new Vec3(1, 0, 0); // fallback
                }

                // Teleport 1 block sideways
                double newX = player.getX() + dodgeDir.x;
                double newZ = player.getZ() + dodgeDir.z;
                BlockPos landing = BlockPos.containing(newX, player.getY(), newZ);
                // Ensure destination is safe (not solid at feet level)
                if (!level.getBlockState(landing).isSolidRender()) {
                    player.teleportTo(newX, player.getY(), newZ);
                }
                return; // One dodge per cycle
            }
        }
    }

    static class BowSpam extends AdminModule {
        private ModuleSetting.IntSetting rate;
        BowSpam() { super("bow_spam", "BowSpam", "Fires arrows rapidly by spawning arrow entities", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override protected void initSettings() {
            rate = integer("Rate", 5, 1, 20, "Ticks between shots");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % rate.getValue() != 0) return;
            ItemStack mainHand = player.getMainHandItem();
            if (!(mainHand.getItem() instanceof BowItem)) return;

            // Check for arrows in inventory
            int arrowSlot = findItemInInventory(player, s -> s.getItem() instanceof ArrowItem);
            boolean hasArrowOffhand = player.getOffhandItem().getItem() instanceof ArrowItem;
            if (arrowSlot < 0 && !hasArrowOffhand) return;

            // Spawn an arrow entity in the player's look direction
            Vec3 look = player.getLookAngle();
            Vec3 eye = player.getEyePosition();
            net.minecraft.world.entity.projectile.arrow.Arrow arrow =
                    new net.minecraft.world.entity.projectile.arrow.Arrow(level, eye.x, eye.y, eye.z, ItemStack.EMPTY, ItemStack.EMPTY);
            arrow.setOwner(player);
            arrow.setDeltaMovement(look.scale(3.0)); // Full charge speed
            arrow.setCritArrow(true);
            level.addFreshEntity(arrow);

            // Consume an arrow from inventory (not in creative)
            if (!player.getAbilities().instabuild) {
                if (hasArrowOffhand) {
                    ItemStack offhandArrow = player.getOffhandItem();
                    offhandArrow.shrink(1);
                    if (offhandArrow.isEmpty()) player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                } else if (arrowSlot >= 0) {
                    ItemStack invArrow = player.getInventory().getItem(arrowSlot);
                    invArrow.shrink(1);
                    if (invArrow.isEmpty()) player.getInventory().setItem(arrowSlot, ItemStack.EMPTY);
                }
            }
        }
    }

    static class SelfWeb extends AdminModule {
        SelfWeb() { super("self_web", "SelfWeb", "Places cobweb at your position", ModuleCategory.COMBAT); }
        @Override public void onEnable(ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
            }
            // One-shot module
            setEnabled(false);
        }
    }

    static class AutoAnvil extends AdminModule {
        AutoAnvil() { super("auto_anvil", "AutoAnvil", "Drops anvils above enemy players", ModuleCategory.COMBAT); }
        int tick = 0;
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            List<ServerPlayer> enemies = getNearbyNonAdminPlayers(player, level, 16.0);
            if (enemies.isEmpty()) return;
            enemies.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));
            ServerPlayer target = enemies.get(0);
            // Place anvil 10 blocks above the target
            BlockPos above = target.blockPosition().above(10);
            if (level.getBlockState(above).isAir()) {
                // Place a falling anvil — use FallingBlockEntity for gravity
                net.minecraft.world.entity.item.FallingBlockEntity anvil =
                        net.minecraft.world.entity.item.FallingBlockEntity.fall(level, above, Blocks.ANVIL.defaultBlockState());
                if (anvil != null) {
                    anvil.setHurtsEntities(2.0f, 40); // damage multiplier, max damage
                }
            }
        }
    }

    // ===================== ATTRIBUTE SWAP =====================

    static class AttributeSwap extends AdminModule {
        AttributeSwap() { super("attribute_swap", "AttributeSwap", "Swaps attack damage and armor attribute values on held item", ModuleCategory.COMBAT); }
        @Override public void onEnable(ServerPlayer player) {
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() || !held.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
                setEnabled(false);
                return;
            }
            ItemAttributeModifiers mods = held.get(DataComponents.ATTRIBUTE_MODIFIERS);
            // Collect attack damage and armor values
            double attackDamage = 0;
            double armorValue = 0;
            for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
                if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value() && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                    attackDamage += entry.modifier().amount();
                }
                if (entry.attribute().value() == Attributes.ARMOR.value() && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                    armorValue += entry.modifier().amount();
                }
            }
            // Build new modifier list with swapped values using Builder API
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            for (ItemAttributeModifiers.Entry entry : mods.modifiers()) {
                if (entry.attribute().value() == Attributes.ATTACK_DAMAGE.value() && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                    builder.add(entry.attribute(),
                            new AttributeModifier(entry.modifier().id(), armorValue, entry.modifier().operation()),
                            entry.slot());
                } else if (entry.attribute().value() == Attributes.ARMOR.value() && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                    builder.add(entry.attribute(),
                            new AttributeModifier(entry.modifier().id(), attackDamage, entry.modifier().operation()),
                            entry.slot());
                } else {
                    builder.add(entry.attribute(), entry.modifier(), entry.slot());
                }
            }
            held.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
            // One-shot module
            setEnabled(false);
        }
    }

    // ===================== SELF ANVIL =====================

    static class SelfAnvil extends AdminModule {
        SelfAnvil() { super("self_anvil", "SelfAnvil", "Drops an anvil 10 blocks above yourself", ModuleCategory.COMBAT); }
        @Override public void onEnable(ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos above = player.blockPosition().above(10);
            if (level.getBlockState(above).isAir()) {
                net.minecraft.world.entity.item.FallingBlockEntity anvil =
                        net.minecraft.world.entity.item.FallingBlockEntity.fall(level, above, Blocks.ANVIL.defaultBlockState());
                if (anvil != null) {
                    anvil.setHurtsEntities(2.0f, 40);
                }
            }
            // One-shot module
            setEnabled(false);
        }
    }
}
