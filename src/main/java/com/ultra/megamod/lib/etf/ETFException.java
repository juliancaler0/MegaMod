package com.ultra.megamod.lib.etf;

import net.minecraft.world.level.block.entity.BlockEntity;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;

public class ETFException extends RuntimeException {
    public ETFException(String message) {
        super(amendMessage(message));
    }

    private static String amendMessage(String message) {
        var entity = ETFRenderContext.getCurrentEntityState();
        return message + """
                
                ----------------------
                ETF context:
                 - Entity = %s
                 - EMF installed = %s
                ----------------------
                """.formatted(entity == null ? "null" : entity.isBlockEntity() ? ((BlockEntity) entity.entity()).getType() : entity.entityType(),
                true);  // EMF bundled with MegaMod
    }
}
