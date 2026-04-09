/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.Identifier
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.dimension.DimensionType
 */
package com.ultra.megamod.feature.dimensions;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class MegaModDimensions {
    public static final ResourceKey<Level> MUSEUM = ResourceKey.create((ResourceKey)Registries.DIMENSION, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"museum"));
    public static final ResourceKey<Level> DUNGEON = ResourceKey.create((ResourceKey)Registries.DIMENSION, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"dungeon"));
    public static final ResourceKey<Level> CASINO = ResourceKey.create((ResourceKey)Registries.DIMENSION, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"casino"));
    public static final ResourceKey<DimensionType> POCKET_TYPE = ResourceKey.create((ResourceKey)Registries.DIMENSION_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"pocket"));
    public static final ResourceKey<Level> RESOURCE = ResourceKey.create((ResourceKey)Registries.DIMENSION, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"resource"));
    public static final ResourceKey<DimensionType> RESOURCE_WORLD_TYPE = ResourceKey.create((ResourceKey)Registries.DIMENSION_TYPE, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"resource_world"));
    public static final ResourceKey<Level> TRADING = ResourceKey.create((ResourceKey)Registries.DIMENSION, (Identifier)Identifier.fromNamespaceAndPath((String)"megamod", (String)"trading"));
}

