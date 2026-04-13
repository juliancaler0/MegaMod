package com.ultra.megamod.lib.spellengine.api.datagen;


import net.minecraft.data.PackOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.item.set.EquipmentSet;
import com.ultra.megamod.lib.spellengine.api.item.set.EquipmentSetRegistry;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class EquipmentSetGenerator implements DataProvider {
    private final CompletableFuture<HolderLookup.Provider> registryLookup;
    protected final PackOutput dataOutput;

    public EquipmentSetGenerator(CompletableFuture<HolderLookup.Provider> registryLookup, PackOutput dataOutput) {
        this.registryLookup = registryLookup;
        this.dataOutput = dataOutput;
    }

    public record Entry(Identifier id, EquipmentSet.Definition equipmentSet) { }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return null;
    }

    @Override
    public String getName() {
        return "Equipment Set Generator";
    }

    private Path getFilePath(Identifier id) {
        return this.dataOutput.createPathProvider(PackOutput.Target.DATA_PACK, EquipmentSetRegistry.ID.getPath()).json(id);
    }
}
