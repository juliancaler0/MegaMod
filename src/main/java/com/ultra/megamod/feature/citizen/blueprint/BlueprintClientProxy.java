package com.ultra.megamod.feature.citizen.blueprint;

import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.citizen.blueprint.screen.BuildToolScreen;
import com.ultra.megamod.feature.citizen.blueprint.screen.ScanToolScreen;
import com.ultra.megamod.feature.citizen.blueprint.screen.ShapeToolScreen;
import com.ultra.megamod.feature.citizen.blueprint.tools.BuildToolItem;
import com.ultra.megamod.feature.citizen.blueprint.tools.ScanToolItem;
import com.ultra.megamod.feature.citizen.blueprint.tools.ShapeToolItem;
import net.minecraft.client.Minecraft;

/**
 * Client-only helper that wires the real screen-opening implementations onto
 * the {@code OPEN_*_SCREEN} Consumer hooks on the blueprint tool items. Loaded
 * from {@code MegaModClient} so this class's references to
 * {@code net.minecraft.client.Minecraft} and the tool screens are never
 * reached on the dedicated server.
 */
public final class BlueprintClientProxy {
    private BlueprintClientProxy() {}

    public static void init() {
        ScanToolItem.OPEN_SCAN_SCREEN = data -> {
            var slot = data.getCurrentSlotData();
            Minecraft.getInstance().setScreen(new ScanToolScreen(slot.getPos1(), slot.getPos2()));
        };

        BuildToolItem.OPEN_BUILD_SCREEN = placementPos -> {
            StructurePacks.discoverPacks(
                    Minecraft.getInstance().gameDirectory.toPath()
                            .resolve("blueprints").resolve("megamod"));
            Minecraft.getInstance().setScreen(new BuildToolScreen(placementPos));
        };

        ShapeToolItem.OPEN_SHAPE_SCREEN = placementPos ->
                Minecraft.getInstance().setScreen(new ShapeToolScreen(placementPos));
    }
}
