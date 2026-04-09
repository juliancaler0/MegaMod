package com.ultra.megamod.feature.backpacks;

public enum BackpackVariant {
    STANDARD("Standard", BackpackTier.LEATHER),
    NETHERITE("Netherite", BackpackTier.NETHERITE),
    DIAMOND("Diamond", BackpackTier.DIAMOND),
    GOLD("Gold", BackpackTier.GOLD),
    IRON("Iron", BackpackTier.IRON),
    EMERALD("Emerald", BackpackTier.IRON),
    LAPIS("Lapis", BackpackTier.IRON),
    REDSTONE("Redstone", BackpackTier.IRON),
    COAL("Coal", BackpackTier.LEATHER),
    QUARTZ("Quartz", BackpackTier.IRON),
    BOOKSHELF("Bookshelf", BackpackTier.IRON),
    SANDSTONE("Sandstone", BackpackTier.LEATHER),
    SNOW("Snow", BackpackTier.LEATHER),
    SPONGE("Sponge", BackpackTier.IRON),
    CAKE("Cake", BackpackTier.LEATHER),
    CACTUS("Cactus", BackpackTier.LEATHER),
    HAY("Hay", BackpackTier.LEATHER),
    MELON("Melon", BackpackTier.LEATHER),
    PUMPKIN("Pumpkin", BackpackTier.LEATHER),
    CREEPER("Creeper", BackpackTier.GOLD),
    DRAGON("Dragon", BackpackTier.NETHERITE),
    ENDERMAN("Enderman", BackpackTier.DIAMOND),
    BLAZE("Blaze", BackpackTier.GOLD),
    GHAST("Ghast", BackpackTier.GOLD),
    MAGMA_CUBE("Magma Cube", BackpackTier.GOLD),
    SKELETON("Skeleton", BackpackTier.IRON),
    SPIDER("Spider", BackpackTier.IRON),
    WITHER("Wither", BackpackTier.NETHERITE),
    WARDEN("Warden", BackpackTier.NETHERITE),
    BAT("Bat", BackpackTier.LEATHER),
    BEE("Bee", BackpackTier.LEATHER),
    WOLF("Wolf", BackpackTier.IRON),
    FOX("Fox", BackpackTier.LEATHER),
    OCELOT("Ocelot", BackpackTier.LEATHER),
    HORSE("Horse", BackpackTier.IRON),
    COW("Cow", BackpackTier.LEATHER),
    PIG("Pig", BackpackTier.LEATHER),
    SHEEP("Sheep", BackpackTier.LEATHER),
    CHICKEN("Chicken", BackpackTier.LEATHER),
    SQUID("Squid", BackpackTier.LEATHER),
    VILLAGER("Villager", BackpackTier.IRON),
    IRON_GOLEM("Iron Golem", BackpackTier.DIAMOND);

    private final String displayName;
    private final BackpackTier defaultTier;

    BackpackVariant(String displayName, BackpackTier defaultTier) {
        this.displayName = displayName;
        this.defaultTier = defaultTier;
    }

    public String getDisplayName() { return displayName; }
    public BackpackTier getDefaultTier() { return defaultTier; }

    public String getRegistryName() {
        return name().toLowerCase() + "_backpack";
    }
}
