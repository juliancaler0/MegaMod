package com.ultra.megamod.feature.relics.ability;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.ability.AbilityCastHandler;
import com.ultra.megamod.feature.relics.ability.AbilityCooldownManager;
import com.ultra.megamod.feature.relics.ability.belt.HunterBeltAbility;
import com.ultra.megamod.feature.relics.ability.feet.IceBreakerAbility;
import com.ultra.megamod.feature.relics.ability.hands.RageGloveAbility;
import com.ultra.megamod.feature.relics.ability.necklace.ReflectionNecklaceAbility;
import com.ultra.megamod.feature.relics.accessory.LibAccessoryLookup;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicAttributeBonus;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import com.ultra.megamod.feature.relics.network.AbilityCooldownSyncPayload;
import com.ultra.megamod.feature.relics.network.WeaponAbilitySyncPayload;
import com.ultra.megamod.feature.combat.spell.SpellAbilityBridge;
import com.ultra.megamod.feature.combat.spell.SpellExecutor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid="megamod")
public class AbilitySystem {
    private static final Map<UUID, Map<String, Boolean>> ACTIVE_TOGGLES = new ConcurrentHashMap<UUID, Map<String, Boolean>>();
    private static final Map<UUID, Long> INVULN_EXPIRY = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, String> LAST_HELD_ITEM = new ConcurrentHashMap<>();
    private static final Set<UUID> PLAYERS_WITH_ACTIVE_CDS = new HashSet<>();
    private static final Set<UUID> PLAYERS_WITH_WEAPON_CDS = new HashSet<>();
    private static final int COOLDOWN_SYNC_INTERVAL = 5;

    public static boolean isToggleActive(UUID playerId, String abilityName) {
        Map<String, Boolean> toggles = ACTIVE_TOGGLES.get(playerId);
        if (toggles == null) {
            return false;
        }
        return toggles.getOrDefault(abilityName, false);
    }

    public static void setToggleActive(UUID playerId, String abilityName, boolean active) {
        Map toggles = ACTIVE_TOGGLES.computeIfAbsent(playerId, k -> new ConcurrentHashMap());
        if (active) {
            toggles.put(abilityName, true);
        } else {
            toggles.remove(abilityName);
        }
    }

    public static void clearToggles(UUID playerId) {
        ACTIVE_TOGGLES.remove(playerId);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        ACTIVE_TOGGLES.remove(id);
        INVULN_EXPIRY.remove(id);
        LAST_HELD_ITEM.remove(id);
        PLAYERS_WITH_ACTIVE_CDS.remove(id);
        PLAYERS_WITH_WEAPON_CDS.remove(id);
    }

    public static Map<String, Boolean> getActiveToggles(UUID playerId) {
        Map<String, Boolean> toggles = ACTIVE_TOGGLES.get(playerId);
        if (toggles == null) {
            return Map.of();
        }
        return Map.copyOf(toggles);
    }

    public static void scheduleInvulnExpiry(UUID playerId, long expiryTick) {
        INVULN_EXPIRY.put(playerId, expiryTick);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        long gameTime = overworld.getGameTime();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerId = player.getUUID();
            Long invulnExpiry = INVULN_EXPIRY.get(playerId);
            if (invulnExpiry != null && gameTime >= invulnExpiry) {
                player.setInvulnerable(false);
                INVULN_EXPIRY.remove(playerId);
            }
            Map<AccessorySlotType, ItemStack> equipped = LibAccessoryLookup.getAllEquipped(player);
            CompoundTag cooldownTag = new CompoundTag();
            for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
                Item item;
                ItemStack stack = entry.getValue();
                if (stack.isEmpty() || !((item = stack.getItem()) instanceof RelicItem)) continue;
                RelicItem relicItem = (RelicItem)item;
                if (!RelicData.isInitialized(stack)) continue;
                int relicLevel = RelicData.getLevel(stack);
                CompoundTag cooldownsBefore = AbilityCooldownManager.getAllCooldowns(stack);
                if (!cooldownsBefore.isEmpty()) {
                    AbilityCooldownManager.tickCooldowns(stack);
                }
                List<RelicAbility> abilities = relicItem.getAbilities();
                for (RelicAbility ability : abilities) {
                    int cd;
                    if (!RelicData.isAbilityUnlocked(relicLevel, ability, abilities)) continue;
                    if (ability.castType() == RelicAbility.CastType.PASSIVE) {
                        AbilitySystem.tickPassiveAbility(player, stack, ability, relicItem);
                    }
                    if (ability.castType() == RelicAbility.CastType.TOGGLE && AbilitySystem.isToggleActive(playerId, ability.name())) {
                        AbilitySystem.tickToggleAbility(player, stack, ability, relicItem);
                    }
                    if ((cd = AbilityCooldownManager.getCooldown(stack, ability.name())) <= 0) continue;
                    cooldownTag.putInt(ability.name(), cd);
                }
                // Apply relic attribute bonuses every 20 ticks. The stack ref is live from the
                // lib container, so in-place NBT mutations (cooldowns, etc.) are already visible.
                if (gameTime % 20L == 0L) {
                    applyAttributeBonuses(player, stack, entry.getKey());
                }
            }

            // Also include held RelicItem weapon cooldowns in the same sync
            ItemStack held = player.getMainHandItem();
            if (!held.isEmpty() && held.getItem() instanceof RelicItem heldRelic) {
                if (RelicData.isInitialized(held)) {
                    CompoundTag heldCooldowns = AbilityCooldownManager.getAllCooldowns(held);
                    if (!heldCooldowns.isEmpty()) {
                        AbilityCooldownManager.tickCooldowns(held);
                    }
                    for (RelicAbility ability : heldRelic.getAbilities()) {
                        if (ability.castType() == RelicAbility.CastType.PASSIVE) continue;
                        int cd = AbilityCooldownManager.getCooldown(held, ability.name());
                        if (cd <= 0) continue;
                        cooldownTag.putInt(ability.name(), cd);
                    }
                }
            }

            // === Weapon spell cooldown sync (SpellAbilityBridge-mapped wands/staves) ===
            // Phase H: manual RPG weapon skills removed; only SpellEngine-mapped spell cooldowns sync here.
            String heldRegistryName = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();

            if (gameTime % 5L == 0L) {
                // Send relic cooldown sync — also send when cooldowns clear so client removes stale display
                if (!cooldownTag.isEmpty()) {
                    PLAYERS_WITH_ACTIVE_CDS.add(playerId);
                    AbilityCooldownSyncPayload payload = new AbilityCooldownSyncPayload(cooldownTag);
                    PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)payload, (CustomPacketPayload[])new CustomPacketPayload[0]);
                } else if (PLAYERS_WITH_ACTIVE_CDS.remove(playerId)) {
                    // Send empty sync so client clears stale cooldowns
                    AbilityCooldownSyncPayload payload = new AbilityCooldownSyncPayload(new CompoundTag());
                    PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)payload, (CustomPacketPayload[])new CustomPacketPayload[0]);
                }

                // Sync spell cooldowns for weapons mapped via SpellAbilityBridge (wands/staves tooltip HUD).
                String spellId = SpellAbilityBridge.getSpellForWeapon(heldRegistryName);
                if (spellId != null) {
                    CompoundTag weaponCdTag = new CompoundTag();
                    float spellCdRemaining = SpellExecutor.getCooldownRemaining(player.getUUID(), spellId);
                    if (spellCdRemaining > 0) {
                        weaponCdTag.putInt(spellId, (int)(spellCdRemaining * 20)); // seconds -> ticks
                    }
                    if (!weaponCdTag.isEmpty()) {
                        PLAYERS_WITH_WEAPON_CDS.add(playerId);
                        WeaponAbilitySyncPayload weaponPayload = new WeaponAbilitySyncPayload(weaponCdTag);
                        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)weaponPayload, (CustomPacketPayload[])new CustomPacketPayload[0]);
                    } else if (PLAYERS_WITH_WEAPON_CDS.remove(playerId)) {
                        WeaponAbilitySyncPayload weaponPayload = new WeaponAbilitySyncPayload(new CompoundTag());
                        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)weaponPayload, (CustomPacketPayload[])new CustomPacketPayload[0]);
                    }
                } else if (PLAYERS_WITH_WEAPON_CDS.remove(playerId)) {
                    WeaponAbilitySyncPayload weaponPayload = new WeaponAbilitySyncPayload(new CompoundTag());
                    PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)weaponPayload, (CustomPacketPayload[])new CustomPacketPayload[0]);
                }
            }
        }
    }

    private static void tickPassiveAbility(ServerPlayer player, ItemStack stack, RelicAbility ability, RelicItem relicItem) {
        String key = relicItem.getRelicName() + ":" + ability.name();
        AbilityCastHandler.AbilityExecutor executor = AbilitySystem.getPassiveExecutor(key);
        if (executor != null) {
            double[] statValues = AbilitySystem.computeStatValues(stack, ability);
            executor.execute(player, stack, ability, statValues);
        }
    }

    private static void tickToggleAbility(ServerPlayer player, ItemStack stack, RelicAbility ability, RelicItem relicItem) {
        String key = relicItem.getRelicName() + ":" + ability.name();
        AbilityCastHandler.AbilityExecutor executor = AbilitySystem.getPassiveExecutor(key);
        if (executor != null) {
            double[] statValues = AbilitySystem.computeStatValues(stack, ability);
            executor.execute(player, stack, ability, statValues);
        }
    }

    private static AbilityCastHandler.AbilityExecutor getPassiveExecutor(String key) {
        return AbilityCastHandler.getExecutor(key);
    }

    private static double[] computeStatValues(ItemStack stack, RelicAbility ability) {
        List<RelicStat> stats = ability.stats();
        double[] values = new double[stats.size()];
        for (int i = 0; i < stats.size(); ++i) {
            values[i] = RelicData.getComputedStatValue(stack, ability.name(), stats.get(i));
        }
        return values;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Get victim and attacker
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // === VICTIM RELIC ABILITIES (triggered when the relic wearer takes damage) ===
        if (victim instanceof ServerPlayer victimPlayer) {
            Map<AccessorySlotType, ItemStack> equipped = LibAccessoryLookup.getAllEquipped(victimPlayer);
            for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
                ItemStack stack = entry.getValue();
                if (stack.isEmpty() || !(stack.getItem() instanceof RelicItem relicItem)) continue;
                if (!RelicData.isInitialized(stack)) continue;
                int level = RelicData.getLevel(stack);
                String relicName = relicItem.getRelicName();

                List<RelicAbility> victimAbilities = relicItem.getAbilities();

                // Ice Breaker - Frost Thorns: chance to freeze attacker
                if ("Ice Breaker".equals(relicName) && event.getSource().getEntity() instanceof LivingEntity attacker) {
                    for (RelicAbility ability : victimAbilities) {
                        if (!"Frost Thorns".equals(ability.name()) || !RelicData.isAbilityUnlocked(level, ability, victimAbilities)) continue;
                        double[] stats = computePassiveStats(stack, ability);
                        IceBreakerAbility.executeFrostThornsOnHit(victimPlayer, attacker, stats);
                    }
                }

                // Reflection Necklace - Absorb: store portion of damage.
                // The stack reference is live from the lib container, so the NBT mutation
                // done by executeAbsorbOnHit is already reflected in storage — no write-back needed.
                if ("Reflection Necklace".equals(relicName)) {
                    for (RelicAbility ability : victimAbilities) {
                        if (!"Absorb".equals(ability.name()) || !RelicData.isAbilityUnlocked(level, ability, victimAbilities)) continue;
                        double[] stats = computePassiveStats(stack, ability);
                        ReflectionNecklaceAbility.executeAbsorbOnHit(victimPlayer, stack, event.getOriginalDamage(), stats);
                    }
                }
            }
        }

        // === ATTACKER RELIC ABILITIES (triggered when the relic wearer deals damage) ===
        if (event.getSource().getEntity() instanceof ServerPlayer attackerPlayer) {
            Map<AccessorySlotType, ItemStack> equipped = LibAccessoryLookup.getAllEquipped(attackerPlayer);
            for (Map.Entry<AccessorySlotType, ItemStack> entry : equipped.entrySet()) {
                ItemStack stack = entry.getValue();
                if (stack.isEmpty() || !(stack.getItem() instanceof RelicItem relicItem)) continue;
                if (!RelicData.isInitialized(stack)) continue;
                int level = RelicData.getLevel(stack);
                String relicName = relicItem.getRelicName();

                List<RelicAbility> attackerAbilities = relicItem.getAbilities();

                // Rage Glove - Fury: add hit stack
                if ("Rage Glove".equals(relicName)) {
                    for (RelicAbility ability : attackerAbilities) {
                        if (!"Fury".equals(ability.name()) || !RelicData.isAbilityUnlocked(level, ability, attackerAbilities)) continue;
                        RageGloveAbility.addFuryStack(stack, (ServerLevel) attackerPlayer.level());
                    }
                }

                // Hunter Belt - Predator: bonus damage to marked target
                if ("Hunter Belt".equals(relicName)) {
                    for (RelicAbility ability : attackerAbilities) {
                        if (!"Predator".equals(ability.name()) || !RelicData.isAbilityUnlocked(level, ability, attackerAbilities)) continue;
                        if (HunterBeltAbility.isMarkedTarget(attackerPlayer, event.getEntity())) {
                            double[] stats = computePassiveStats(stack, ability);
                            float bonusDmg = event.getOriginalDamage() * (float)(stats.length > 0 ? stats[0] / 100.0 : 0.2);
                            event.setNewDamage(event.getNewDamage() + bonusDmg);
                        }
                    }
                }
            }
        }
    }

    private static double[] computePassiveStats(ItemStack stack, RelicAbility ability) {
        List<RelicStat> stats = ability.stats();
        double[] values = new double[stats.size()];
        for (int i = 0; i < stats.size(); ++i) {
            values[i] = RelicData.getComputedStatValue(stack, ability.name(), stats.get(i));
        }
        return values;
    }

    private static void applyAttributeBonuses(ServerPlayer player, ItemStack stack, AccessorySlotType slotType) {
        List<RelicAttributeBonus> bonuses = RelicData.getAttributeBonuses(stack);
        String slotName = slotType.name().toLowerCase();
        for (int i = 0; i < bonuses.size(); i++) {
            RelicAttributeBonus bonus = bonuses.get(i);
            RelicAttributeBonus.BonusPool pool = RelicAttributeBonus.getPoolEntry(bonus.attributeId());
            if (pool == null) continue;
            Identifier modifierId = bonus.getModifierId(slotName, i);
            AttributeInstance attr = player.getAttribute(pool.attribute());
            if (attr == null) continue;
            attr.removeModifier(modifierId);
            attr.addTransientModifier(new AttributeModifier(modifierId, bonus.value(), pool.operation()));
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            UUID uuid = player.getUUID();
            AbilitySystem.clearToggles(uuid);
            INVULN_EXPIRY.remove(uuid);
            LAST_HELD_ITEM.remove(uuid);
            PLAYERS_WITH_ACTIVE_CDS.remove(uuid);
            PLAYERS_WITH_WEAPON_CDS.remove(uuid);
        }
    }
}
