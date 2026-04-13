package com.ultra.megamod.lib.accessories.client;

import com.ultra.megamod.lib.accessories.fabric.event.Event;
import com.ultra.megamod.lib.accessories.fabric.event.EventFactory;
import net.minecraft.client.Minecraft;

//@Environment(EnvType.CLIENT)
public class ClientLifecycleEvents {

    public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = EventFactory.createArrayBacked(EndDataPackReload.class, callbacks -> (client, success) -> {
        for (EndDataPackReload callback : callbacks) {
            callback.endDataPackReload(client, success);
        }
    });

    @FunctionalInterface
    public interface EndDataPackReload {
        /**
         * Called after data packs on a Minecraft client have been reloaded.
         *
         * <p>If the reload was not successful, the old data packs will be kept.
         *
         * @param client the client
         * @param success if the reload was successful
         */
        void endDataPackReload(Minecraft client, boolean success);
    }
}
