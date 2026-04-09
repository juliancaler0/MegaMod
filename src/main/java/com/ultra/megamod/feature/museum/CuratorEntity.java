/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.npc.villager.VillagerData
 *  net.minecraft.world.entity.npc.villager.VillagerProfession
 *  net.minecraft.world.entity.npc.villager.VillagerType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.storage.ValueInput
 *  net.minecraft.world.level.storage.ValueOutput
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.museum;

import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.museum.catalog.AquariumCatalog;
import com.ultra.megamod.feature.museum.catalog.ArtCatalog;
import com.ultra.megamod.feature.museum.catalog.AchievementCatalog;
import com.ultra.megamod.feature.museum.catalog.ItemCatalog;
import com.ultra.megamod.feature.museum.catalog.WildlifeCatalog;
import com.ultra.megamod.feature.museum.network.OpenMuseumPayload;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class CuratorEntity
extends PathfinderMob {
    private static final EntityDataAccessor<VillagerData> DATA_VILLAGER = SynchedEntityData.defineId(CuratorEntity.class, (EntityDataSerializer)EntityDataSerializers.VILLAGER_DATA);

    public CuratorEntity(EntityType<? extends CuratorEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setPersistenceRequired();
        this.setCustomName((Component)Component.literal((String)"Museum Curator").withStyle(ChatFormatting.GOLD));
        this.setCustomNameVisible(true);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VILLAGER, new VillagerData((Holder)BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS), (Holder)BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.LIBRARIAN), 5));
    }

    public VillagerData getVillagerData() {
        return (VillagerData)this.entityData.get(DATA_VILLAGER);
    }

    public void setVillagerData(VillagerData data) {
        this.entityData.set(DATA_VILLAGER, data);
    }

    public boolean isInvulnerable() {
        return true;
    }

    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 12.0f));
    }

    @Override
    public void travel(Vec3 movementInput) {
        // Lock in place — only rotation from LookAtPlayerGoal
        super.travel(Vec3.ZERO);
    }

    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide() && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            MuseumData data = MuseumData.get((ServerLevel)this.level());
            UUID pid = serverPlayer.getUUID();

            // Send curator dialogue before opening museum
            String dialogue = getCuratorDialogue(data, pid);
            serverPlayer.sendSystemMessage((Component)Component.literal((String)("Curator: " + dialogue)).withStyle(ChatFormatting.GOLD));

            String json = this.serializeMuseumData(data, pid);
            PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new OpenMuseumPayload(json), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    private String getCuratorDialogue(MuseumData data, UUID pid) {
        // Calculate completion percentages per wing
        Set<String> mobs = data.getDonatedMobs(pid);
        int aqTotal = AquariumCatalog.getTotalCount();
        int aqDonated = 0;
        for (AquariumCatalog.MobEntry e : AquariumCatalog.ENTRIES) {
            if (mobs.contains(e.entityId())) aqDonated++;
        }
        int wlTotal = WildlifeCatalog.getTotalCount();
        int wlDonated = 0;
        for (WildlifeCatalog.MobEntry e : WildlifeCatalog.ENTRIES) {
            if (mobs.contains(e.entityId())) wlDonated++;
        }
        int artTotal = ArtCatalog.getTotalCount();
        int artDonated = 0;
        for (ArtCatalog.ArtEntry e : ArtCatalog.ENTRIES) {
            if (data.getDonatedArt(pid).contains(e.id())) artDonated++;
        }
        Set<String> donatedItems = data.getDonatedItems(pid);
        java.util.HashSet<String> uniqueCatalogItems = new java.util.HashSet<>();
        for (java.util.List<String> catItems : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            uniqueCatalogItems.addAll(catItems);
        }
        int itemTotal = uniqueCatalogItems.size();
        int itemDonated = 0;
        for (String item : uniqueCatalogItems) {
            if (donatedItems.contains(item)) itemDonated++;
        }

        int totalDonated = aqDonated + wlDonated + artDonated + itemDonated;
        int totalCatalog = aqTotal + wlTotal + artTotal + itemTotal;
        int overallPct = totalCatalog > 0 ? totalDonated * 100 / totalCatalog : 0;

        int aqPct = aqTotal > 0 ? aqDonated * 100 / aqTotal : 0;
        int wlPct = wlTotal > 0 ? wlDonated * 100 / wlTotal : 0;
        int artPct = artTotal > 0 ? artDonated * 100 / artTotal : 0;
        int itemPct = itemTotal > 0 ? itemDonated * 100 / itemTotal : 0;

        int idx = this.tickCount;

        // Full completion
        if (overallPct >= 100) {
            String[] complete = {
                "The museum is magnificent! What a collection!",
                "You've done it! Every exhibit is filled!",
                "A true curator's masterpiece!",
                "Scholars will travel far and wide to study your collection!",
                "I have never seen a finer museum in all my years!"
            };
            return complete[Math.abs(idx) % complete.length];
        }

        // Check for wing-specific 100% completion
        if (aqPct >= 100 && Math.abs(idx) % 7 == 0) return "The aquarium is fully stocked! Magnificent!";
        if (wlPct >= 100 && Math.abs(idx) % 7 == 1) return "The wildlife wing is complete! Every creature accounted for!";
        if (artPct >= 100 && Math.abs(idx) % 7 == 2) return "The gallery is a masterwork of curation!";
        if (itemPct >= 100 && Math.abs(idx) % 7 == 3) return "The items collection is second to none!";

        // Low overall completion - welcome messages
        if (overallPct < 10) {
            String[] welcome = {
                "Welcome! Bring me items, creatures, and art to fill these halls!",
                "Every great museum starts with a single donation!",
                "I can't wait to see what you'll bring me!",
                "Sneak and right-click the Museum Block with an item to donate!",
                "Use the Mob Net on creatures, then donate the captured mob here!"
            };
            return welcome[Math.abs(idx) % welcome.length];
        }

        // Milestone encouragement
        if (overallPct >= 75) {
            String[] almost = {
                "You're so close to completing the entire collection!",
                "Just a few more pieces and this museum will be legendary!",
                "The halls are nearly full... what a sight to behold!"
            };
            if (Math.abs(idx) % 4 == 0) return almost[Math.abs(idx / 4) % almost.length];
        } else if (overallPct >= 50) {
            String[] halfway = {
                "Halfway there! The museum is really taking shape!",
                "Impressive progress! The exhibits are coming along nicely!",
                "Visitors would already be amazed by this collection!"
            };
            if (Math.abs(idx) % 5 == 0) return halfway[Math.abs(idx / 5) % halfway.length];
        } else if (overallPct >= 25) {
            String[] quarter = {
                "A solid start! Keep those donations coming!",
                "The museum is beginning to feel alive!",
                "You have a keen eye for collecting!"
            };
            if (Math.abs(idx) % 5 == 0) return quarter[Math.abs(idx / 5) % quarter.length];
        }

        // Dungeon-specific hints (show occasionally when items wing is < 50%)
        if (itemPct < 50 && Math.abs(idx) % 6 == 0) {
            String[] dungeonHints = {
                "Adventurers tell me the dungeons hold rare treasures worth collecting...",
                "Have you seen the Void Shards that dungeon delvers bring back? I'd love one!",
                "Boss trophies from the dungeons would make magnificent centerpieces!",
                "Dungeon relics and ancient artifacts belong in a museum!",
                "I hear there are unique masks deep within the dungeon chambers..."
            };
            return dungeonHints[Math.abs(idx / 6) % dungeonHints.length];
        }

        // Find the least-complete wing and give hints
        int minPct = Math.min(Math.min(aqPct, wlPct), Math.min(artPct, itemPct));

        if (minPct == aqPct) {
            String[] aqHints = {
                "The aquarium could use more exhibits! Try mob nets or fish buckets!",
                "Water creatures make wonderful exhibits!",
                "Have you explored ocean monuments? Elder Guardians are rare finds!",
                "Even humble cod and salmon deserve a place in the aquarium!",
                "Axolotls, dolphins, turtles... the ocean is full of wonders!"
            };
            return aqHints[Math.abs(idx) % aqHints.length];
        }
        if (minPct == wlPct) {
            String[] wlHints = {
                "Capture some creatures with the Mob Net for the wildlife wing!",
                "I'd love to see some hostile mobs in safe enclosures!",
                "Every creature deserves a place in the museum!",
                "From tiny bats to mighty wardens, all belong here!",
                "Even the Ender Dragon can be displayed... if you're brave enough!"
            };
            return wlHints[Math.abs(idx) % wlHints.length];
        }
        if (minPct == artPct) {
            String[] artHints = {
                "The gallery walls are looking rather bare...",
                "Fine art deserves a fine museum, don't you think?",
                "I've heard tales of masterpiece paintings scattered across the land...",
                "From the Mona Lisa to Starry Night, bring me the world's greatest works!",
                "There are 64 masterpiece paintings to discover. How many have you found?"
            };
            return artHints[Math.abs(idx) % artHints.length];
        }
        // Default: items hints
        String[] itemHints = {
            "I hear miners have been finding interesting ores lately...",
            "Have you tried donating some of your crafting materials?",
            "The collection wing could use some building blocks!",
            "Tools, armor, food, redstone... all manner of items belong here!",
            "Don't forget the dungeon loot and relics for the collection!"
        };
        return itemHints[Math.abs(idx) % itemHints.length];
    }

    public boolean isPushable() {
        return false;
    }

    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("IsCurator", true);
    }

    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
    }

    public boolean shouldShowName() {
        return true;
    }

    private String serializeMuseumData(MuseumData data, UUID pid) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"items\":").append(this.setToJsonArray(data.getDonatedItems(pid))).append(",");
        sb.append("\"mobs\":").append(this.setToJsonArray(data.getDonatedMobs(pid))).append(",");
        sb.append("\"art\":").append(this.setToJsonArray(data.getDonatedArt(pid))).append(",");
        sb.append("\"achievements\":").append(this.setToJsonArray(data.getCompletedAchievements(pid))).append(",");

        // Completion stats
        Set<String> mobs = data.getDonatedMobs(pid);
        int aqDonated = 0;
        for (AquariumCatalog.MobEntry e : AquariumCatalog.ENTRIES) {
            if (mobs.contains(e.entityId())) aqDonated++;
        }
        int wlDonated = 0;
        for (WildlifeCatalog.MobEntry e : WildlifeCatalog.ENTRIES) {
            if (mobs.contains(e.entityId())) wlDonated++;
        }
        int artDonated = 0;
        for (ArtCatalog.ArtEntry e : ArtCatalog.ENTRIES) {
            if (data.getDonatedArt(pid).contains(e.id())) artDonated++;
        }
        int achDonated = 0;
        for (AchievementCatalog.AchievementEntry e : AchievementCatalog.ENTRIES) {
            if (data.getCompletedAchievements(pid).contains(e.advancementId())) achDonated++;
        }
        // Count unique donated items that are in the catalog
        Set<String> donatedItems = data.getDonatedItems(pid);
        java.util.HashSet<String> uniqueCatalogItems = new java.util.HashSet<>();
        for (java.util.List<String> catItems : ItemCatalog.ITEMS_BY_CATEGORY.values()) {
            uniqueCatalogItems.addAll(catItems);
        }
        int itemDonated = 0;
        for (String item : uniqueCatalogItems) {
            if (donatedItems.contains(item)) itemDonated++;
        }
        int totalDonated = aqDonated + wlDonated + artDonated + achDonated + itemDonated;
        int totalCatalog = AquariumCatalog.getTotalCount() + WildlifeCatalog.getTotalCount()
                + ArtCatalog.getTotalCount() + AchievementCatalog.getTotalCount()
                + ItemCatalog.getTotalItemCount();
        int overall = totalCatalog > 0 ? totalDonated * 100 / totalCatalog : 0;

        sb.append("\"completion\":{");
        sb.append("\"aquarium\":").append(aqDonated).append(",");
        sb.append("\"aquarium_total\":").append(AquariumCatalog.getTotalCount()).append(",");
        sb.append("\"wildlife\":").append(wlDonated).append(",");
        sb.append("\"wildlife_total\":").append(WildlifeCatalog.getTotalCount()).append(",");
        sb.append("\"art\":").append(artDonated).append(",");
        sb.append("\"art_total\":").append(ArtCatalog.getTotalCount()).append(",");
        sb.append("\"items\":").append(itemDonated).append(",");
        sb.append("\"items_total\":").append(ItemCatalog.getTotalItemCount()).append(",");
        sb.append("\"achievements\":").append(achDonated).append(",");
        sb.append("\"achievements_total\":").append(AchievementCatalog.getTotalCount()).append(",");
        sb.append("\"overall\":").append(overall);
        sb.append("},");

        // Donation timestamps for History tab
        java.util.Map<String, Long> timestamps = data.getAllTimestamps(pid);
        sb.append("\"timestamps\":{");
        boolean firstTs = true;
        for (java.util.Map.Entry<String, Long> ts : timestamps.entrySet()) {
            if (!firstTs) sb.append(",");
            sb.append("\"").append(ts.getKey()).append("\":").append(ts.getValue());
            firstTs = false;
        }
        sb.append("}");

        sb.append("}");
        return sb.toString();
    }

    private String setToJsonArray(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String s : set) {
            if (!first) {
                sb.append(",");
            }
            sb.append("\"").append(s).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}

