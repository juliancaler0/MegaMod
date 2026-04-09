package com.ultra.megamod.feature.furniture;

import com.mojang.datafixers.util.Pair;
import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Injects quest board structures into vanilla village template pools
 * so they naturally spawn in villages during world generation.
 *
 * The quest board structure template is created programmatically on server start,
 * then injected into the house pools of all 5 village biome types.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class VillageQuestBoardInjector {

    private static final String[] VILLAGE_POOLS = {
        "minecraft:village/plains/houses",
        "minecraft:village/desert/houses",
        "minecraft:village/savanna/houses",
        "minecraft:village/snowy/houses",
        "minecraft:village/taiga/houses"
    };

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();

        // Create the quest board structure template programmatically
        createQuestBoardTemplate(server);

        // Inject it into vanilla village pools
        injectIntoVillagePools(server);
    }

    private static void createQuestBoardTemplate(MinecraftServer server) {
        try {
            var templateManager = server.getStructureManager();
            Identifier templateId = Identifier.parse("megamod:village/quest_board");

            // Check if template already loaded
            if (templateManager.get(templateId).isPresent()) return;

            // Get or create the template
            var template = templateManager.getOrCreate(templateId);

            var questBoardState = FurnitureRegistry.QUEST_BOARD.get().defaultBlockState()
                .setValue(FurnitureBlock.FACING, net.minecraft.core.Direction.SOUTH);
            var cobblestone = net.minecraft.world.level.block.Blocks.COBBLESTONE.defaultBlockState();

            // Build block info list: 3x2x3 (cobblestone base + quest board on top)
            List<net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo> blocks = new ArrayList<>();

            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    blocks.add(new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(
                        new net.minecraft.core.BlockPos(x, 0, z), cobblestone, null));
                }
            }
            blocks.add(new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(
                new net.minecraft.core.BlockPos(1, 1, 1), questBoardState, null));

            // Set size via reflection
            var sizeField = template.getClass().getDeclaredField("size");
            sizeField.setAccessible(true);
            sizeField.set(template, new net.minecraft.core.Vec3i(3, 2, 3));

            // Create Palette via reflection (constructor is package-private)
            var paletteClass = Class.forName("net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$Palette");
            Constructor<?> paletteCtor = paletteClass.getDeclaredConstructor(List.class);
            paletteCtor.setAccessible(true);
            Object palette = paletteCtor.newInstance(blocks);

            var palettesField = template.getClass().getDeclaredField("palettes");
            palettesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            var palettes = (List<Object>) palettesField.get(template);
            palettes.clear();
            palettes.add(palette);

            MegaMod.LOGGER.info("[MegaMod] Created quest board village structure template");
        } catch (Exception e) {
            MegaMod.LOGGER.warn("[MegaMod] Could not create quest board template: {}", e.getMessage());
        }
    }

    private static void injectIntoVillagePools(MinecraftServer server) {
        Registry<StructureTemplatePool> poolRegistry = server.registryAccess()
            .lookupOrThrow(Registries.TEMPLATE_POOL);
        Holder<StructureProcessorList> emptyProcessor = server.registryAccess()
            .lookupOrThrow(Registries.PROCESSOR_LIST)
            .getOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.parse("minecraft:empty")));

        int injectedCount = 0;
        for (String poolId : VILLAGE_POOLS) {
            try {
                StructureTemplatePool pool = poolRegistry.getValue(Identifier.parse(poolId));
                if (pool == null) continue;

                StructurePoolElement questBoardElement = SinglePoolElement.single(
                    "megamod:village/quest_board", emptyProcessor).apply(StructureTemplatePool.Projection.RIGID);

                // Access the raw templates list via reflection (usually ImmutableList)
                var rawTemplatesField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
                rawTemplatesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Pair<StructurePoolElement, Integer>> rawTemplates =
                    (List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool);

                List<Pair<StructurePoolElement, Integer>> mutableRaw = new ArrayList<>(rawTemplates);
                mutableRaw.add(Pair.of(questBoardElement, 2));
                rawTemplatesField.set(pool, mutableRaw);

                // Also update the flattened templates list
                var templatesField = StructureTemplatePool.class.getDeclaredField("templates");
                templatesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<StructurePoolElement> templates = (List<StructurePoolElement>) templatesField.get(pool);
                ObjectArrayList<StructurePoolElement> mutableTemplates = new ObjectArrayList<>(templates);
                mutableTemplates.add(questBoardElement);
                mutableTemplates.add(questBoardElement);
                templatesField.set(pool, mutableTemplates);

                injectedCount++;
            } catch (Exception e) {
                MegaMod.LOGGER.warn("[MegaMod] Could not inject quest board into village pool {}: {}", poolId, e.getMessage());
            }
        }
        MegaMod.LOGGER.info("[MegaMod] Quest Board injected into {} village generation pools", injectedCount);
    }
}
