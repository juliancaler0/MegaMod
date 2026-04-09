/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.ultra.megamod.feature.museum;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.museum.MuseumRegistry;
import com.ultra.megamod.feature.museum.dimension.MuseumDimensionManager;
import com.ultra.megamod.feature.museum.dimension.MuseumPortalHandler;
import com.ultra.megamod.feature.museum.paintings.MasterpiecePaintingItem;
import com.ultra.megamod.feature.museum.network.OpenMuseumPayload;
import com.ultra.megamod.feature.skills.SkillManager;
import com.ultra.megamod.feature.skills.SkillTreeType;
import net.minecraft.world.item.Items;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public class MuseumBlock
extends Block {
    public static final MapCodec<MuseumBlock> CODEC = MuseumBlock.simpleCodec(MuseumBlock::new);

    public MuseumBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    protected MapCodec<? extends MuseumBlock> codec() {
        return CODEC;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (MuseumPortalHandler.shouldDonateItem(serverPlayer)) {
                ItemStack held = serverPlayer.getMainHandItem();
                MuseumData data = MuseumData.get((ServerLevel)level);
                long currentDay = level.getDayTime() / 24000L;
                data.setCurrentDay(currentDay);
                String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
                Item item = held.getItem();
                if (item instanceof SpawnEggItem) {
                    SpawnEggItem spawnEgg = (SpawnEggItem)item;
                    String mobType = BuiltInRegistries.ENTITY_TYPE.getKey(spawnEgg.getType(held)).toString();
                    String mobName = spawnEgg.getType(held).getDescription().getString();
                    if (data.donateMob(serverPlayer.getUUID(), mobType)) {
                        held.shrink(1);
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)("Donated mob: " + mobName)).withStyle(ChatFormatting.GREEN));
                        ServerLevel serverLevel = (ServerLevel) level;
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.5);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
                        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.8f);
                        SkillManager skills = SkillManager.get(serverLevel);
                        skills.addXp(serverPlayer.getUUID(), SkillTreeType.COMBAT, 5);
                    } else {
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Already in your museum!").withStyle(ChatFormatting.YELLOW));
                    }
                } else if (held.getItem() == MuseumRegistry.CAPTURED_MOB_ITEM.get()) {
                    CustomData customData = held.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag customTag = customData.copyTag();
                    String mobType = customTag.getStringOr("megamod_captured_mob", "");
                    if (!mobType.isEmpty() && data.donateMob(serverPlayer.getUUID(), mobType)) {
                        held.shrink(1);
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)("Donated mob: " + mobType)).withStyle(ChatFormatting.GREEN));
                        ServerLevel serverLevel = (ServerLevel) level;
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.5);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
                        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.8f);
                        SkillManager skills = SkillManager.get(serverLevel);
                        skills.addXp(serverPlayer.getUUID(), SkillTreeType.COMBAT, 5);
                    } else {
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Already in your museum!").withStyle(ChatFormatting.YELLOW));
                    }
                } else if (getMobBucketType(itemId) != null) {
                    String mobType = getMobBucketType(itemId);
                    String mobName = mobType.substring(mobType.indexOf(':') + 1).replace('_', ' ');
                    if (data.donateMob(serverPlayer.getUUID(), mobType)) {
                        held.shrink(1);
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)("Donated aquatic mob: " + mobName)).withStyle(ChatFormatting.AQUA));
                        ServerLevel serverLevel = (ServerLevel) level;
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.5);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
                        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.8f);
                        SkillManager skills = SkillManager.get(serverLevel);
                        skills.addXp(serverPlayer.getUUID(), SkillTreeType.COMBAT, 5);
                    } else {
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Already in your museum!").withStyle(ChatFormatting.YELLOW));
                    }
                } else if (item instanceof MasterpiecePaintingItem) {
                    MasterpiecePaintingItem masterpiece = (MasterpiecePaintingItem) item;
                    String artId = masterpiece.getVariantName();
                    if (data.donateArt(serverPlayer.getUUID(), artId)) {
                        held.shrink(1);
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)("Donated masterpiece: " + masterpiece.getTitle())).withStyle(ChatFormatting.LIGHT_PURPLE));
                        ServerLevel serverLevel = (ServerLevel) level;
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.5);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
                        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.8f);
                        SkillManager skills = SkillManager.get(serverLevel);
                        skills.addXp(serverPlayer.getUUID(), SkillTreeType.ARCANE, 5);
                    } else {
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Already in your museum!").withStyle(ChatFormatting.YELLOW));
                    }
                } else if (item == Items.PAINTING) {
                    if (data.donateArt(serverPlayer.getUUID(), "minecraft:painting")) {
                        held.shrink(1);
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Donated: Painting").withStyle(ChatFormatting.GREEN));
                        ServerLevel serverLevel = (ServerLevel) level;
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.5);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
                        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.8f);
                        SkillManager skills = SkillManager.get(serverLevel);
                        skills.addXp(serverPlayer.getUUID(), SkillTreeType.ARCANE, 5);
                    } else {
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Already in your museum!").withStyle(ChatFormatting.YELLOW));
                    }
                } else {
                    String displayName = held.getHoverName().getString();
                    if (data.donateItem(serverPlayer.getUUID(), itemId)) {
                        held.shrink(1);
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)("Donated: " + displayName)).withStyle(ChatFormatting.GREEN));
                        ServerLevel serverLevel = (ServerLevel) level;
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 25, 0.5, 0.8, 0.5, 0.5);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.0);
                        serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5f, 1.8f);
                        SkillManager skills = SkillManager.get(serverLevel);
                        SkillTreeType treeType = getSkillTreeForItem(itemId);
                        int xpAmount = getSkillXpForItem(itemId);
                        skills.addXp(serverPlayer.getUUID(), treeType, xpAmount);
                    } else {
                        serverPlayer.sendSystemMessage((Component)Component.literal((String)"Already in your museum!").withStyle(ChatFormatting.YELLOW));
                    }
                }
                // Refresh museum displays after donation
                MuseumDimensionManager.get((ServerLevel) level).refreshMuseum(serverPlayer);
            } else if (MuseumPortalHandler.shouldOpenCatalog(serverPlayer)) {
                MuseumData data = MuseumData.get((ServerLevel)level);
                UUID pid = serverPlayer.getUUID();
                String json = this.serializeMuseumData(data, pid);
                PacketDistributor.sendToPlayer((ServerPlayer)serverPlayer, (CustomPacketPayload)new OpenMuseumPayload(json), (CustomPacketPayload[])new CustomPacketPayload[0]);
            } else if (MuseumPortalHandler.shouldEnterMuseum(serverPlayer)) {
                MuseumPortalHandler.handleEnterMuseum(serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private String serializeMuseumData(MuseumData data, UUID pid) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"items\":").append(this.setToJsonArray(data.getDonatedItems(pid))).append(",");
        sb.append("\"mobs\":").append(this.setToJsonArray(data.getDonatedMobs(pid))).append(",");
        sb.append("\"art\":").append(this.setToJsonArray(data.getDonatedArt(pid))).append(",");
        sb.append("\"achievements\":").append(this.setToJsonArray(data.getCompletedAchievements(pid))).append(",");

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

    private static String getMobBucketType(String itemId) {
        return switch (itemId) {
            case "minecraft:pufferfish_bucket" -> "minecraft:pufferfish";
            case "minecraft:salmon_bucket" -> "minecraft:salmon";
            case "minecraft:cod_bucket" -> "minecraft:cod";
            case "minecraft:tropical_fish_bucket" -> "minecraft:tropical_fish";
            case "minecraft:axolotl_bucket" -> "minecraft:axolotl";
            case "minecraft:tadpole_bucket" -> "minecraft:tadpole";
            default -> null;
        };
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

    private static final String[] FARMING_KEYWORDS = {
        "wheat", "carrot", "potato", "beetroot", "melon", "pumpkin", "seed",
        "sweet_berries", "apple", "bread", "cookie", "cake", "mushroom_stew",
        "rabbit_stew", "suspicious_stew"
    };

    private static final String[] MINING_KEYWORDS = {
        "ore", "raw_", "deepslate"
    };

    private static SkillTreeType getSkillTreeForItem(String itemId) {
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        for (String keyword : MINING_KEYWORDS) {
            if (path.contains(keyword)) return SkillTreeType.MINING;
        }
        for (String keyword : FARMING_KEYWORDS) {
            if (path.contains(keyword)) return SkillTreeType.FARMING;
        }
        return SkillTreeType.SURVIVAL;
    }

    private static int getSkillXpForItem(String itemId) {
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        for (String keyword : MINING_KEYWORDS) {
            if (path.contains(keyword)) return 3;
        }
        for (String keyword : FARMING_KEYWORDS) {
            if (path.contains(keyword)) return 3;
        }
        return 2;
    }
}

