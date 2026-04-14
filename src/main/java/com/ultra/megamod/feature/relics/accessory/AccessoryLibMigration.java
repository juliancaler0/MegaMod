package com.ultra.megamod.feature.relics.accessory;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One-shot migration from the retired {@code megamod_accessories.dat} SavedData file
 * into the {@link com.ultra.megamod.lib.accessories.api.AccessoriesCapability}.
 *
 * <p>On first login after upgrade, each player's legacy entries are written into the
 * lib capability and removed from the in-memory pending map. Anything still pending
 * on server stop (offline players) is re-serialized back to disk so it survives
 * across restarts. When the pending map drains, the legacy file is deleted.</p>
 *
 * <p>This class also owns the per-login {@link LibAccessoryLookup#syncToClient} push
 * so the MegaMod HUD overlays (ability bar, equipment stats, etc.) populate their
 * client-side mirror when a player joins.</p>
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AccessoryLibMigration {

    private static final String LEGACY_FILE_NAME = "megamod_accessories.dat";
    private static final AtomicBoolean legacyLoaded = new AtomicBoolean(false);
    private static final Map<UUID, EnumMap<AccessorySlotType, ItemStack>> pending = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var server = player.level().getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();

        ensureLegacyLoaded(overworld);

        EnumMap<AccessorySlotType, ItemStack> forPlayer = pending.remove(player.getUUID());
        if (forPlayer != null) {
            for (Map.Entry<AccessorySlotType, ItemStack> entry : forPlayer.entrySet()) {
                LibAccessoryLookup.setEquipped(player, entry.getKey(), entry.getValue());
            }
            MegaMod.LOGGER.info("Migrated {} legacy accessory slot(s) for {} into lib/accessories",
                    forPlayer.size(), player.getGameProfile().name());
        }

        LibAccessoryLookup.syncToClient(player);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        if (!legacyLoaded.get()) return;
        persistPending(event.getServer().overworld());
    }

    private static synchronized void ensureLegacyLoaded(ServerLevel level) {
        if (!legacyLoaded.compareAndSet(false, true)) return;
        Path legacyFile = legacyFilePath(level);
        if (!Files.exists(legacyFile)) return;

        try {
            CompoundTag root = NbtIo.readCompressed(legacyFile, NbtAccounter.unlimitedHeap());
            CompoundTag players = root.getCompoundOrEmpty("players");
            for (String uuidStr : players.keySet()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException skip) {
                    continue;
                }
                CompoundTag playerTag = players.getCompoundOrEmpty(uuidStr);
                EnumMap<AccessorySlotType, ItemStack> slots = new EnumMap<>(AccessorySlotType.class);
                for (AccessorySlotType slot : AccessorySlotType.values()) {
                    if (slot == AccessorySlotType.NONE) continue;
                    CompoundTag slotTag = playerTag.getCompoundOrEmpty(slot.name());
                    if (slotTag.isEmpty()) continue;
                    String itemId = slotTag.getStringOr("item", "");
                    if (itemId.isEmpty()) continue;
                    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
                    if (item == null) continue;
                    ItemStack stack = new ItemStack(item);
                    CompoundTag data = slotTag.getCompoundOrEmpty("data");
                    if (!data.isEmpty()) {
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
                    }
                    slots.put(slot, stack);
                }
                if (!slots.isEmpty()) pending.put(uuid, slots);
            }
            if (!pending.isEmpty()) {
                MegaMod.LOGGER.info("Queued {} player(s) from legacy {} for lib/accessories migration",
                        pending.size(), LEGACY_FILE_NAME);
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to read legacy accessory file {}", LEGACY_FILE_NAME, e);
        }
    }

    private static void persistPending(ServerLevel level) {
        Path legacyFile = legacyFilePath(level);
        try {
            if (pending.isEmpty()) {
                Files.deleteIfExists(legacyFile);
                return;
            }
            CompoundTag root = new CompoundTag();
            CompoundTag players = new CompoundTag();
            for (Map.Entry<UUID, EnumMap<AccessorySlotType, ItemStack>> entry : pending.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                for (Map.Entry<AccessorySlotType, ItemStack> slotEntry : entry.getValue().entrySet()) {
                    ItemStack stack = slotEntry.getValue();
                    if (stack.isEmpty()) continue;
                    CompoundTag slotTag = new CompoundTag();
                    slotTag.putString("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                    if (stack.has(DataComponents.CUSTOM_DATA)) {
                        slotTag.put("data", stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
                    }
                    playerTag.put(slotEntry.getKey().name(), slotTag);
                }
                if (!playerTag.isEmpty()) players.put(entry.getKey().toString(), playerTag);
            }
            root.put("players", players);
            Files.createDirectories(legacyFile.getParent());
            NbtIo.writeCompressed(root, legacyFile);
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to persist remaining legacy accessory data", e);
        }
    }

    private static Path legacyFilePath(ServerLevel level) {
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("data").resolve(LEGACY_FILE_NAME);
    }
}
