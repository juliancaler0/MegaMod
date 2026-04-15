package com.ultra.megamod.lib.accessories.networking.client;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.api.AccessoriesContainer;
import com.ultra.megamod.lib.accessories.api.slot.SlotPath;
import com.ultra.megamod.lib.accessories.endec.NbtMapCarrier;
import com.ultra.megamod.lib.accessories.impl.core.AccessoriesContainerImpl;
import com.ultra.megamod.lib.accessories.impl.core.AccessoriesHolderImpl;
import com.ultra.megamod.lib.accessories.menu.variants.AccessoriesMenuBase;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.serialization.CodecUtils;
import com.ultra.megamod.lib.accessories.owo.serialization.RegistriesAttribute;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.*;

/**
 * Catch all packet for handling syncing of containers and accessories within the main container
 * and cosmetic variant with the ability for it to be sync separately
 */
public record SyncContainerData(int entityId, Map<String, NbtMapCarrier> updatedContainers, Map<SlotPath, ItemStack> dirtyStacks, Map<SlotPath, ItemStack> dirtyCosmeticStacks) {

    // The upstream endec (Endec.map(SlotPath.ENDEC, …)) builds a Codec.unboundedMap
    // whose key codec is the struct SlotPath.CODEC. NBT requires map keys to be
    // strings — Codec.unboundedMap calls keyCodec.encodeStart then expects its
    // output to be a StringTag; a CompoundTag blows up with "Not a string". That
    // broke every equip attempt server→client sync with:
    //   EncoderException: Failed to encode: Not a string SyncContainerData[…]
    // Fix: keep SlotPath on the API side but serialize the map with string keys
    // using SlotPath.createString / SlotPath.fromString.
    private static final Endec<Map<SlotPath, ItemStack>> PATH_TO_STACK_ENDEC =
            new Endec<>() {
                @Override
                public com.mojang.serialization.Codec<Map<SlotPath, ItemStack>> codec() {
                    var valueCodec = ItemStack.OPTIONAL_CODEC;
                    var stringKeyedMap = com.mojang.serialization.Codec.unboundedMap(
                            com.mojang.serialization.Codec.STRING,
                            valueCodec);
                    return stringKeyedMap.xmap(
                            m -> {
                                var out = new HashMap<SlotPath, ItemStack>();
                                m.forEach((k, v) -> {
                                    var p = SlotPath.fromString(k);
                                    if (p != null) out.put(p, v);
                                });
                                return out;
                            },
                            m -> {
                                var out = new HashMap<String, ItemStack>();
                                m.forEach((k, v) -> out.put(k.createString(), v));
                                return out;
                            });
                }
            };

    public static final StructEndec<SyncContainerData> ENDEC = StructEndecBuilder.of(
            Endec.VAR_INT.fieldOf("entityId", SyncContainerData::entityId),
            NbtMapCarrier.ENDEC.mapOf().fieldOf("updatedContainers", SyncContainerData::updatedContainers),
            PATH_TO_STACK_ENDEC.fieldOf("dirtyStacks", SyncContainerData::dirtyStacks),
            PATH_TO_STACK_ENDEC.fieldOf("dirtyCosmeticStacks", SyncContainerData::dirtyCosmeticStacks),
            SyncContainerData::new
    );

    public static SyncContainerData of(LivingEntity livingEntity, Collection<AccessoriesContainer> updatedContainers, Map<SlotPath, ItemStack> dirtyStacks, Map<SlotPath, ItemStack> dirtyCosmeticStacks){
        var updatedContainerTags = new HashMap<String, NbtMapCarrier>();

        for (AccessoriesContainer updatedContainer : updatedContainers) {
            var syncCarrier = NbtMapCarrier.of();

            ((AccessoriesContainerImpl) updatedContainer).write(syncCarrier, SerializationContext.attributes(RegistriesAttribute.of(livingEntity.registryAccess())), true);

            updatedContainerTags.put(updatedContainer.getSlotName(), syncCarrier);
        }

        return new SyncContainerData(livingEntity.getId(), updatedContainerTags, dirtyStacks, dirtyCosmeticStacks);
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    //@Environment(EnvType.CLIENT)
    public static void handlePacket(SyncContainerData packet, Player player) {
        var level = player.level();

        var entity = level.getEntity(packet.entityId());

        if(entity == null) {
            LOGGER.error("Unable to Sync Container Data for a given Entity as it is null on the Client! [EntityId: {}]", packet.entityId());

            return;
        }

        if(!(entity instanceof LivingEntity livingEntity)) return;

        var capability = AccessoriesCapability.get(livingEntity);

        if(capability == null) {
            LOGGER.error("Unable to Sync Container Data for a given Entity as its Capability is null on the Client! [EntityId: {}]", packet.entityId());

            return;
        }

        var containers = capability.getContainers();

        var aContainerHasResized = false;

        Set<String> changedContainers = new HashSet<>();

        //--

        Set<String> invalidSyncedContainers = new HashSet<>();

        for (var entry : packet.updatedContainers().entrySet()) {
            if (!containers.containsKey(entry.getKey())) {
                invalidSyncedContainers.add(entry.getKey());

                continue;
            }

            var container = containers.get(entry.getKey());

            changedContainers.add(container.getSlotName());

            ((AccessoriesContainerImpl) container).read(entry.getValue(), SerializationContext.attributes(RegistriesAttribute.of(player.level().registryAccess())), true);

            if (container.getAccessories().wasNewlyConstructed()) aContainerHasResized = true;
        }

        if(!invalidSyncedContainers.isEmpty()) {
            LOGGER.warn("Unable to sync container data for the following containers: {}", invalidSyncedContainers);
        }

        //--

        Set<String> invalidDirtyStackContainers = new HashSet<>();

        for (var entry : packet.dirtyStacks().entrySet()) {
            var path = entry.getKey();
            var slot = path.slotName();

            if(!containers.containsKey(slot)) {
                invalidDirtyStackContainers.add(slot);

                continue;
            }

            var container = containers.get(slot);

            changedContainers.add(container.getSlotName());

            try {
                container.getAccessories().setItem(path.index(), entry.getValue());
            } catch (NumberFormatException ignored){}
        }

        if(!invalidDirtyStackContainers.isEmpty()) {
            LOGGER.warn("Unable to sync dirty stack data for the following containers: {}", invalidSyncedContainers);
        }

        //--

        Set<String> invalidDirtyCosmeticContainers = new HashSet<>();

        for (var entry : packet.dirtyCosmeticStacks().entrySet()) {
            var path = entry.getKey();
            var slot = path.slotName();

            if(!containers.containsKey(slot)) {
                invalidDirtyCosmeticContainers.add(slot);

                continue;
            }

            var container = containers.get(slot);

            changedContainers.add(container.getSlotName());

            try {
                container.getCosmeticAccessories().setItem(path.index(), entry.getValue());
            } catch (NumberFormatException ignored){}
        }

        if(!invalidDirtyCosmeticContainers.isEmpty()) {
            LOGGER.warn("Unable to sync dirty stack data for the following containers: {}", invalidSyncedContainers);
        }

        //--

        var cache = AccessoriesHolderImpl.getHolder(capability).getLookupCache();

        if (cache != null) {
            changedContainers.forEach(cache::clearContainerCache);
        }

        if(player.containerMenu instanceof AccessoriesMenuBase menu && aContainerHasResized) {
            menu.reopenMenu();
            //AccessoriesClient.attemptToOpenScreen();
        }
    }
}
