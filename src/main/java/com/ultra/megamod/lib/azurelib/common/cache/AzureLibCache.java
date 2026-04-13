/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package com.ultra.megamod.lib.azurelib.common.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.ultra.megamod.lib.azurelib.common.animation.cache.AzBakedAnimationCache;
import com.ultra.megamod.lib.azurelib.common.model.cache.AzBakedModelCache;
import com.ultra.megamod.lib.azurelib.common.util.AzureLibException;

public final class AzureLibCache {

    private AzureLibCache() {
        throw new UnsupportedOperationException();
    }

    public static void registerReloadListener() {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null) {
            return;
        }

        if (!(mc.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
            throw new AzureLibException("AzureLib was initialized too early!");
        }

        resourceManager.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                // Loading is done in apply phase
                return null;
            }

            @Override
            protected void apply(Void nothing, ResourceManager resourceManager, ProfilerFiller profiler) {
                try {
                    AzBakedAnimationCache.getInstance().loadAnimations(Runnable::run, resourceManager).join();
                    AzBakedModelCache.getInstance().loadModels(Runnable::run, resourceManager).join();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to reload AzureLib caches", e);
                }
            }
        });
    }
}
