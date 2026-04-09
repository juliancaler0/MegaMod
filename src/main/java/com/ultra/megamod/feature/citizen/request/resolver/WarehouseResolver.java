package com.ultra.megamod.feature.citizen.request.resolver;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.building.BuildingWorkHelper;
import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.colonyblocks.TileEntityRack;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.request.IRequest;
import com.ultra.megamod.feature.citizen.request.IRequestResolver;
import com.ultra.megamod.feature.citizen.request.IRequestable;
import com.ultra.megamod.feature.citizen.request.IToken;
import com.ultra.megamod.feature.citizen.request.RequestState;
import com.ultra.megamod.feature.citizen.request.StandardToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Resolves requests from warehouse stock.
 * If the colony warehouse contains the requested item, this resolver marks it
 * for pickup, which then triggers a deliveryman assignment.
 * <p>
 * Searches all TileEntityRack instances within claimed chunks near the
 * requester's position. If the total count of matching items across all racks
 * meets or exceeds the request count, the request can be resolved.
 * <p>
 * Priority: 50 (high — preferred when items are already in stock).
 */
public class WarehouseResolver implements IRequestResolver {

    private final StandardToken resolverId = new StandardToken();

    /** Search radius (in blocks) around the requester position for racks. */
    private static final int WAREHOUSE_SEARCH_RADIUS = 64;

    @Nullable
    private ServerLevel serverLevel;

    @Override
    public IToken getResolverId() {
        return resolverId;
    }

    /**
     * Sets the server level context for this resolver.
     * Must be called before canResolve/resolve if warehouse scanning is needed.
     */
    public void setServerLevel(@Nullable ServerLevel level) {
        this.serverLevel = level;
    }

    @Override
    public boolean canResolve(IRequest request) {
        // Resolve server level from requester position if not explicitly set
        ServerLevel level = this.serverLevel;
        if (level == null) {
            level = resolveServerLevel(request);
        }
        if (level == null) return false;

        IRequestable requestable = request.getRequestable();
        BlockPos requesterPos = request.getRequester().getRequesterPosition();
        int neededCount = requestable.getCount();

        // Search all TileEntityRack instances near the requester's position
        int totalFound = 0;
        List<TileEntityRack> racks = BuildingWorkHelper.findNearbyRacks(level, requesterPos);

        // Also search in a wider radius around the requester for warehouse racks
        for (int x = -WAREHOUSE_SEARCH_RADIUS; x <= WAREHOUSE_SEARCH_RADIUS; x += 4) {
            for (int z = -WAREHOUSE_SEARCH_RADIUS; z <= WAREHOUSE_SEARCH_RADIUS; z += 4) {
                for (int y = -8; y <= 8; y += 4) {
                    BlockPos scanPos = requesterPos.offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(scanPos);
                    if (be instanceof TileEntityRack rack && !racks.contains(rack)) {
                        racks.add(rack);
                    }
                }
            }
        }

        for (TileEntityRack rack : racks) {
            for (int i = 0; i < rack.getContainerSize(); i++) {
                ItemStack stack = rack.getItem(i);
                if (!stack.isEmpty() && requestable.matchesItem(stack)) {
                    totalFound += stack.getCount();
                    if (totalFound >= neededCount) {
                        // Store the source rack position so deliveryman knows where to pick up
                        if (request instanceof com.ultra.megamod.feature.citizen.request.StandardRequest sr) {
                            sr.setSourcePos(rack.getBlockPos());
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Attempts to resolve a ServerLevel from the server if not explicitly set.
     * Uses the Minecraft server instance from loaded chunks.
     */
    @Nullable
    private ServerLevel resolveServerLevel(IRequest request) {
        try {
            net.minecraft.server.MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                return server.overworld();
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public void resolve(IRequest request) {
        request.setState(RequestState.ASSIGNED);
        request.setResolver(resolverId);
        MegaMod.LOGGER.debug("WarehouseResolver resolved request {} — item found in stock, assigned for delivery", request.getToken().getId());
    }

    @Override
    public int getPriority() {
        return 50;
    }
}
