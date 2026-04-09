package com.ultra.megamod.feature.citizen.visitor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Data class representing a visitor NPC at a tavern.
 * Visitors appear at taverns and can be recruited into the colony
 * by paying their recruitment cost (an item stack).
 */
public class VisitorData {

    private UUID visitorId;
    private String name;
    private String biography;
    private ItemStack recruitCost;
    private int recruitTier;
    private BlockPos sittingPos;
    private long arrivalTick;
    private boolean recruited;

    // --- Names pool ---
    private static final String[] FIRST_NAMES = {
        "Aiden", "Brin", "Calla", "Dorin", "Elara", "Finn", "Gwen", "Holt",
        "Ivy", "Jasper", "Kira", "Liam", "Mira", "Nolan", "Opal", "Pierce",
        "Quinn", "Rowan", "Sable", "Thane", "Una", "Voss", "Wren", "Xander",
        "Yara", "Zeke", "Astrid", "Bjorn", "Colette", "Dmitri", "Eira",
        "Frey", "Gareth", "Helga", "Ingrid", "Jorik", "Katla", "Leif",
        "Maren", "Niall", "Olga", "Pavel", "Ragna", "Sigrid", "Tormund",
        "Ulf", "Vara", "Willem", "Ximena", "Yves", "Zelda"
    };

    private static final String[] BIOS = {
        "A wandering traveler seeking a new home.",
        "Former blacksmith from a distant village.",
        "Escaped a pirate crew and wants a fresh start.",
        "A retired soldier looking for peaceful work.",
        "Traveled across the ocean to find new lands.",
        "Once a merchant, now seeking honest labor.",
        "Left the mines after a cave-in destroyed the old shaft.",
        "A farmer whose crops were destroyed by raiders.",
        "Seeking shelter after bandits burned the old town.",
        "A skilled woodworker with nowhere to go.",
        "Dreams of joining a thriving colony.",
        "Heard tales of your colony's prosperity.",
        "A shipwrecked sailor washed ashore nearby.",
        "Fled from a corrupt kingdom across the sea.",
        "An orphan raised on the road, seeking roots.",
        "A scholar who tired of dusty libraries.",
        "Ran away from a boring desk job at the guild.",
        "Heard the tavern serves the best stew around.",
        "Looking for honest work and a warm bed.",
        "A bard who lost their instrument and career."
    };

    // --- Tier item tables (MegaColonies style) ---
    // Stored as Item + count pairs to avoid creating ItemStacks before registries are frozen.
    private static final Object[][] TIER_TABLE = {
        // Tier 1: count 1
        { Items.DRIED_KELP, 1 }, { Items.BREAD, 1 }, { Items.PAPER, 1 },
        { Items.APPLE, 1 }, { Items.BAKED_POTATO, 1 },
        // Tier 2: count 2
        { Items.MUSHROOM_STEW, 2 }, { Items.FEATHER, 2 },
        { Items.LEATHER, 2 }, { Items.COOKED_COD, 2 },
        // Tier 3: count 3
        { Items.COOKED_CHICKEN, 3 }, { Items.COOKED_PORKCHOP, 3 },
        { Items.COOKED_BEEF, 3 }, { Items.BOOK, 3 },
        // Tier 4: count 4
        { Items.IRON_INGOT, 4 }, { Items.GOLD_INGOT, 4 },
        { Items.REDSTONE, 4 }, { Items.LAPIS_LAZULI, 4 },
        // Tier 5: count 5
        { Items.HONEYCOMB, 5 }, { Items.HONEY_BOTTLE, 5 },
        { Items.NETHER_WART, 5 },
        // Tier 6: count 6
        { Items.BLAZE_POWDER, 6 }, { Items.SPIDER_EYE, 6 },
        { Items.SLIME_BALL, 6 },
        // Tier 7: count 7
        { Items.DIAMOND, 7 }, { Items.EMERALD, 7 },
    };

    // Tier boundaries: tier N uses indices [TIER_START[N-1], TIER_START[N])
    private static final int[] TIER_START = { 0, 5, 9, 13, 17, 20, 23, 25 };

    private static ItemStack pickFromTier(Random rand, int tier) {
        int start = TIER_START[tier - 1];
        int end = TIER_START[tier];
        int idx = start + rand.nextInt(end - start);
        return new ItemStack((Item) TIER_TABLE[idx][0], (int) TIER_TABLE[idx][1]);
    }

    public VisitorData() {
        this.visitorId = UUID.randomUUID();
        this.name = "Visitor";
        this.biography = "";
        this.recruitCost = ItemStack.EMPTY;
        this.recruitTier = 1;
        this.sittingPos = BlockPos.ZERO;
        this.arrivalTick = 0;
        this.recruited = false;
    }

    public VisitorData(UUID visitorId, String name, String biography, ItemStack recruitCost,
                       int recruitTier, BlockPos sittingPos, long arrivalTick) {
        this.visitorId = visitorId;
        this.name = name;
        this.biography = biography;
        this.recruitCost = recruitCost;
        this.recruitTier = recruitTier;
        this.sittingPos = sittingPos;
        this.arrivalTick = arrivalTick;
        this.recruited = false;
    }

    // --- Factory ---

    /**
     * Creates a random visitor with tier-appropriate recruitment cost.
     * Higher tavern level increases the chance of higher-tier visitors.
     *
     * @param rand         random source
     * @param tavernLevel  the tavern building level (1-5)
     * @return a new VisitorData with randomized properties
     */
    public static VisitorData createRandom(Random rand, int tavernLevel) {
        VisitorData data = new VisitorData();
        data.visitorId = UUID.randomUUID();
        data.name = FIRST_NAMES[rand.nextInt(FIRST_NAMES.length)];
        data.biography = BIOS[rand.nextInt(BIOS.length)];

        // Determine tier based on tavern level
        // Base tier range: 1 to (tavernLevel + 2), capped at 7
        int maxTier = Math.min(tavernLevel + 2, 7);
        // Weighted roll: higher tavern levels bias toward higher tiers
        int tier = 1 + rand.nextInt(maxTier);
        // Apply bonus roll for high tavern levels (level 4-5 get a second chance)
        if (tavernLevel >= 4 && rand.nextFloat() < 0.3f) {
            tier = Math.min(tier + 1, 7);
        }
        if (tavernLevel >= 5 && rand.nextFloat() < 0.2f) {
            tier = Math.min(tier + 1, 7);
        }
        data.recruitTier = tier;

        // Pick a random item from the tier table
        data.recruitCost = pickFromTier(rand, tier);

        data.arrivalTick = 0; // Set by caller
        data.recruited = false;

        return data;
    }

    // --- NBT ---

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("visitorId", visitorId.toString());
        tag.putString("name", name);
        tag.putString("biography", biography);
        if (!recruitCost.isEmpty()) {
            tag.putString("costItemId", BuiltInRegistries.ITEM.getKey(recruitCost.getItem()).toString());
            tag.putInt("costCount", recruitCost.getCount());
        }
        tag.putInt("recruitTier", recruitTier);
        tag.putInt("sittingX", sittingPos.getX());
        tag.putInt("sittingY", sittingPos.getY());
        tag.putInt("sittingZ", sittingPos.getZ());
        tag.putLong("arrivalTick", arrivalTick);
        tag.putBoolean("recruited", recruited);
        return tag;
    }

    public static VisitorData load(CompoundTag tag) {
        VisitorData data = new VisitorData();
        data.visitorId = UUID.fromString(tag.getStringOr("visitorId", UUID.randomUUID().toString()));
        data.name = tag.getStringOr("name", "Visitor");
        data.biography = tag.getStringOr("biography", "");
        String costId = tag.getStringOr("costItemId", "");
        if (!costId.isEmpty()) {
            try {
                Identifier itemId = Identifier.parse(costId);
                Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(itemId);
                if (itemOpt.isPresent()) {
                    int count = tag.getIntOr("costCount", 1);
                    data.recruitCost = new ItemStack(itemOpt.get(), count);
                }
            } catch (Exception ignored) {}
        }
        data.recruitTier = tag.getIntOr("recruitTier", 1);
        data.sittingPos = new BlockPos(
            tag.getIntOr("sittingX", 0),
            tag.getIntOr("sittingY", 0),
            tag.getIntOr("sittingZ", 0)
        );
        data.arrivalTick = tag.getLongOr("arrivalTick", 0L);
        data.recruited = tag.getBooleanOr("recruited", false);
        return data;
    }

    // --- Getters / Setters ---

    public UUID getVisitorId() { return visitorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }
    public ItemStack getRecruitCost() { return recruitCost; }
    public void setRecruitCost(ItemStack recruitCost) { this.recruitCost = recruitCost; }
    public int getRecruitTier() { return recruitTier; }
    public void setRecruitTier(int recruitTier) { this.recruitTier = recruitTier; }
    public BlockPos getSittingPos() { return sittingPos; }
    public void setSittingPos(BlockPos sittingPos) { this.sittingPos = sittingPos; }
    public long getArrivalTick() { return arrivalTick; }
    public void setArrivalTick(long arrivalTick) { this.arrivalTick = arrivalTick; }
    public boolean isRecruited() { return recruited; }
    public void setRecruited(boolean recruited) { this.recruited = recruited; }

    /**
     * Returns a display string for the recruitment cost (e.g., "4x Iron Ingot").
     */
    public String getCostDisplayString() {
        if (recruitCost.isEmpty()) return "Free";
        return recruitCost.getCount() + "x " + recruitCost.getHoverName().getString();
    }
}
