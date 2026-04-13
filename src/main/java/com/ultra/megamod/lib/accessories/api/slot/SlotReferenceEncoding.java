package com.ultra.megamod.lib.accessories.api.slot;

import io.netty.buffer.ByteBuf;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationAttribute;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.StructEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.format.bytebuf.ByteBufDeserializer;
import com.ultra.megamod.lib.accessories.endec.adapter.format.bytebuf.ByteBufSerializer;
import com.ultra.megamod.lib.accessories.endec.adapter.impl.StructEndecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helper class allowing for a Serialization of a given SlotReference instance across the network
 */
public class SlotReferenceEncoding {

    /**
     * Encodes the given {@link SlotReference} into the passed {@link ByteBuf} and returns such
     */
    public static ByteBuf encodeReference(ByteBuf byteBuf, SlotReference slotReference) {
        // Stub - encoding via ByteBuf not directly supported in adapter
        // In practice, this is handled via StreamCodec networking
        return byteBuf;
    }

    /**
     * Safe method of decoding {@link SlotReference} data from the given {@link ByteBuf} requiring the {@link Level} that the
     * entity is located within as a parameter. It is recommended to double-check that the given {@link SlotReference}
     * is still valid using {@link SlotReference#isValid()} as changes may have occurred that invalidates the reference.
     */
    public static SlotReference decodeReference(ByteBuf byteBuf, Level level) {
        // Stub - decoding via ByteBuf not directly supported in adapter
        // In practice, this is handled via StreamCodec networking
        throw new UnsupportedOperationException("SlotReference ByteBuf decode not yet ported");
    }

    //--

    private static final Endec<LivingEntity> LIVING_ENTITY_ENDEC = Endec.VAR_INT.xmapWithContext(
            (ctx, id) -> {
                var level = ctx.requireAttributeValue(LevelAttribute.LEVEL).level();
                var entity = level.getEntity(id);

                if(entity == null) {
                    throw new IllegalStateException("Unable to locate the given entity with the following ID with the passed level! [Id: " + id + " , Level: " + level.dimension() + " ]");
                }

                if(!(entity instanceof LivingEntity living)) {
                    throw new IllegalStateException("Given entity found within the world was not of LivingEntity! [Id: " + id + ", EntityType: " + entity.getType() + ", Level: " + level.dimension() + " ]");
                }

                return living;
            },
            (context, entity) -> entity.getId());

    /**
     * An {@link Endec} for {@link SlotReference} that requires during {@link Endec#encode} or {@link Endec#decode}  that
     * the given {@link SerializationContext} passed within the given method calls requires a
     * {@link LevelAttribute} to properly en(de)code the given reference data
     */
    @ApiStatus.Experimental
    public static final StructEndec<SlotReference> ENDEC = StructEndecBuilder.of(
            LIVING_ENTITY_ENDEC.fieldOf("entity", SlotReference::entity),
            SlotPath.ENDEC.flatFieldOf(SlotReference::slotPath),
            SlotReference::of);

    @ApiStatus.Experimental
    public record LevelAttribute(Level level) implements SerializationAttribute.Instance {
        public static final SerializationAttribute.WithValue<LevelAttribute> LEVEL = SerializationAttribute.withValue("current_minecraft_level");

        @Override public SerializationAttribute attribute() { return LEVEL; }
        @Override public Object value() { return this; }
    }
}
