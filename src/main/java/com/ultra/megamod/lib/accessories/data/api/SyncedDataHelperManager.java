package com.ultra.megamod.lib.accessories.data.api;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import com.ultra.megamod.lib.accessories.owo.network.ClientAccess;
import com.ultra.megamod.lib.accessories.owo.network.OwoNetChannel;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SyncedDataHelperManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<Identifier, SyncedDataHelper<?>> ALL_SYNCED_LOADERS = new LinkedHashMap<>();

    public static void registerLoader(SyncedDataHelper<?> loader) {
        if (ALL_SYNCED_LOADERS.containsKey(loader.getId())) {
            throw new IllegalStateException("An already existing SyncedDataLoader has been registered! [Id: " + loader.getId() + "]");
        }

        ALL_SYNCED_LOADERS.put(loader.getId(), loader);
    }

    @Nullable
    public static SyncedDataHelper<?> getLoader(Identifier id) {
        return ALL_SYNCED_LOADERS.get(id);
    }

    @ApiStatus.Internal
    public static void init(OwoNetChannel channel, Consumer<Consumer<Player>> hookRegistration) {
        channel.registerClientboundDeferred(SyncAllLoaderDataPacket.class, SyncAllLoaderDataPacket.ENDEC);

        hookRegistration.accept(player -> {
            var endecDataLoaders = ALL_SYNCED_LOADERS.values()
                    .stream()
                    .collect(Collectors.toList());

            Set<Identifier> resolvedIds = new HashSet<>();

            for (SyncedDataHelper<?> dataLoader : endecDataLoaders) {
                resolvedIds.add(dataLoader.getId());
            }

            List<SyncedDataHelper<?>> dataLoaders = new ArrayList<>();

            int lastSize = -1;

            while (dataLoaders.size() != lastSize) {
                lastSize = dataLoaders.size();

                var it = endecDataLoaders.iterator();

                while (it.hasNext()) {
                    SyncedDataHelper<?> dataLoader = it.next();

                    if (resolvedIds.containsAll(dataLoader.getDependencyIds())) {
                        resolvedIds.add(dataLoader.getId());
                        dataLoaders.add(dataLoader);
                        it.remove();
                    }
                }
            }

            // Pre-validate each packet by dry-encoding it via the dispatched codec.
            // Netty's async encoder throws on NPE (null String in data), which
            // doesn't reach a try/catch on .send(). Pre-encoding here is synchronous
            // so broken packets can be filtered out BEFORE the Netty pipeline runs.
            var packets = new java.util.ArrayList<SyncLoaderDataPacket>();
            for (SyncedDataHelper<?> dataLoader : dataLoaders) {
                var id = dataLoader.getId();
                var data = dataLoader.getServerData();
                var packet = new SyncLoaderDataPacket(id, data);
                try {
                    // Dry-encode via the dispatched codec; any NPE/Throwable surfaces here
                    SyncLoaderDataPacket.ENDEC.codec()
                            .encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, packet)
                            .getOrThrow();
                    packets.add(packet);
                } catch (Throwable t) {
                    LOGGER.error("SyncedDataHelperManager: skipping loader {} — encoding failed (likely null field in its data)", id, t);
                }
            }

            if (packets.isEmpty()) return;
            try {
                channel.serverHandle(player).send(new SyncAllLoaderDataPacket(packets));
            } catch (Throwable t) {
                LOGGER.error("SyncedDataHelperManager: send failed for {}", player.getName().getString(), t);
            }
        });
    }

    @ApiStatus.Internal
    public static void initClient(OwoNetChannel channel) {
        channel.registerClientbound(SyncAllLoaderDataPacket.class, SyncAllLoaderDataPacket.ENDEC, SyncAllLoaderDataPacket::handle);
    }

    @ApiStatus.Internal
    private record SyncAllLoaderDataPacket(List<SyncLoaderDataPacket> packets) {
        private static final StructEndec<SyncAllLoaderDataPacket> ENDEC = StructEndecBuilder.of(
                SyncLoaderDataPacket.ENDEC.listOf().fieldOf("packets", SyncAllLoaderDataPacket::packets),
                SyncAllLoaderDataPacket::new
        );

        private static void handle(SyncAllLoaderDataPacket packet, ClientAccess access) {
            for (var innerPacket : packet.packets()) {
                SyncLoaderDataPacket.handle(innerPacket, access);
            }
        }
    }

    @ApiStatus.Internal
    private static final class SyncLoaderDataPacket {
        private static final Map<Identifier, StructEndec<SyncLoaderDataPacket>> CACHED_ENDECS = new HashMap<>();

        private static final StructEndec<SyncLoaderDataPacket> ENDEC = Endec.dispatched(
                id -> {
                    var loader = getLoader(id);

                    if (loader == null) {
                        throw new IllegalStateException("Unable to get following Data Loader to handle the given sync packet: " + id);
                    }

                    return CACHED_ENDECS.computeIfAbsent(id, identifier -> {
                        return StructEndecBuilder.of(
                                MinecraftEndecs.IDENTIFIER.fieldOf("id", SyncLoaderDataPacket::id),
                                ((Endec<Object>) loader.syncDataEndec()).fieldOf("data", SyncLoaderDataPacket::data),
                                SyncLoaderDataPacket::new
                        );
                    });
                },
                SyncLoaderDataPacket::id,
                MinecraftEndecs.IDENTIFIER);

        private final Identifier id;
        private final Object data;

        private SyncLoaderDataPacket(Identifier id, Object data) {
            this.id = id;
            this.data = data;
        }

        private static void handle(SyncLoaderDataPacket packet, ClientAccess access) {
            var loader = getLoader(packet.id());

            var exception = loader.onReceivedDataUnsafe(packet.data());

            if (exception != null) {
                LOGGER.error("An error has occured when attempting to send sync data to the given SyncedDataLoader: {}", packet.id(), exception);

                throw new RuntimeException(exception);
            }
        }

        public Identifier id() {
            return id;
        }

        public Object data() {
            return data;
        }
    }
}
