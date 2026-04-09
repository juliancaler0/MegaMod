package com.ultra.megamod.feature.citizen.request;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.request.types.CraftingRequest;
import com.ultra.megamod.feature.citizen.request.types.DeliveryRequest;
import com.ultra.megamod.feature.citizen.request.types.FoodRequest;
import com.ultra.megamod.feature.citizen.request.types.PickupRequest;
import com.ultra.megamod.feature.citizen.request.types.ToolRequest;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registry for deserializing {@link IRequestable} types from NBT.
 * Each requestable type registers its type string and factory function here.
 */
public final class RequestableRegistry {

    private static final Map<String, Function<CompoundTag, IRequestable>> FACTORIES = new HashMap<>();

    static {
        FACTORIES.put("delivery", DeliveryRequest::fromNbt);
        FACTORIES.put("pickup", PickupRequest::fromNbt);
        FACTORIES.put("crafting", CraftingRequest::fromNbt);
        FACTORIES.put("food", FoodRequest::fromNbt);
        FACTORIES.put("tool", ToolRequest::fromNbt);
    }

    private RequestableRegistry() {
    }

    /**
     * Deserializes a requestable from a compound tag using the "type" field.
     *
     * @param tag the compound tag
     * @return the deserialized requestable, or a fallback delivery request on unknown type
     */
    public static IRequestable fromNbt(CompoundTag tag) {
        String type = tag.getStringOr("type", "delivery");
        Function<CompoundTag, IRequestable> factory = FACTORIES.get(type);
        if (factory != null) {
            return factory.apply(tag);
        }
        MegaMod.LOGGER.warn("Unknown requestable type '{}', falling back to delivery", type);
        return DeliveryRequest.fromNbt(tag);
    }

    /**
     * Registers a custom requestable type factory.
     *
     * @param type    the type string
     * @param factory the factory function
     */
    public static void register(String type, Function<CompoundTag, IRequestable> factory) {
        FACTORIES.put(type, factory);
    }
}
