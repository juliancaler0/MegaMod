package com.ultra.megamod.lib.etf;

import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.property_reading.PropertiesRandomProvider;
import com.ultra.megamod.lib.etf.features.property_reading.TrueRandomProvider;
import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperties;
import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.state.HoldsETFRenderState;
import com.ultra.megamod.lib.etf.utils.ETFEntity;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * ETF's external API surface — Phase A slice.
 * <p>
 * Only the parser / selector portion is ported here: {@link ETFVariantSuffixProvider} and
 * the custom-property registration hook used by downstream mods (EMF, ESF). Texture /
 * emissive / render helpers land in Phase B+ when the matching machinery is in place.
 */
public final class ETFApi {

    /** API version — bumped on upstream release; retained for compatibility. */
    public static final int ETFApiVersion = 11;

    /** Sentinel UUID that tells ETF "do not look up variants for this entity". */
    public static final UUID ETF_GENERIC_UUID = UUID.nameUUIDFromBytes("GENERIC".getBytes());

    /**
     * Spawner-mini-entity marker encoded in the least-significant bits of the entity UUID.
     * Read by several predicates ({@code SpawnerProperty}, {@code BlocksProperty}).
     */
    public static final long ETF_SPAWNER_MARKER = (12345L << 32) + 12345L;

    private ETFApi() {}

    /**
     * Returns the UUID ETF uses to identify a {@link BlockEntity} — built from the
     * block state's hashcode (most-significant bits) + the block position's packed long
     * (least-significant bits). Ported 1:1 from upstream.
     */
    public static UUID getUUIDForBlockEntity(BlockEntity blockEntity) {
        final var blockEntityState = blockEntity.getBlockState();
        long most = blockEntityState == null ? Long.MAX_VALUE : blockEntityState.hashCode();
        final var pos = blockEntity.getBlockPos();
        long least = pos == null ? 0 : pos.asLong();
        return new UUID(most, least);
    }

    /**
     * Translation key form of a {@link BlockEntityType}, mirroring upstream's helper.
     */
    public static String getBlockEntityTypeToTranslationKey(BlockEntityType<?> type) {
        var id = BlockEntityType.getKey(type);
        if (id == null) return null;
        return "block." + id.getNamespace() + '.' + id.getPath();
    }

    /**
     * Build a {@link ETFVariantSuffixProvider} from a properties file + its vanilla texture. Returns
     * null when neither an OptiFine nor a true-random provider can be created.
     */
    @Nullable
    public static ETFVariantSuffixProvider getVariantSupplierOrNull(Identifier propertiesFileIdentifier, Identifier vanillaIdentifier, String... suffixKeys) {
        return ETFVariantSuffixProvider.getVariantProviderOrNull(propertiesFileIdentifier, vanillaIdentifier, suffixKeys);
    }

    /**
     * Register new {@link RandomProperty} objects to be included in OptiFine property-file testing.
     */
    public static void registerCustomRandomPropertyFactory(String yourModId, RandomProperties.RandomPropertyFactory... factories) {
        if (factories != null && factories.length != 0) {
            RandomProperties.register(factories);
            ETFUtils2.logMessage(factories.length + " new ETF Random Properties registered by " + yourModId);
        }
    }

    /**
     * Variant-suffix supplier for an entity + texture pair. Typically wraps either a
     * properties-driven provider ({@link PropertiesRandomProvider}) or a "number the files"
     * true-random provider ({@link TrueRandomProvider}).
     */
    public interface ETFVariantSuffixProvider {

        @Nullable
        static ETFVariantSuffixProvider getVariantProviderOrNull(Identifier propertiesFileIdentifier, Identifier vanillaIdentifier, String... suffixKeyName) {
            PropertiesRandomProvider optifine = PropertiesRandomProvider.of(propertiesFileIdentifier, vanillaIdentifier, suffixKeyName);
            TrueRandomProvider random = ETFRenderContext.isRandomLimitedToProperties() ? null : TrueRandomProvider.of(vanillaIdentifier);

            // fallback for vanilla textures that include a state-suffix like _tame / _angry
            if (optifine == null
                    && vanillaIdentifier.getPath().endsWith(".png")
                    && "minecraft".equals(vanillaIdentifier.getNamespace())
                    && vanillaIdentifier.getPath().contains("_")) {
                String vanId = vanillaIdentifier.getPath().replaceAll("_(tame|angry|nectar|shooting|cold)", "");
                optifine = PropertiesRandomProvider.of(
                        ETFUtils2.res(vanId.replace(".png", ".properties")),
                        ETFUtils2.res(vanId),
                        suffixKeyName);
            }

            if (random == null && optifine == null) {
                return null;
            } else if (optifine == null) {
                return random;
            } else if (random == null) {
                return optifine;
            } else {
                return optifine.isHigherPackThan(random.getPackName()) ? optifine : random;
            }
        }

        boolean entityCanUpdate(UUID uuid);

        IntOpenHashSet getAllSuffixes();

        int size();

        default int getSuffixForEntity(Entity entityToBeTested) {
            return getSuffixForETFEntity(ETFEntityRenderState.forEntity((ETFEntity) entityToBeTested));
        }

        default int getSuffixForEntityState(EntityRenderState entityToBeTested) {
            return getSuffixForETFEntity(((HoldsETFRenderState) entityToBeTested).etf$getState());
        }

        default int getSuffixForBlockEntity(BlockEntity entityToBeTested) {
            return getSuffixForETFEntity(ETFEntityRenderState.forEntity((ETFEntity) entityToBeTested));
        }

        int getSuffixForETFEntity(ETFEntityRenderState entityToBeTested);

        @Deprecated(forRemoval = true)
        default void setRandomSupplier(EntityRandomSeedFunction entityRandomSeedFunction) {}

        @Deprecated(forRemoval = true)
        interface EntityRandomSeedFunction {
            int toInt(ETFEntityRenderState entity);
        }
    }
}
