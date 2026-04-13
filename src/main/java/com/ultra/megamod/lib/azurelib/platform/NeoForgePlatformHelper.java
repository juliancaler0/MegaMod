package com.ultra.megamod.lib.azurelib.platform;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.ultra.megamod.lib.azurelib.common.platform.services.IPlatformHelper;
import com.ultra.megamod.lib.azurelib.NeoForgeAzureLibMod;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        // Use system property to detect dev environment
        return System.getProperty("neoforge.development") != null;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    @Override
    public boolean isServerEnvironment() {
        // Check if client classes exist to determine if this is a server
        try {
            Class.forName("net.minecraft.client.Minecraft");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    @Override
    public boolean isEnvironmentClient() {
        return !isServerEnvironment();
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(
        String id,
        UnaryOperator<DataComponentType.Builder<T>> builder
    ) {
        return NeoForgeAzureLibMod.DATA_COMPONENTS_REGISTER.registerComponentType(id, builder);
    }
}
