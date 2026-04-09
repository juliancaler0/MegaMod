package com.ultra.megamod.feature.relics.ability;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.attributes.network.CombatTextSender;
import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.ability.AbilityCooldownManager;
import com.ultra.megamod.feature.relics.ability.AbilitySystem;
import com.ultra.megamod.feature.relics.accessory.AccessoryManager;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponRegistry;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponSkillHandler;
import com.ultra.megamod.feature.combat.spell.SpellAbilityBridge;
import com.ultra.megamod.feature.combat.spell.SpellDefinition;
import com.ultra.megamod.feature.combat.spell.SpellExecutor;
import com.ultra.megamod.feature.combat.spell.SpellRegistry;
import com.ultra.megamod.feature.skills.integration.SkillsRelicIntegration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AbilityCastHandler {
    private static final Map<String, AbilityExecutor> EXECUTORS = new HashMap<String, AbilityExecutor>();
    private static final int DEFAULT_COOLDOWN = 60;

    public static void registerAbility(String relicName, String abilityName, AbilityExecutor executor) {
        String key = relicName + ":" + abilityName;
        EXECUTORS.put(key, executor);
    }

    public static AbilityExecutor getExecutor(String key) {
        return EXECUTORS.get(key);
    }

    /**
     * Main cast entry point. Called from AbilityCastPayload.handleOnServer().
     * Now accepts abilityIndex for unified bar index-based resolution.
     */
    public static void handleCast(ServerPlayer player, String slotName, String abilityName, int abilityIndex) {
        // === OFFHAND: shield weapon abilities ===
        if ("OFFHAND".equals(slotName)) {
            ItemStack offhand = player.getOffhandItem();
            if (offhand.isEmpty()) {
                player.displayClientMessage(Component.literal("No shield in offhand.").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            Item offItem = offhand.getItem();
            if (offItem instanceof RpgWeaponItem || RpgWeaponRegistry.isRpgWeapon(
                    BuiltInRegistries.ITEM.getKey(offItem).toString())) {
                RpgWeaponSkillHandler.handleCast(player, offhand, abilityIndex);
                return;
            }
            if (offItem instanceof RelicItem relicItem) {
                handleRelicCast(player, offhand, relicItem, slotName, abilityName, abilityIndex);
                return;
            }
            player.displayClientMessage(Component.literal("No abilities on offhand item.").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        // === MAINHAND: weapon abilities (RelicItem or RpgWeaponItem) ===
        if ("MAINHAND".equals(slotName)) {
            ItemStack held = player.getMainHandItem();
            // If mainhand is empty or not a weapon, also check offhand (shield slot)
            if (held.isEmpty() || !(held.getItem() instanceof RelicItem || held.getItem() instanceof RpgWeaponItem
                    || RpgWeaponRegistry.isRpgWeapon(BuiltInRegistries.ITEM.getKey(held.getItem()).toString()))) {
                ItemStack offhand = player.getOffhandItem();
                if (!offhand.isEmpty() && (offhand.getItem() instanceof RpgWeaponItem
                        || offhand.getItem() instanceof RelicItem
                        || RpgWeaponRegistry.isRpgWeapon(BuiltInRegistries.ITEM.getKey(offhand.getItem()).toString()))) {
                    held = offhand;
                }
            }
            if (held.isEmpty()) {
                player.displayClientMessage(Component.literal("No weapon in hand.").withStyle(ChatFormatting.GRAY), true);
                return;
            }
            Item item = held.getItem();

            // RelicItem weapon (NONE slot relics held in hand)
            if (item instanceof RelicItem relicItem) {
                handleRelicCast(player, held, relicItem, slotName, abilityName, abilityIndex);
                return;
            }

            // RpgWeaponItem (arsenal weapons, staves, shields, etc.)
            if (item instanceof RpgWeaponItem || RpgWeaponRegistry.isRpgWeapon(
                    BuiltInRegistries.ITEM.getKey(item).toString())) {
                RpgWeaponSkillHandler.handleCast(player, held, abilityIndex);
                return;
            }

            player.displayClientMessage(Component.literal("No abilities on this item.").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        // === ACCESSORY SLOT: equipped relic abilities ===
        ItemStack stack = findRelicStack(player, slotName);
        if (stack.isEmpty() || !(stack.getItem() instanceof RelicItem relicItem)) {
            player.displayClientMessage(Component.literal("No relic in " + slotName + " slot.").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        handleRelicCast(player, stack, relicItem, slotName, abilityName, abilityIndex);
    }

    /**
     * Handles casting for RelicItem (both weapons and accessories).
     */
    private static void handleRelicCast(ServerPlayer player, ItemStack stack, RelicItem relicItem,
                                         String slotName, String abilityName, int abilityIndex) {
        // Resolve ability name from index if needed
        String resolvedName = abilityName;
        if ("__byindex__".equals(abilityName) || "__primary__".equals(abilityName) || "__secondary__".equals(abilityName)) {
            int targetIndex = abilityIndex;
            if ("__primary__".equals(abilityName)) targetIndex = 0;
            if ("__secondary__".equals(abilityName)) targetIndex = 1;
            resolvedName = resolveAbilityByIndex(relicItem, stack, targetIndex);
            if (resolvedName == null) {
                player.displayClientMessage(Component.literal(relicItem.getRelicName() + " has no castable ability at index " + targetIndex + ".").withStyle(ChatFormatting.GRAY), true);
                return;
            }
        }

        // Find the target ability
        RelicAbility targetAbility = null;
        for (RelicAbility ability : relicItem.getAbilities()) {
            if (ability.name().equals(resolvedName)) {
                targetAbility = ability;
                break;
            }
        }
        if (targetAbility == null) return;

        // Auto-initialize if needed
        if (!RelicData.isInitialized(stack)) {
            RelicData.initialize(stack, relicItem.getAbilities(), player.getRandom());
            updateStackInSlot(player, stack);
        }

        // Level check
        int relicLevel = RelicData.getLevel(stack);
        if (!RelicData.isAbilityUnlocked(relicLevel, targetAbility, relicItem.getAbilities())) {
            player.displayClientMessage(Component.literal("Need relic level " + targetAbility.requiredLevel() + " to use " + resolvedName + ".").withStyle(ChatFormatting.RED), true);
            return;
        }

        // Cooldown check
        if (AbilityCooldownManager.isOnCooldown(stack, resolvedName)) {
            player.displayClientMessage(Component.literal(resolvedName + " is on cooldown.").withStyle(ChatFormatting.YELLOW), true);
            return;
        }

        // Execute
        executeAbility(player, stack, targetAbility, relicItem);
    }

    private static void executeAbility(ServerPlayer player, ItemStack stack, RelicAbility ability, RelicItem relicItem) {
        // Spell bridge: if this ability is mapped to a spell, delegate to SpellExecutor
        String spellId = SpellAbilityBridge.getSpellForAbility(relicItem.getRelicName(), ability.name());
        if (spellId != null) {
            SpellDefinition spell = SpellRegistry.get(spellId);
            if (spell != null) {
                boolean cast = SpellExecutor.cast(player, spell);
                if (cast) {
                    CombatTextSender.sendAbility(player, ability.name());
                    com.ultra.megamod.feature.skills.challenges.SkillChallenges.addProgress(player, "ability_use", 1);
                    com.ultra.megamod.feature.hud.combos.CombatComboTracker.onAbilityCast(player, ability.name());
                    int baseCooldown = RelicData.getEffectiveCooldown(stack, ability.name(), DEFAULT_COOLDOWN);
                    int adjustedCooldown = SkillsRelicIntegration.applyCooldownReduction(player, baseCooldown);
                    AbilityCooldownManager.setCooldown(stack, ability.name(), adjustedCooldown);
                    SkillsRelicIntegration.grantArcaneCastXp(player);
                    AbilityCastHandler.updateStackInSlot(player, stack);
                }
                return;
            }
        }

        switch (ability.castType()) {
            case PASSIVE: {
                return;
            }
            case INSTANTANEOUS: {
                double[] statValues = AbilityCastHandler.computeStatValues(stack, ability);
                statValues = SkillsRelicIntegration.applyAbilityPower(player, statValues);
                String key = relicItem.getRelicName() + ":" + ability.name();
                AbilityExecutor executor = EXECUTORS.get(key);
                if (executor != null) {
                    executor.execute(player, stack, ability, statValues);
                    CombatTextSender.sendAbility(player, ability.name());
                    // Challenge hook: ability_use
                    com.ultra.megamod.feature.skills.challenges.SkillChallenges.addProgress(player, "ability_use", 1);
                    // Combat combo hook
                    com.ultra.megamod.feature.hud.combos.CombatComboTracker.onAbilityCast(player, ability.name());
                } else {
                    MegaMod.LOGGER.debug("No executor registered for ability: {}", (Object)key);
                }
                int baseCooldown = RelicData.getEffectiveCooldown(stack, ability.name(), DEFAULT_COOLDOWN);
                int adjustedCooldown = SkillsRelicIntegration.applyCooldownReduction(player, baseCooldown);
                AbilityCooldownManager.setCooldown(stack, ability.name(), adjustedCooldown);
                SkillsRelicIntegration.grantArcaneCastXp(player);
                AbilityCastHandler.updateStackInSlot(player, stack);
                break;
            }
            case TOGGLE: {
                boolean currentState = AbilitySystem.isToggleActive(player.getUUID(), ability.name());
                AbilitySystem.setToggleActive(player.getUUID(), ability.name(), !currentState);
                if (!currentState) {
                    CombatTextSender.sendAbility(player, ability.name() + " ON");
                    MegaMod.LOGGER.debug("Toggle ON: {} for {}", (Object)ability.name(), (Object)player.getGameProfile().name());
                    break;
                }
                int toggleCooldown = RelicData.getEffectiveCooldown(stack, ability.name(), 20);
                AbilityCooldownManager.setCooldown(stack, ability.name(), toggleCooldown);
                AbilityCastHandler.updateStackInSlot(player, stack);
            }
        }
    }

    private static double[] computeStatValues(ItemStack stack, RelicAbility ability) {
        List<RelicStat> stats = ability.stats();
        double[] values = new double[stats.size()];
        for (int i = 0; i < stats.size(); ++i) {
            values[i] = RelicData.getComputedStatValue(stack, ability.name(), stats.get(i));
        }
        return values;
    }

    private static ItemStack findRelicStack(ServerPlayer player, String slotName) {
        AccessorySlotType slot;
        if ("MAINHAND".equals(slotName)) {
            return player.getMainHandItem();
        }
        try {
            slot = AccessorySlotType.valueOf(slotName);
        }
        catch (IllegalArgumentException e) {
            return ItemStack.EMPTY;
        }
        if (slot == AccessorySlotType.NONE) {
            return ItemStack.EMPTY;
        }
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server == null) return ItemStack.EMPTY;
        ServerLevel overworld = server.overworld();
        AccessoryManager manager = AccessoryManager.get(overworld);
        return manager.getEquipped(player.getUUID(), slot);
    }

    /**
     * Resolves the Nth castable (non-PASSIVE, level-unlocked) ability on a relic.
     */
    private static String resolveAbilityByIndex(RelicItem relicItem, ItemStack stack, int targetIndex) {
        int relicLevel = RelicData.getLevel(stack);
        List<RelicAbility> abilities = relicItem.getAbilities();
        int currentIndex = 0;
        for (RelicAbility ability : abilities) {
            if (!RelicData.isAbilityUnlocked(relicLevel, ability, abilities) || ability.castType() == RelicAbility.CastType.PASSIVE) continue;
            if (currentIndex == targetIndex) {
                return ability.name();
            }
            ++currentIndex;
        }
        return null;
    }

    private static void updateStackInSlot(ServerPlayer player, ItemStack stack) {
        Item item = stack.getItem();
        if (!(item instanceof RelicItem)) {
            return;
        }
        net.minecraft.server.MinecraftServer server = player.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        AccessoryManager manager = AccessoryManager.get(overworld);
        Map<AccessorySlotType, ItemStack> equipped = manager.getAllEquipped(player.getUUID());
        for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
            if (!ItemStack.isSameItem((ItemStack)entry.getValue(), (ItemStack)stack)) continue;
            manager.setEquipped(player.getUUID(), entry.getKey(), stack);
            break;
        }
    }

    @FunctionalInterface
    public static interface AbilityExecutor {
        public void execute(ServerPlayer var1, ItemStack var2, RelicAbility var3, double[] var4);
    }
}
