package com.ultra.megamod.lib.accessories.data;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.api.client.AccessoriesRendererRegistry;
import com.ultra.megamod.lib.accessories.data.api.EndecDataLoader;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Map;

public class RendererBindingLoader extends EndecDataLoader<Map<Item, Identifier>> {

    public static final RendererBindingLoader LOADER = new RendererBindingLoader();

    private RendererBindingLoader() {
        super(Accessories.of("rendering_binding"), "accessories/render/binding", Endec.map(
                item -> BuiltInRegistries.ITEM.getKey(item).toString(),
                itemId -> BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId)),
                MinecraftEndecs.IDENTIFIER), PackType.CLIENT_RESOURCES);
    }

    @Override
    protected void apply(Map<Identifier, Map<Item, Identifier>> rawData, ResourceManager resourceManager, ProfilerFiller profiler) {
        rawData.forEach((location, data) -> AccessoriesRendererRegistry.setDataLoadedItemToRenderer(data));
    }
}
