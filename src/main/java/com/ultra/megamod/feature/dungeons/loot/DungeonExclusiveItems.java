/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.TooltipDisplay
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredItem
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.DeferredRegister$Items
 */
package com.ultra.megamod.feature.dungeons.loot;

import java.util.function.Consumer;
import java.util.function.Supplier;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.dungeons.item.DungeonArmorItem;
import com.ultra.megamod.feature.dungeons.item.MythicNetheriteItem;
import com.ultra.megamod.feature.dungeons.items.AbsorptionOrbItem;
import com.ultra.megamod.feature.dungeons.items.LivingDiviningRodItem;
import com.ultra.megamod.feature.dungeons.items.StrangeMeatItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DungeonExclusiveItems {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks((String)"megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems((String)"megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"megamod");
    public static final DeferredBlock<Block> WRAITH_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("wraith_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> WRAITH_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("wraith_trophy", WRAITH_TROPHY_BLOCK);
    public static final DeferredBlock<Block> OSSUKAGE_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("ossukage_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> OSSUKAGE_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("ossukage_trophy", OSSUKAGE_TROPHY_BLOCK);
    public static final DeferredBlock<Block> DUNGEON_KEEPER_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("dungeon_keeper_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> DUNGEON_KEEPER_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("dungeon_keeper_trophy", DUNGEON_KEEPER_TROPHY_BLOCK);
    public static final DeferredBlock<Block> FROSTMAW_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("frostmaw_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> FROSTMAW_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("frostmaw_trophy", FROSTMAW_TROPHY_BLOCK);
    public static final DeferredBlock<Block> WROUGHTNAUT_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("wroughtnaut_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> WROUGHTNAUT_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("wroughtnaut_trophy", WROUGHTNAUT_TROPHY_BLOCK);
    public static final DeferredBlock<Block> UMVUTHI_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("umvuthi_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> UMVUTHI_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("umvuthi_trophy", UMVUTHI_TROPHY_BLOCK);
    public static final DeferredBlock<Block> CHAOS_SPAWNER_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("chaos_spawner_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> CHAOS_SPAWNER_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("chaos_spawner_trophy", CHAOS_SPAWNER_TROPHY_BLOCK);
    public static final DeferredBlock<Block> SCULPTOR_TROPHY_BLOCK = BLOCKS.registerSimpleBlock("sculptor_trophy", () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion());
    public static final DeferredItem<BlockItem> SCULPTOR_TROPHY_ITEM = ITEMS.registerSimpleBlockItem("sculptor_trophy", SCULPTOR_TROPHY_BLOCK);
    public static final DeferredItem<Item> VOID_SHARD = ITEMS.registerItem("void_shard", props -> new Item(props.stacksTo(16)){

        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept((Component)Component.literal((String)"A fragment of condensed void energy").withStyle(ChatFormatting.DARK_PURPLE));
            tooltip.accept((Component)Component.empty());
            tooltip.accept((Component)Component.literal((String)"Used to craft powerful artifacts").withStyle(ChatFormatting.GRAY));
            tooltip.accept((Component)Component.literal((String)"Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }

        public boolean isFoil(ItemStack stack) {
            return true;
        }
    });
    public static final DeferredItem<Item> BOSS_TROPHY = ITEMS.registerItem("boss_trophy", props -> new Item(props.stacksTo(1)){

        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept((Component)Component.literal((String)"A trophy from a defeated dungeon boss").withStyle(ChatFormatting.GOLD));
            tooltip.accept((Component)Component.empty());
            tooltip.accept((Component)Component.literal((String)"A symbol of your triumph").withStyle(ChatFormatting.GRAY));
            tooltip.accept((Component)Component.literal((String)"Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }

        public boolean isFoil(ItemStack stack) {
            return true;
        }
    });
    public static final DeferredItem<Item> DUNGEON_MAP = ITEMS.registerItem("dungeon_map", props -> new Item(props.stacksTo(1)){
        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;

            // Apply Glowing to all mobs in 64-block radius
            net.minecraft.world.phys.AABB area = player.getBoundingBox().inflate(64.0);
            int revealed = 0;
            for (net.minecraft.world.entity.LivingEntity mob : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area,
                    e -> e != player && !e.isAlliedTo(player))) {
                mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 200, 0, true, false));
                revealed++;
            }

            ((net.minecraft.server.level.ServerPlayer) player).sendSystemMessage((Component)Component.literal("Map reveals " + revealed + " entities nearby!").withStyle(ChatFormatting.AQUA));
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.5f);

            // 30-second cooldown
            player.getCooldowns().addCooldown(stack, 600);

            return InteractionResult.SUCCESS;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept(Component.literal("An ancient map showing dungeon passages").withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.empty());
            tooltip.accept(Component.literal("Use to reveal all nearby entities").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("30 second cooldown").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }
    });
    public static final DeferredItem<Item> INFERNAL_ESSENCE = ITEMS.registerItem("infernal_essence", props -> new Item(props.stacksTo(8)){

        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept((Component)Component.literal((String)"Concentrated infernal power").withStyle(ChatFormatting.RED));
            tooltip.accept((Component)Component.empty());
            tooltip.accept((Component)Component.literal((String)"Only from Infernal tier bosses").withStyle(ChatFormatting.GRAY));
            tooltip.accept((Component)Component.literal((String)"Used to forge legendary gear").withStyle(ChatFormatting.GRAY));
            tooltip.accept((Component)Component.literal((String)"Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }

        public boolean isFoil(ItemStack stack) {
            return true;
        }
    });
    public static final DeferredItem<Item> WARP_STONE = ITEMS.registerItem("warp_stone", props -> new Item(props.stacksTo(4)){
        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            ItemStack stack = player.getItemInHand(hand);

            // Get the player's current dungeon instance
            com.ultra.megamod.feature.dungeons.DungeonManager manager = com.ultra.megamod.feature.dungeons.DungeonManager.get((net.minecraft.server.level.ServerLevel) level);
            com.ultra.megamod.feature.dungeons.DungeonManager.DungeonInstance instance = manager.getDungeonForPlayer(player.getUUID());

            if (instance == null) {
                ((net.minecraft.server.level.ServerPlayer) player).sendSystemMessage((Component)Component.literal("You are not in a dungeon!").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // Teleport to dungeon entrance (spawn point is blockPos offset by 0,1,5)
            net.minecraft.core.BlockPos entrance = instance.blockPos.offset(0, 1, 5);
            player.teleportTo(entrance.getX() + 0.5, entrance.getY(), entrance.getZ() + 0.5);
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.CHORUS_FRUIT_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
            ((net.minecraft.server.level.ServerPlayer) player).sendSystemMessage((Component)Component.literal("Warped back to dungeon entrance!").withStyle(ChatFormatting.LIGHT_PURPLE));

            // Consume one stone
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept(Component.literal("A shimmering stone of spatial magic").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.accept(Component.empty());
            tooltip.accept(Component.literal("Use to warp back to dungeon entrance").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }
    });

    // New unique items
    public static final DeferredItem<StrangeMeatItem> STRANGE_MEAT = ITEMS.registerItem("strange_meat", props -> new StrangeMeatItem(props.stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<LivingDiviningRodItem> LIVING_DIVINING_ROD = ITEMS.registerItem("living_divining_rod", props -> new LivingDiviningRodItem(props.stacksTo(1).durability(100).rarity(Rarity.RARE)));
    public static final DeferredItem<AbsorptionOrbItem> ABSORPTION_ORB = ITEMS.registerItem("absorption_orb", props -> new AbsorptionOrbItem(props.stacksTo(1).durability(250).rarity(Rarity.RARE)));

    // New dungeon materials
    public static final DeferredItem<Item> CERULEAN_INGOT = ITEMS.registerItem("cerulean_ingot", props -> new Item(props.stacksTo(64)){
        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept(Component.literal("A cool blue metal from the dungeon depths").withStyle(ChatFormatting.BLUE));
            tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }
    });
    public static final DeferredItem<Item> CRYSTALLINE_SHARD = ITEMS.registerItem("crystalline_shard", props -> new Item(props.stacksTo(64)){
        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept(Component.literal("A fragment of enchanted crystal").withStyle(ChatFormatting.AQUA));
            tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
    });
    public static final DeferredItem<Item> SPECTRAL_SILK = ITEMS.registerItem("spectral_silk", props -> new Item(props.stacksTo(32).rarity(Rarity.UNCOMMON)){
        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept(Component.literal("Ethereal thread woven by spectral entities").withStyle(ChatFormatting.WHITE));
            tooltip.accept(Component.literal("Used for crafting spectral gear").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }
    });
    public static final DeferredItem<Item> UMBRA_INGOT = ITEMS.registerItem("umbra_ingot", props -> new Item(props.stacksTo(64).rarity(Rarity.UNCOMMON)){
        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, tooltip, flag);
            tooltip.accept(Component.literal("Dark metal forged in shadow").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.accept(Component.literal("Found only in higher-tier dungeons").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
        }
    });

    // Mythic Netherite gear — Mythic+ dungeon exclusive
    public static final DeferredItem<MythicNetheriteItem> MYTHIC_NETHERITE_SWORD = ITEMS.registerItem("mythic_netherite_sword",
            props -> new MythicNetheriteItem(props, MythicNetheriteItem.Piece.SWORD));
    public static final DeferredItem<MythicNetheriteItem> MYTHIC_NETHERITE_AXE = ITEMS.registerItem("mythic_netherite_axe",
            props -> new MythicNetheriteItem(props, MythicNetheriteItem.Piece.AXE));
    public static final DeferredItem<MythicNetheriteItem> MYTHIC_NETHERITE_HELMET = ITEMS.registerItem("mythic_netherite_helmet",
            props -> new MythicNetheriteItem(props, MythicNetheriteItem.Piece.HELMET));
    public static final DeferredItem<MythicNetheriteItem> MYTHIC_NETHERITE_CHESTPLATE = ITEMS.registerItem("mythic_netherite_chestplate",
            props -> new MythicNetheriteItem(props, MythicNetheriteItem.Piece.CHESTPLATE));
    public static final DeferredItem<MythicNetheriteItem> MYTHIC_NETHERITE_LEGGINGS = ITEMS.registerItem("mythic_netherite_leggings",
            props -> new MythicNetheriteItem(props, MythicNetheriteItem.Piece.LEGGINGS));
    public static final DeferredItem<MythicNetheriteItem> MYTHIC_NETHERITE_BOOTS = ITEMS.registerItem("mythic_netherite_boots",
            props -> new MythicNetheriteItem(props, MythicNetheriteItem.Piece.BOOTS));

    // --- Dungeon Armor: rollable Chainmail / Iron / Diamond / Netherite ---
    public static final DeferredItem<DungeonArmorItem> DUNGEON_CHAINMAIL_HELMET = ITEMS.registerItem("dungeon_chainmail_helmet",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.CHAINMAIL, EquipmentSlot.HEAD));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_CHAINMAIL_CHESTPLATE = ITEMS.registerItem("dungeon_chainmail_chestplate",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.CHAINMAIL, EquipmentSlot.CHEST));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_CHAINMAIL_LEGGINGS = ITEMS.registerItem("dungeon_chainmail_leggings",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.CHAINMAIL, EquipmentSlot.LEGS));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_CHAINMAIL_BOOTS = ITEMS.registerItem("dungeon_chainmail_boots",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.CHAINMAIL, EquipmentSlot.FEET));

    public static final DeferredItem<DungeonArmorItem> DUNGEON_IRON_HELMET = ITEMS.registerItem("dungeon_iron_helmet",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.IRON, EquipmentSlot.HEAD));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_IRON_CHESTPLATE = ITEMS.registerItem("dungeon_iron_chestplate",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.IRON, EquipmentSlot.CHEST));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_IRON_LEGGINGS = ITEMS.registerItem("dungeon_iron_leggings",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.IRON, EquipmentSlot.LEGS));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_IRON_BOOTS = ITEMS.registerItem("dungeon_iron_boots",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.IRON, EquipmentSlot.FEET));

    public static final DeferredItem<DungeonArmorItem> DUNGEON_DIAMOND_HELMET = ITEMS.registerItem("dungeon_diamond_helmet",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.DIAMOND, EquipmentSlot.HEAD));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_DIAMOND_CHESTPLATE = ITEMS.registerItem("dungeon_diamond_chestplate",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.DIAMOND, EquipmentSlot.CHEST));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_DIAMOND_LEGGINGS = ITEMS.registerItem("dungeon_diamond_leggings",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.DIAMOND, EquipmentSlot.LEGS));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_DIAMOND_BOOTS = ITEMS.registerItem("dungeon_diamond_boots",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.DIAMOND, EquipmentSlot.FEET));

    public static final DeferredItem<DungeonArmorItem> DUNGEON_NETHERITE_HELMET = ITEMS.registerItem("dungeon_netherite_helmet",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.NETHERITE, EquipmentSlot.HEAD));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_NETHERITE_CHESTPLATE = ITEMS.registerItem("dungeon_netherite_chestplate",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.NETHERITE, EquipmentSlot.CHEST));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_NETHERITE_LEGGINGS = ITEMS.registerItem("dungeon_netherite_leggings",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.NETHERITE, EquipmentSlot.LEGS));
    public static final DeferredItem<DungeonArmorItem> DUNGEON_NETHERITE_BOOTS = ITEMS.registerItem("dungeon_netherite_boots",
            props -> new DungeonArmorItem(props, DungeonArmorItem.Material.NETHERITE, EquipmentSlot.FEET));

    /** Lookup a registered dungeon armor item by material and slot. */
    public static Item getDungeonArmor(DungeonArmorItem.Material material, EquipmentSlot slot) {
        return switch (material) {
            case CHAINMAIL -> switch (slot) {
                case HEAD -> DUNGEON_CHAINMAIL_HELMET.get();
                case CHEST -> DUNGEON_CHAINMAIL_CHESTPLATE.get();
                case LEGS -> DUNGEON_CHAINMAIL_LEGGINGS.get();
                default -> DUNGEON_CHAINMAIL_BOOTS.get();
            };
            case IRON -> switch (slot) {
                case HEAD -> DUNGEON_IRON_HELMET.get();
                case CHEST -> DUNGEON_IRON_CHESTPLATE.get();
                case LEGS -> DUNGEON_IRON_LEGGINGS.get();
                default -> DUNGEON_IRON_BOOTS.get();
            };
            case DIAMOND -> switch (slot) {
                case HEAD -> DUNGEON_DIAMOND_HELMET.get();
                case CHEST -> DUNGEON_DIAMOND_CHESTPLATE.get();
                case LEGS -> DUNGEON_DIAMOND_LEGGINGS.get();
                default -> DUNGEON_DIAMOND_BOOTS.get();
            };
            case NETHERITE -> switch (slot) {
                case HEAD -> DUNGEON_NETHERITE_HELMET.get();
                case CHEST -> DUNGEON_NETHERITE_CHESTPLATE.get();
                case LEGS -> DUNGEON_NETHERITE_LEGGINGS.get();
                default -> DUNGEON_NETHERITE_BOOTS.get();
            };
        };
    }

    // --- Armor Creative Tab ---
    public static final Supplier<CreativeModeTab> ARMOR_TAB = CREATIVE_MODE_TABS.register("megamod_armor_tab", () -> CreativeModeTab.builder()
        .title((Component)Component.literal((String)"MegaMod - Armor"))
        .icon(() -> new ItemStack((ItemLike)Items.DIAMOND_CHESTPLATE))
        .displayItems((parameters, output) -> {
            // Dungeon Chainmail
            output.accept((ItemLike)DUNGEON_CHAINMAIL_HELMET.get());
            output.accept((ItemLike)DUNGEON_CHAINMAIL_CHESTPLATE.get());
            output.accept((ItemLike)DUNGEON_CHAINMAIL_LEGGINGS.get());
            output.accept((ItemLike)DUNGEON_CHAINMAIL_BOOTS.get());
            // Dungeon Iron
            output.accept((ItemLike)DUNGEON_IRON_HELMET.get());
            output.accept((ItemLike)DUNGEON_IRON_CHESTPLATE.get());
            output.accept((ItemLike)DUNGEON_IRON_LEGGINGS.get());
            output.accept((ItemLike)DUNGEON_IRON_BOOTS.get());
            // Dungeon Diamond
            output.accept((ItemLike)DUNGEON_DIAMOND_HELMET.get());
            output.accept((ItemLike)DUNGEON_DIAMOND_CHESTPLATE.get());
            output.accept((ItemLike)DUNGEON_DIAMOND_LEGGINGS.get());
            output.accept((ItemLike)DUNGEON_DIAMOND_BOOTS.get());
            // Dungeon Netherite
            output.accept((ItemLike)DUNGEON_NETHERITE_HELMET.get());
            output.accept((ItemLike)DUNGEON_NETHERITE_CHESTPLATE.get());
            output.accept((ItemLike)DUNGEON_NETHERITE_LEGGINGS.get());
            output.accept((ItemLike)DUNGEON_NETHERITE_BOOTS.get());
            // Geomancer Set
            output.accept((ItemLike)DungeonEntityRegistry.GEOMANCER_HELM.get());
            output.accept((ItemLike)DungeonEntityRegistry.GEOMANCER_CHEST.get());
            output.accept((ItemLike)DungeonEntityRegistry.GEOMANCER_LEGS.get());
            output.accept((ItemLike)DungeonEntityRegistry.GEOMANCER_BOOTS.get());
            // Wrought Helm
            output.accept((ItemLike)DungeonEntityRegistry.WROUGHT_HELM.get());
            // Mythic Netherite Armor
            output.accept((ItemLike)MYTHIC_NETHERITE_HELMET.get());
            output.accept((ItemLike)MYTHIC_NETHERITE_CHESTPLATE.get());
            output.accept((ItemLike)MYTHIC_NETHERITE_LEGGINGS.get());
            output.accept((ItemLike)MYTHIC_NETHERITE_BOOTS.get());
        }).build());

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}

