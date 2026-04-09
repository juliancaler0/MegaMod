package shiroroku.theaurorian.Registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shiroroku.theaurorian.TheAurorian;

public class POIRegistry {

    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(ForgeRegistries.POI_TYPES, TheAurorian.MODID);

    public static final RegistryObject<PoiType> aurorian_portal = POIS.register("aurorian_portal", () -> new PoiType(ImmutableSet.copyOf(BlockRegistry.aurorian_portal.get().getStateDefinition().getPossibleStates()), 0, 1));

}
