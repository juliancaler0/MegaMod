package com.ultra.megamod.lib.etf;

import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Thrown when a properties file fails to parse cleanly.
 * <p>
 * Ported 1:1 from upstream Entity_Texture_Features.
 */
public class ETFException extends RuntimeException {
    public ETFException(String message) {
        super(amendMessage(message));
    }

    private static String amendMessage(String message) {
        var entity = ETFRenderContext.getCurrentEntityState();
        String entityDesc;
        if (entity == null) {
            entityDesc = "null";
        } else if (entity.isBlockEntity() && entity.entity() instanceof BlockEntity be) {
            entityDesc = String.valueOf(be.getType());
        } else {
            entityDesc = String.valueOf(entity.entityType());
        }
        return message + """

                ----------------------
                ETF context:
                 - Entity = %s
                 - EMF installed = %s
                ----------------------
                """.formatted(entityDesc, ETF.isThisModLoaded("entity_model_features"));
    }
}
