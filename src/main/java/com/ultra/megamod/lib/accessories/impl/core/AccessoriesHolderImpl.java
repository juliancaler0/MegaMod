package com.ultra.megamod.lib.accessories.impl.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.logging.LogUtils;
import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.AccessoriesInternals;
import com.ultra.megamod.lib.accessories.api.AccessoriesCapability;
import com.ultra.megamod.lib.accessories.api.AccessoriesContainer;
import com.ultra.megamod.lib.accessories.data.EntitySlotLoader;
import com.ultra.megamod.lib.accessories.endec.NbtMapCarrier;
import com.ultra.megamod.lib.accessories.impl.caching.AccessoriesHolderLookupCache;
import com.ultra.megamod.lib.accessories.impl.option.AccessoriesPlayerOptionsHolder;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOption;
import com.ultra.megamod.lib.accessories.pond.AccessoriesLivingEntityExtension;
import com.ultra.megamod.lib.accessories.utils.EndecUtils;
import com.ultra.megamod.lib.accessories.utils.InstanceEndec;
import com.ultra.megamod.lib.accessories.utils.ValidatingForwardingMap;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationAttribute;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.KeyedEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrier;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierDecodable;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierEncodable;
import com.ultra.megamod.lib.accessories.owo.serialization.RegistriesAttribute;
import com.ultra.megamod.lib.accessories.owo.serialization.format.nbt.NbtEndec;
import net.minecraft.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@ApiStatus.Internal
public class AccessoriesHolderImpl implements InstanceEndec {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final MapCarrier EMPTY = new NbtMapCarrier(new CompoundTag());

    private final Map<String, AccessoriesContainer> slotContainers = new LinkedHashMap<>();
    private final Map<String, AccessoriesContainer> slotContainersView = Collections.unmodifiableMap(this.getAllSlotContainers());

    public final List<ItemStack> invalidStacks = new ArrayList<>();

    private final Map<AccessoriesContainer, Boolean> containersRequiringUpdates = new HashMap<>();

    // --

    private MapCarrierDecodable carrier;
    protected boolean loadedFromTag = false;

    public AccessoriesHolderImpl(){}

    public boolean loadedFromTag() {
        return loadedFromTag;
    }

    public Map<AccessoriesContainer, Boolean> containersRequiringUpdates() {
        return containersRequiringUpdates;
    }

    public static AccessoriesHolderImpl of(){
        var holder = new AccessoriesHolderImpl();

        holder.loadedFromTag = true;
        holder.carrier = EMPTY;

        return holder;
    }

    @Nullable
    public static AccessoriesHolderImpl getHolder(LivingEntity livingEntity) {
        var capability = ((AccessoriesLivingEntityExtension)livingEntity).getOrCreateAccessoriesCapability();

        if (capability == null) return null;

        return getHolder(capability);
    }


    public static AccessoriesHolderImpl getHolder(AccessoriesCapability capability) {
        var entity = capability.entity();

        var holder = AccessoriesInternals.INSTANCE.getHolder(entity);

        // If data has been yet to be loaded
        if (holder.loadedFromTag) {
            if (entity.level().isClientSide()) {
                // Will init containers from data
                holder.init(capability);
            } else {
                // Reset the container when loaded from tag on the server
                capability.reset(true);
            }
        } else if (!isEntitySlotsValid(entity, holder)) {
            // Prevents containers from not existing even if a given entity will have such slots but have yet to be synced to the client
            holder.init(capability);
        }

        return holder;
    }

    private static final Cache<Integer, Boolean> validatedServerEntities = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .build();

    private static final Cache<Integer, Boolean> validatedClientEntities = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(30))
        .build();

    private static boolean isEntitySlotsValid(LivingEntity entity, AccessoriesHolderImpl holder){
        var validEntities = entity.level().isClientSide()
            ? validatedClientEntities
            : validatedServerEntities;

        var hash = Objects.hash(entity.getUUID(), entity.hashCode());

        var result = validEntities.getIfPresent(hash);

        if (result != null) {
            if (result) return true;

            validEntities.invalidate(hash);
        }

        var currentContainers = holder.getSlotContainers();
        var requiredSlotTypes = EntitySlotLoader.getEntitySlots(entity);

        result = currentContainers.size() == requiredSlotTypes.size();

        if (result) validEntities.put(hash, true);

        return result;
    }

    public static void clearValidationCache(boolean isClientSide) {
        (isClientSide ? validatedClientEntities : validatedServerEntities).invalidateAll();
    }

    //--

    @ApiStatus.Internal
    public Map<String, AccessoriesContainer> getAllSlotContainers() {
        return Collections.unmodifiableMap(this.slotContainers);
    }

    @Nullable
    private Set<String> validSlotTypes = null;

    @Nullable
    private final Map<String, AccessoriesContainer> validSlotContainers = new ValidatingForwardingMap<>(
        this.slotContainers,
        String.class, AccessoriesContainer.class,
        s -> this.validSlotTypes == null || this.validSlotTypes.contains(s), AccessoriesContainer::getSlotName);

    public void setValidTypes(Set<String> validTypes) {
        if (this.currentlyInitializingHolder.isLockedNotByOwner(Thread.currentThread())) return;

        this.validSlotTypes = this.slotContainers.keySet().containsAll(validTypes) ? null : validTypes;
    }

    @ApiStatus.Internal
    public Map<String, AccessoriesContainer> getSlotContainers() {
        return this.validSlotTypes != null ? this.validSlotContainers : this.slotContainersView;
    }

    @Nullable
    public AccessoriesHolderLookupCache getLookupCache() {
        return null;
    }

    //--

    private final OwnerAccessibleReentrantLock currentlyInitializingHolder = new OwnerAccessibleReentrantLock();

    public void init(AccessoriesCapability capability) {
        var livingEntity = capability.entity();

        //this.slotContainers.clear();

        var entitySlots = EntitySlotLoader.getEntitySlots(livingEntity);

        //LOGGER.error("Entity Slots for [{}]: {}", livingEntity, entitySlots.keySet());

        if(livingEntity instanceof Player && entitySlots.isEmpty()) {
            LOGGER.warn("It seems the given player has no slots bound to it within a init call, is that desired?");
        }

        this.validSlotTypes = null;

        // Prevent nested init calls on the same thread as this really is not good idea nor makes any sense
        if (this.currentlyInitializingHolder.isLockedByOwner(Thread.currentThread())) {
            return;
        }

        try {
            this.currentlyInitializingHolder.lock();

            if (loadedFromTag) {
                entitySlots.forEach((s, slotType) -> {
                    this.slotContainers.putIfAbsent(s, new AccessoriesContainerImpl(capability, slotType));
                });

                var ctx = SerializationContext.attributes(
                        new EntityAttribute(livingEntity),
                        RegistriesAttribute.of(livingEntity.registryAccess())
                );

                read(capability, livingEntity, this.carrier, ctx);
            } else {
                entitySlots.forEach((s, slotType) -> {
                    this.slotContainers.put(s, new AccessoriesContainerImpl(capability, slotType));
                });
            }
        } finally {
            this.currentlyInitializingHolder.unlock();
        }

        this.setValidTypes(entitySlots.keySet());
    }

    // TODO: SPLIT DECODING AND VALIDATION SAFETY DOWN THE ROAD
    private static final KeyedEndec<Map<String, AccessoriesContainer>> CONTAINERS_KEY = NbtEndec.COMPOUND.xmapWithContext(
            (ctx, containersMap) -> {
                var entity = ctx.requireAttributeValue(EntityAttribute.ENTITY).livingEntity();
                var slotContainers = ctx.requireAttributeValue(ContainersAttribute.CONTAINERS).slotContainers();
                var invalidStacks = ctx.requireAttributeValue(InvalidStacksAttribute.INVALID_STACKS).invalidStacks();

                var slots = EntitySlotLoader.getEntitySlots(entity);

                for (var key : containersMap.keySet()) {
                    var containerElement = containersMap.getCompoundOrEmpty(key);

                    if (containerElement.isEmpty()) continue; // TODO: Handle this case?

                    if (slots.containsKey(key)) {
                        var container = slotContainers.get(key);
                        var prevAccessories = AccessoriesContainerImpl.copyContainerList(container.getAccessories());
                        var prevCosmetics = AccessoriesContainerImpl.copyContainerList(container.getCosmeticAccessories());

                        ((AccessoriesContainerImpl) container).decode(new NbtMapCarrier(containerElement), ctx);

                        if (prevAccessories.getContainerSize() > container.getSize()) {
                            for (int i = container.getSize() - 1; i < prevAccessories.getContainerSize(); i++) {
                                var prevStack = prevAccessories.getItem(i);

                                if (!prevStack.isEmpty()) invalidStacks.add(prevStack);

                                var prevCosmetic = prevCosmetics.getItem(i);

                                if (!prevCosmetic.isEmpty()) invalidStacks.add(prevCosmetic);
                            }
                        }
                    } else {
                        var containers = AccessoriesContainerImpl.readContainers(
                            new NbtMapCarrier(containerElement),
                            ctx,
                            AccessoriesContainerImpl.COSMETICS_KEY, AccessoriesContainerImpl.ITEMS_KEY);

                        for (var simpleContainer : containers) {
                            for (int i = 0; i < simpleContainer.getContainerSize(); i++) {
                                var stack = simpleContainer.getItem(i);

                                if (!stack.isEmpty()) invalidStacks.add(stack);
                            }
                        }
                    }
                }

                return slotContainers;
            }, (ctx, containers) -> {
                var containerMap = new CompoundTag();

                containers.forEach((s, container) -> {
                    containerMap.put(s, Util.make(NbtMapCarrier.of(), innerCarrier -> ((AccessoriesContainerImpl) container).encode(innerCarrier, ctx)).compoundTag());
                });

                return containerMap;
            }).keyed("accessories_containers", HashMap::new);

    @Override
    public void encode(MapCarrierEncodable carrier, SerializationContext ctx) {
        if(slotContainers.isEmpty()) return;

        carrier.put(ctx, CONTAINERS_KEY, this.slotContainers);
    }

    public void read(LivingEntity entity, MapCarrier carrier, SerializationContext ctx) {
        read(((com.ultra.megamod.lib.accessories.pond.AccessoriesAPIAccess) entity).accessoriesCapability(), entity, carrier, ctx);
    }

    public void read(AccessoriesCapability capability, LivingEntity entity, MapCarrierDecodable carrier, SerializationContext ctx) {
        this.loadedFromTag = false;

        EndecUtils.dfuKeysCarrier(
                carrier,
                Map.of(
                        "AccessoriesContainers", "accessories_containers",
                        "CosmeticsShown", "cosmetics_shown",
                        "LinesShown", "lines_shown",
                        "EquipControl", "equip_control"
                ));

        // Include EntityAttribute — the CONTAINERS_KEY decoder requireAttributeValue's
        // it; without this the client decode path threw "Required serialization
        // attribute not found: entity" during SyncEntireContainer.handlePacket.
        carrier.getWithErrors(ctx.withAttributes(new EntityAttribute(entity), new ContainersAttribute(this.slotContainers), new InvalidStacksAttribute(this.invalidStacks)), CONTAINERS_KEY);

        this.setValidTypes(EntitySlotLoader.getEntitySlots(entity).keySet());

        capability.clearCachedSlotModifiers();

        this.carrier = EMPTY;

        var cache = this.getLookupCache();

        if (cache != null) cache.clearCache();
    }

    private static <F> void setIfPresent(MapCarrierDecodable carrier, AccessoriesPlayerOptionsHolder options, KeyedEndec<F> keyedEndec, PlayerOption<F> option) {
        if (carrier.has(keyedEndec)) {
            options.setData(option, carrier.get(keyedEndec));
        }
    }

    @Override
    public void decode(MapCarrierDecodable carrier, SerializationContext context) {
        this.loadedFromTag = true;

        this.carrier = carrier;
    }

    private record ContainersAttribute(Map<String, AccessoriesContainer> slotContainers) implements SerializationAttribute.Instance {
        public static final SerializationAttribute.WithValue<ContainersAttribute> CONTAINERS = SerializationAttribute.withValue(Accessories.translationKey("containers"));

        @Override public SerializationAttribute attribute() { return CONTAINERS; }
        @Override public Object value() { return this; }
    }

    private record InvalidStacksAttribute(List<ItemStack> invalidStacks) implements SerializationAttribute.Instance {
        public static final SerializationAttribute.WithValue<InvalidStacksAttribute> INVALID_STACKS = SerializationAttribute.withValue(Accessories.translationKey("invalidStacks"));

        @Override public SerializationAttribute attribute() { return INVALID_STACKS; }
        @Override public Object value() { return this; }
    }

    private record EntityAttribute(LivingEntity livingEntity) implements SerializationAttribute.Instance{
        public static final SerializationAttribute.WithValue<EntityAttribute> ENTITY = SerializationAttribute.withValue("entity");

        @Override public SerializationAttribute attribute() { return ENTITY; }
        @Override public Object value() { return this;}
    }

    private static class OwnerAccessibleReentrantLock extends ReentrantLock {
        @Override
        @Nullable
        public Thread getOwner() {
            return super.getOwner();
        }

        public boolean isLockedNotByOwner(Thread thread) {
            if (!isLocked()) return false;

            var owner = getOwner();

            if (owner == null) return false;

            return owner != thread;
        }

        public boolean isLockedByOwner(Thread thread) {
            if (!isLocked()) return false;

            var owner = getOwner();

            if (owner == null) return false;

            return owner == thread;
        }
    }
}