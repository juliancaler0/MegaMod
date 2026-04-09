package com.ultra.megamod.feature.citizen.request;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.citizen.request.resolver.CraftingResolver;
import com.ultra.megamod.feature.citizen.request.resolver.DeliverymanResolver;
import com.ultra.megamod.feature.citizen.request.resolver.PlayerResolver;
import com.ultra.megamod.feature.citizen.request.resolver.WarehouseResolver;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Central manager for the colony request/delivery system.
 * Maintains all active requests and registered resolvers, handles the lifecycle
 * of requests from creation through resolution, and persists state to disk.
 * <p>
 * Singleton per server instance, accessed via {@link #get(ServerLevel)}.
 */
public class RequestManager {

    private static volatile RequestManager INSTANCE;
    private static final String FILE_NAME = "megamod_requests.dat";

    /** Stale request timeout: 5 minutes (6000 ticks). */
    private static final long STALE_TIMEOUT_TICKS = 6000L;
    /** Retry interval for failed requests: 30 seconds (600 ticks). */
    private static final long RETRY_INTERVAL_TICKS = 600L;

    private boolean dirty = false;

    /** All active requests indexed by token UUID. */
    private final Map<UUID, IRequest> activeRequests = new LinkedHashMap<>();
    /** All registered resolvers indexed by their token UUID. */
    private final Map<UUID, IRequestResolver> resolvers = new LinkedHashMap<>();
    /** Requests that failed and are awaiting retry, with the tick they failed at. */
    private final Map<UUID, Long> retryQueue = new HashMap<>();

    private long lastTickCount = 0;

    // ==================== Singleton ====================

    /**
     * Gets or creates the singleton RequestManager for this server.
     *
     * @param level the server overworld level
     * @return the request manager instance
     */
    public static RequestManager get(ServerLevel level) {
        RequestManager inst = INSTANCE;
        if (inst == null) {
            synchronized (RequestManager.class) {
                inst = INSTANCE;
                if (inst == null) {
                    inst = new RequestManager();
                    inst.registerDefaultResolvers();
                    inst.loadFromDisk(level);
                    INSTANCE = inst;
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Resets the singleton. Called on server stop.
     */
    public static void reset() {
        INSTANCE = null;
    }

    // ==================== Resolver Registration ====================

    /**
     * Registers the default set of resolvers.
     */
    private void registerDefaultResolvers() {
        registerResolver(new WarehouseResolver());
        registerResolver(new CraftingResolver());
        registerResolver(new DeliverymanResolver());
        registerResolver(new PlayerResolver());
    }

    /**
     * Registers a request resolver.
     *
     * @param resolver the resolver to register
     */
    public void registerResolver(IRequestResolver resolver) {
        resolvers.put(resolver.getResolverId().getId(), resolver);
    }

    /**
     * Unregisters a resolver by its token.
     *
     * @param resolverId the resolver token
     */
    public void unregisterResolver(IToken resolverId) {
        resolvers.remove(resolverId.getId());
    }

    // ==================== Request Lifecycle ====================

    /**
     * Creates a new request and attempts to resolve it immediately.
     *
     * @param requester   the entity creating the request
     * @param requestable what is being requested
     * @return the token for the newly created request
     */
    public IToken createRequest(IRequester requester, IRequestable requestable) {
        StandardRequest request = new StandardRequest(requester, requestable);
        request.setCreatedTick(lastTickCount);
        activeRequests.put(request.getToken().getId(), request);
        markDirty();

        MegaMod.LOGGER.debug("Created request {} from '{}': {}",
            request.getToken().getId(), requester.getRequesterName(), requestable.getDescription());

        // Try to resolve immediately
        assignRequest(request.getToken());
        return request.getToken();
    }

    /**
     * Finds the best resolver for a request and assigns it.
     *
     * @param token the request token
     */
    public void assignRequest(IToken token) {
        IRequest request = activeRequests.get(token.getId());
        if (request == null) return;
        if (request.getState() != RequestState.CREATED && request.getState() != RequestState.FAILED) return;

        // Sort resolvers by priority (lowest first = highest priority)
        List<IRequestResolver> sorted = resolvers.values().stream()
            .sorted(Comparator.comparingInt(IRequestResolver::getPriority))
            .collect(Collectors.toList());

        for (IRequestResolver resolver : sorted) {
            if (resolver.canResolve(request)) {
                resolver.resolve(request);
                markDirty();
                MegaMod.LOGGER.debug("Request {} assigned to resolver {} (priority {})",
                    token.getId(), resolver.getResolverId().getId(), resolver.getPriority());
                return;
            }
        }

        MegaMod.LOGGER.debug("No resolver found for request {}", token.getId());
    }

    /**
     * Marks a request as completed with the delivered item.
     *
     * @param token    the request token
     * @param delivery the delivered item stack
     */
    public void completeRequest(IToken token, ItemStack delivery) {
        IRequest request = activeRequests.get(token.getId());
        if (request == null) return;

        request.setState(RequestState.COMPLETED);
        request.setDelivery(delivery);
        retryQueue.remove(token.getId());
        markDirty();

        MegaMod.LOGGER.debug("Request {} completed with {}", token.getId(),
            delivery != null ? delivery.getHoverName().getString() : "null");
    }

    /**
     * Cancels a request.
     *
     * @param token the request token
     */
    public void cancelRequest(IToken token) {
        IRequest request = activeRequests.get(token.getId());
        if (request == null) return;

        request.setState(RequestState.CANCELLED);
        retryQueue.remove(token.getId());
        markDirty();

        MegaMod.LOGGER.debug("Request {} cancelled", token.getId());
    }

    /**
     * Marks a request as failed. It will be retried on the next cycle.
     *
     * @param token the request token
     */
    public void failRequest(IToken token) {
        IRequest request = activeRequests.get(token.getId());
        if (request == null) return;

        request.setState(RequestState.FAILED);
        request.setResolver(null);
        retryQueue.put(token.getId(), lastTickCount);
        markDirty();

        MegaMod.LOGGER.debug("Request {} failed, queued for retry", token.getId());
    }

    // ==================== Queries ====================

    /**
     * Gets all requests created by a specific requester.
     *
     * @param requesterId the requester token
     * @return list of matching requests
     */
    public List<IRequest> getRequestsForRequester(IToken requesterId) {
        return activeRequests.values().stream()
            .filter(r -> r.getRequester().getRequesterId().getId().equals(requesterId.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Gets all requests assigned to a specific resolver.
     *
     * @param resolverId the resolver token
     * @return list of matching requests
     */
    public List<IRequest> getRequestsForResolver(IToken resolverId) {
        return activeRequests.values().stream()
            .filter(r -> r.getResolverId() != null && r.getResolverId().getId().equals(resolverId.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Gets a request by its token.
     *
     * @param token the request token
     * @return the request, or null if not found
     */
    @Nullable
    public IRequest getRequest(IToken token) {
        return activeRequests.get(token.getId());
    }

    /**
     * Gets a request by its UUID.
     *
     * @param id the request UUID
     * @return the request, or null if not found
     */
    @Nullable
    public IRequest getRequest(UUID id) {
        return activeRequests.get(id);
    }

    /**
     * Returns all active (non-completed, non-cancelled) requests.
     *
     * @return unmodifiable list of active requests
     */
    public List<IRequest> getAllActiveRequests() {
        return activeRequests.values().stream()
            .filter(r -> r.getState() != RequestState.COMPLETED && r.getState() != RequestState.CANCELLED)
            .collect(Collectors.toList());
    }

    /**
     * Returns the total number of tracked requests (including completed/cancelled).
     *
     * @return total request count
     */
    public int getTotalRequestCount() {
        return activeRequests.size();
    }

    /**
     * Returns a human-readable name for the resolver with the given token.
     * Derives the name from the resolver's class name (e.g., "WarehouseResolver" -> "Warehouse").
     *
     * @param resolverId the resolver token UUID
     * @return the resolver display name, or "Unknown" if not found
     */
    public String getResolverName(@Nullable UUID resolverId) {
        if (resolverId == null) return "Unassigned";
        IRequestResolver resolver = resolvers.get(resolverId);
        if (resolver == null) return "Unknown";
        String className = resolver.getClass().getSimpleName();
        return className.replace("Resolver", "");
    }

    // ==================== Tick ====================

    /**
     * Called every server tick to process retries and clean up stale requests.
     *
     * @param level the server level
     */
    public void tick(ServerLevel level) {
        lastTickCount = level.getServer().getTickCount();

        // Process retry queue
        List<UUID> toRetry = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : retryQueue.entrySet()) {
            if (lastTickCount - entry.getValue() >= RETRY_INTERVAL_TICKS) {
                toRetry.add(entry.getKey());
            }
        }
        for (UUID id : toRetry) {
            retryQueue.remove(id);
            IRequest request = activeRequests.get(id);
            if (request != null && request.getState() == RequestState.FAILED) {
                request.setState(RequestState.CREATED);
                assignRequest(request.getToken());
            }
        }

        // Clean up completed/cancelled requests older than timeout
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, IRequest> entry : activeRequests.entrySet()) {
            IRequest request = entry.getValue();
            if (request.getState() == RequestState.COMPLETED || request.getState() == RequestState.CANCELLED) {
                if (request instanceof StandardRequest sr && lastTickCount - sr.getCreatedTick() > STALE_TIMEOUT_TICKS) {
                    toRemove.add(entry.getKey());
                }
            }
            // Timeout stale ASSIGNED/IN_PROGRESS requests that have been sitting too long
            if (request.getState() == RequestState.ASSIGNED || request.getState() == RequestState.IN_PROGRESS) {
                if (request instanceof StandardRequest sr && lastTickCount - sr.getCreatedTick() > STALE_TIMEOUT_TICKS * 3) {
                    MegaMod.LOGGER.debug("Request {} timed out after {} ticks", entry.getKey(),
                        lastTickCount - sr.getCreatedTick());
                    failRequest(request.getToken());
                }
            }
        }
        for (UUID id : toRemove) {
            activeRequests.remove(id);
            markDirty();
        }
    }

    // ==================== Persistence ====================

    private void markDirty() {
        this.dirty = true;
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataFile = new File(saveDir, "data" + File.separator + FILE_NAME);
            if (dataFile.exists()) {
                CompoundTag root = NbtIo.readCompressed((Path) dataFile.toPath(), (NbtAccounter) NbtAccounter.unlimitedHeap());
                ListTag requestList = root.getListOrEmpty("requests");
                for (int i = 0; i < requestList.size(); i++) {
                    CompoundTag reqTag = requestList.getCompoundOrEmpty(i);
                    try {
                        StandardRequest request = StandardRequest.load(reqTag);
                        activeRequests.put(request.getToken().getId(), request);
                        if (request.getState() == RequestState.FAILED) {
                            retryQueue.put(request.getToken().getId(), 0L);
                        }
                    } catch (Exception e) {
                        MegaMod.LOGGER.warn("Failed to load request entry {}", i, e);
                    }
                }
                MegaMod.LOGGER.info("Loaded {} requests from disk", activeRequests.size());
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load request data from {}", FILE_NAME, e);
        }
    }

    /**
     * Saves all request data to disk if dirty.
     *
     * @param level the server level
     */
    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            File saveDir = level.getServer().getWorldPath(LevelResource.ROOT).toFile();
            File dataDir = new File(saveDir, "data");
            if (!dataDir.exists()) dataDir.mkdirs();
            File dataFile = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            ListTag requestList = new ListTag();

            for (IRequest request : activeRequests.values()) {
                // Only persist non-completed/cancelled requests (or very recently completed ones)
                if (request instanceof StandardRequest sr) {
                    requestList.add(sr.save());
                }
            }
            root.put("requests", (Tag) requestList);

            NbtIo.writeCompressed((CompoundTag) root, (Path) dataFile.toPath());
            dirty = false;
            MegaMod.LOGGER.debug("Saved {} requests to disk", requestList.size());
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save request data to {}", FILE_NAME, e);
        }
    }
}
