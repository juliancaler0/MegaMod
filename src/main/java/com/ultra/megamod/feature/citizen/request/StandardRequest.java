package com.ultra.megamod.feature.citizen.request;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Default implementation of {@link IRequest}.
 * Tracks the full lifecycle of a colony item request including requester, requestable,
 * assigned resolver, delivery, and state.
 */
public class StandardRequest implements IRequest {

    private final IToken token;
    private final IRequester requester;
    private final IRequestable requestable;
    private RequestState state;
    @Nullable
    private IToken resolverId;
    @Nullable
    private ItemStack delivery;
    private long createdTick;
    /** Source position where the requested item was found (set by WarehouseResolver). */
    @Nullable
    private net.minecraft.core.BlockPos sourcePos;

    public StandardRequest(IRequester requester, IRequestable requestable) {
        this.token = new StandardToken();
        this.requester = requester;
        this.requestable = requestable;
        this.state = RequestState.CREATED;
        this.createdTick = 0;
    }

    private StandardRequest(IToken token, IRequester requester, IRequestable requestable,
                            RequestState state, @Nullable IToken resolverId,
                            @Nullable ItemStack delivery, long createdTick) {
        this.token = token;
        this.requester = requester;
        this.requestable = requestable;
        this.state = state;
        this.resolverId = resolverId;
        this.delivery = delivery;
        this.createdTick = createdTick;
    }

    @Override
    public IToken getToken() {
        return token;
    }

    @Override
    public IRequester getRequester() {
        return requester;
    }

    @Override
    public IRequestable getRequestable() {
        return requestable;
    }

    @Override
    public RequestState getState() {
        return state;
    }

    @Override
    public void setState(RequestState state) {
        this.state = state;
    }

    @Override
    @Nullable
    public IToken getResolverId() {
        return resolverId;
    }

    @Override
    public void setResolver(IToken resolverId) {
        this.resolverId = resolverId;
    }

    @Override
    @Nullable
    public ItemStack getDelivery() {
        return delivery;
    }

    @Override
    public void setDelivery(ItemStack delivery) {
        this.delivery = delivery;
    }

    public long getCreatedTick() {
        return createdTick;
    }

    public void setCreatedTick(long tick) {
        this.createdTick = tick;
    }

    /**
     * Returns the source position where the requested item was found.
     * Set by WarehouseResolver when it finds items in a rack.
     * @return the source position, or null if unknown
     */
    @Nullable
    public net.minecraft.core.BlockPos getSourcePos() {
        return sourcePos;
    }

    /**
     * Sets the source position for pickup.
     * @param pos the rack/building position where the item is stored
     */
    public void setSourcePos(@Nullable net.minecraft.core.BlockPos pos) {
        this.sourcePos = pos;
    }

    /**
     * Serializes this request to NBT.
     * Note: the requester is saved by token ID only — it must be re-resolved on load.
     *
     * @return the compound tag
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("token", (Tag) ((StandardToken) token).toNbt());
        tag.putString("requesterId", requester.getRequesterId().getId().toString());
        tag.putString("requesterName", requester.getRequesterName());
        tag.putInt("requesterPosX", requester.getRequesterPosition().getX());
        tag.putInt("requesterPosY", requester.getRequesterPosition().getY());
        tag.putInt("requesterPosZ", requester.getRequesterPosition().getZ());
        tag.put("requestable", (Tag) requestable.toNbt());
        tag.putInt("state", state.ordinal());
        if (resolverId != null) {
            tag.putString("resolverId", resolverId.getId().toString());
        }
        if (delivery != null && !delivery.isEmpty()) {
            net.minecraft.resources.Identifier deliveryId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(delivery.getItem());
            tag.putString("deliveryItemId", deliveryId.toString());
            tag.putInt("deliveryCount", delivery.getCount());
        }
        tag.putLong("createdTick", createdTick);
        if (sourcePos != null) {
            tag.putInt("sourcePosX", sourcePos.getX());
            tag.putInt("sourcePosY", sourcePos.getY());
            tag.putInt("sourcePosZ", sourcePos.getZ());
        }
        return tag;
    }

    /**
     * Deserializes a request from NBT.
     * Creates a stub requester from saved position and name data.
     *
     * @param tag the compound tag
     * @return the loaded request
     */
    public static StandardRequest load(CompoundTag tag) {
        StandardToken token = StandardToken.fromNbt(tag.getCompoundOrEmpty("token"));

        String requesterIdStr = tag.getStringOr("requesterId", "");
        String requesterName = tag.getStringOr("requesterName", "Unknown");
        int posX = tag.getIntOr("requesterPosX", 0);
        int posY = tag.getIntOr("requesterPosY", 0);
        int posZ = tag.getIntOr("requesterPosZ", 0);
        net.minecraft.core.BlockPos requesterPos = new net.minecraft.core.BlockPos(posX, posY, posZ);

        IRequester stubRequester = new StubRequester(requesterIdStr, requesterName, requesterPos);

        CompoundTag requestableTag = tag.getCompoundOrEmpty("requestable");
        IRequestable requestable = RequestableRegistry.fromNbt(requestableTag);

        RequestState state = RequestState.fromOrdinal(tag.getIntOr("state", 0));

        IToken resolverId = null;
        String resolverIdStr = tag.getStringOr("resolverId", "");
        if (!resolverIdStr.isEmpty()) {
            try {
                resolverId = new StandardToken(java.util.UUID.fromString(resolverIdStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

        ItemStack delivery = null;
        String deliveryItemIdStr = tag.getStringOr("deliveryItemId", "");
        if (!deliveryItemIdStr.isEmpty()) {
            try {
                net.minecraft.resources.Identifier deliveryId = net.minecraft.resources.Identifier.parse(deliveryItemIdStr);
                int deliveryCount = tag.getIntOr("deliveryCount", 1);
                delivery = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(deliveryId), deliveryCount);
            } catch (Exception e) {
                delivery = ItemStack.EMPTY;
            }
        }

        long createdTick = tag.getLongOr("createdTick", 0L);

        StandardRequest request = new StandardRequest(token, stubRequester, requestable, state, resolverId, delivery, createdTick);

        // Restore source position if saved
        if (tag.contains("sourcePosX")) {
            request.setSourcePos(new net.minecraft.core.BlockPos(
                    tag.getIntOr("sourcePosX", 0),
                    tag.getIntOr("sourcePosY", 0),
                    tag.getIntOr("sourcePosZ", 0)
            ));
        }

        return request;
    }

    /**
     * Simple stub requester used when loading requests from disk.
     * The actual building/citizen requester should be re-linked after world load.
     */
    private record StubRequester(String idStr, String name,
                                 net.minecraft.core.BlockPos pos) implements IRequester {
        @Override
        public IToken getRequesterId() {
            try {
                return new StandardToken(java.util.UUID.fromString(idStr));
            } catch (IllegalArgumentException e) {
                return new StandardToken();
            }
        }

        @Override
        public String getRequesterName() {
            return name;
        }

        @Override
        public net.minecraft.core.BlockPos getRequesterPosition() {
            return pos;
        }
    }
}
