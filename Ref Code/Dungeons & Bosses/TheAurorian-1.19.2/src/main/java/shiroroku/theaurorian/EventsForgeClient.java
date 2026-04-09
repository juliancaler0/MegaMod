package shiroroku.theaurorian;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shiroroku.theaurorian.Renderers.AurorianiteShovelBlockOutline;
import shiroroku.theaurorian.Renderers.UmbraPickaxeBlockOutline;

@Mod.EventBusSubscriber(modid = TheAurorian.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventsForgeClient {

    @SubscribeEvent
    public static void onRenderOutline(RenderHighlightEvent.Block event) {
        UmbraPickaxeBlockOutline.onRenderOutline(event);
        AurorianiteShovelBlockOutline.onRenderOutline(event);
    }

}
