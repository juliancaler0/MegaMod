package com.ultra.megamod.lib.accessories.pond;

import com.ultra.megamod.lib.accessories.api.AccessoriesStorageLookup;
import com.ultra.megamod.lib.accessories.api.client.AccessoriesRenderStateKeys;
import com.ultra.megamod.lib.accessories.api.client.RenderStateStorage;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface AccessoriesRenderStateAPI extends RenderStateStorage {
    @Nullable
    default AccessoriesStorageLookup getStorageLookup() {
        return getRenderData(AccessoriesRenderStateKeys.STORAGE_LOOKUP);
    }

    default UUID getEntityUUIDForState() {
        return getRenderData(AccessoriesRenderStateKeys.ENTITY_UUID);
    }

    default float getEntityPartialTicksForState() {
        return getRenderData(AccessoriesRenderStateKeys.PARTIAL_TICKS);
    }

    default int getEntityIdForState() {
        return getRenderData(AccessoriesRenderStateKeys.ENTITY_ID);
    }
}
