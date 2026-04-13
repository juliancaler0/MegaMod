package com.ultra.megamod.lib.etf.features.texture_handlers;

import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Determines which of the supported texture directory conventions a given identifier lives in:
 * <ul>
 *   <li>{@link #VANILLA} — {@code textures/entity/}</li>
 *   <li>{@link #OPTIFINE} — {@code optifine/random/entity/}</li>
 *   <li>{@link #OLD_OPTIFINE} — {@code optifine/mob/}</li>
 *   <li>{@link #ETF} — {@code etf/random/entity/}</li>
 * </ul>
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public enum ETFDirectory {
    DOES_NOT_EXIST(null),
    ETF(new String[]{"textures", "etf/random"}),
    OLD_OPTIFINE(new String[]{"textures/entity", "optifine/mob"}),
    OPTIFINE(new String[]{"textures", "optifine/random"}),
    VANILLA(null);

    private final String[] replaceStrings;

    ETFDirectory(String[] replaceStrings) {
        this.replaceStrings = replaceStrings;
    }


    public static HashMap<@NotNull Identifier, @NotNull ETFDirectory> getCache() {
        return ETFManager.getInstance().ETF_DIRECTORY_CACHE;
    }

    @Nullable
    public static Identifier getDirectoryVersionOf(@Nullable Identifier vanillaIdentifier) {
        if (vanillaIdentifier == null) return null;
        ETFDirectory directory = getDirectoryOf(vanillaIdentifier);
        return switch (directory) {
            case DOES_NOT_EXIST -> null;
            case VANILLA -> vanillaIdentifier;
            default -> getIdentifierAsDirectory(vanillaIdentifier, directory);
        };
    }

    @NotNull
    public static ETFDirectory getDirectoryOf(@NotNull Identifier vanillaIdentifier) {
        HashMap<@NotNull Identifier, ETFDirectory> cache = getCache();
        var value = cache.get(vanillaIdentifier);
        if (value == null) {
            value = findDirectoryOf(vanillaIdentifier);
            cache.put(vanillaIdentifier, value);
        }
        return value;
    }

    @NotNull
    private static ETFDirectory findDirectoryOf(Identifier vanillaIdentifier) {
        String path = vanillaIdentifier.getPath();
        ResourceManager resources = Minecraft.getInstance().getResourceManager();

        if (path.contains("etf/random/entity") && resources.getResource(vanillaIdentifier).isPresent()) {
            return ETF;
        } else if (path.contains("optifine/random/entity") && resources.getResource(vanillaIdentifier).isPresent()) {
            return OPTIFINE;
        } else if (path.contains("optifine/mob") && resources.getResource(vanillaIdentifier).isPresent()) {
            return OLD_OPTIFINE;
        }

        ObjectArrayList<ETFDirectory> foundDirectories = new ObjectArrayList<>();

        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, VANILLA)).isPresent())
            foundDirectories.add(VANILLA);
        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, OLD_OPTIFINE)).isPresent())
            foundDirectories.add(OLD_OPTIFINE);
        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, OPTIFINE)).isPresent())
            foundDirectories.add(OPTIFINE);
        if (resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, ETF)).isPresent())
            foundDirectories.add(ETF);

        if (foundDirectories.isEmpty()) {
            return DOES_NOT_EXIST;
        } else if (foundDirectories.size() == 1) {
            return foundDirectories.get(0);
        } else {
            HashMap<String, ETFDirectory> resourcePackNames = new HashMap<>();

            for (ETFDirectory directory : foundDirectories) {
                resources.getResource(getIdentifierAsDirectory(vanillaIdentifier, directory))
                        .ifPresent(value -> resourcePackNames.put(value.sourcePackId(), directory));
            }

            String returnedPack = ETFUtils2.returnNameOfHighestPackFromTheseMultiple(resourcePackNames.keySet().toArray(new String[0]));
            return returnedPack != null ? resourcePackNames.get(returnedPack) : VANILLA;
        }
    }

    @NotNull
    public static Identifier getIdentifierAsDirectory(Identifier identifier, ETFDirectory directory) {
        if (directory.doesReplace()) {
            return ETFUtils2.res(identifier.getNamespace(), identifier.getPath().replace(directory.replaceStrings[0], directory.replaceStrings[1]));
        } else {
            return identifier;
        }
    }

    public boolean doesReplace() {
        return this.replaceStrings != null;
    }
}
