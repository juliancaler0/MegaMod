package com.ultra.megamod.lib.spellengine.api.item.set;


import net.minecraft.world.entity.player.Player;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;

import java.util.List;

public class EquipmentSetFeature {
    public static void init() {
        // Equipment set registry syncing is handled via NeoForge data pack registration
        SpellContainerSource.addSource(new SpellContainerSource.Entry("equipment_set", new SpellContainerSource.Source() {
            @Override
            public List<SpellContainerSource.SourcedContainer> getSpellContainers(Player player, String name) {
                return List.of();
            }
        }, null)); // Dirty checker relying on equipment changes
    }
}
