package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.economy.shop.FurnitureShop;
import com.ultra.megamod.feature.economy.shop.ShopItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server-side handler for the furniture showcase admin tab.
 * Places furniture blocks in a grid layout on flat terrain for visual inspection.
 * Layout: rows of 20, spaced 3 blocks apart, with oak signs showing names.
 */
public class FurnitureShowcaseHandler {

    private static final int BLOCKS_PER_ROW = 20;
    private static final int SPACING = 3;
    private static final String SHOWCASE_TAG = "megamod_furniture_showcase";

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) return false;

        switch (action) {
            case "furniture_showcase_place" -> {
                handlePlace(player, jsonData, level, eco);
                return true;
            }
            case "furniture_showcase_clear" -> {
                handleClear(player, level, eco);
                return true;
            }
        }
        return false;
    }

    private static void handlePlace(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String type = obj.get("type").getAsString();

            List<ShopItem> catalog = FurnitureShop.getCatalog();

            // Gather block IDs to place
            List<String> blockIds = new ArrayList<>();
            List<String> displayNames = new ArrayList<>();

            switch (type) {
                case "single" -> {
                    String blockId = obj.get("blockId").getAsString();
                    blockIds.add(blockId);
                    for (ShopItem item : catalog) {
                        if (item.itemId().equals(blockId)) {
                            displayNames.add(item.displayName());
                            break;
                        }
                    }
                    if (displayNames.isEmpty()) displayNames.add(blockId);
                }
                case "category" -> {
                    String category = obj.get("category").getAsString();
                    for (int i = 0; i < catalog.size(); i++) {
                        if (category.equals(FurnitureShop.getCategoryForIndex(i))) {
                            blockIds.add(catalog.get(i).itemId());
                            displayNames.add(catalog.get(i).displayName());
                        }
                    }
                }
                case "all" -> {
                    for (ShopItem item : catalog) {
                        blockIds.add(item.itemId());
                        displayNames.add(item.displayName());
                    }
                }
            }

            if (blockIds.isEmpty()) {
                sendResult(player, "No blocks to place", eco);
                return;
            }

            // Calculate grid origin: 5 blocks in front of player
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position();
            double frontX = look.x;
            double frontZ = look.z;
            double len = Math.sqrt(frontX * frontX + frontZ * frontZ);
            if (len < 0.01) { frontX = 0; frontZ = 1; len = 1; }
            frontX /= len;
            frontZ /= len;
            double rightX = -frontZ;
            double rightZ = frontX;

            int placed = 0;
            int startDistance = 5;

            for (int i = 0; i < blockIds.size(); i++) {
                int row = i / BLOCKS_PER_ROW;
                int col = i % BLOCKS_PER_ROW;

                // Center each row
                int rowSize = Math.min(BLOCKS_PER_ROW, blockIds.size() - row * BLOCKS_PER_ROW);
                double colOffset = (col - (rowSize - 1) / 2.0) * SPACING;
                double rowOffset = startDistance + row * (SPACING + 1);

                double bx = pos.x + frontX * rowOffset + rightX * colOffset;
                double bz = pos.z + frontZ * rowOffset + rightZ * colOffset;
                BlockPos blockPos = new BlockPos((int) Math.floor(bx), (int) pos.y, (int) Math.floor(bz));

                if (placeBlock(level, blockIds.get(i), blockPos)) {
                    placed++;
                    // Place a sign below with the item name
                    BlockPos signPos = blockPos.below();
                    placeNameSign(level, signPos, displayNames.get(i));
                }
            }

            sendResult(player, "Placed " + placed + "/" + blockIds.size() + " furniture blocks in grid", eco);
        } catch (Exception e) {
            sendResult(player, "Error: " + e.getMessage(), eco);
        }
    }

    private static boolean placeBlock(ServerLevel level, String itemId, BlockPos pos) {
        try {
            Identifier id = Identifier.parse(itemId);
            Optional<?> opt = BuiltInRegistries.ITEM.getOptional(id);
            if (opt.isEmpty()) return false;

            if (opt.get() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                BlockState state = block.defaultBlockState();

                // Clear the position and sign position
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), 3);

                // Place the block
                level.setBlock(pos, state, 3);

                // Tag nearby for cleanup — use an invisible armor stand as marker
                net.minecraft.world.entity.decoration.ArmorStand marker =
                    new net.minecraft.world.entity.decoration.ArmorStand(level, pos.getX() + 0.5, pos.getY() - 1.5, pos.getZ() + 0.5);
                marker.setInvisible(true);
                marker.setNoGravity(true);
                marker.setInvulnerable(true);
                marker.setSilent(true);
                marker.addTag(SHOWCASE_TAG);
                marker.addTag("showcase_block:" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ());
                level.addFreshEntity(marker);

                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private static void placeNameSign(ServerLevel level, BlockPos pos, String displayName) {
        // Use an armor stand with custom name as a floating label
        net.minecraft.world.entity.decoration.ArmorStand label =
            new net.minecraft.world.entity.decoration.ArmorStand(level, pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5);
        label.setInvisible(true);
        label.setNoGravity(true);
        label.setInvulnerable(true);
        label.setSilent(true);
        label.setCustomName(net.minecraft.network.chat.Component.literal(displayName));
        label.setCustomNameVisible(true);
        label.addTag(SHOWCASE_TAG);
        // Marker mode not accessible; small armor stand is sufficient
        level.addFreshEntity(label);
    }

    private static void handleClear(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        // Remove all showcase entities within 500 blocks
        AABB area = new AABB(player.blockPosition()).inflate(500);
        List<Entity> entities = level.getEntities((Entity) null, area,
            e -> e.getTags().contains(SHOWCASE_TAG));

        int cleared = 0;
        for (Entity entity : entities) {
            // If it's a block marker, also clear the placed block
            for (String tag : entity.getTags()) {
                if (tag.startsWith("showcase_block:")) {
                    try {
                        String[] parts = tag.split(":");
                        BlockPos bp = new BlockPos(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                        level.setBlock(bp, Blocks.AIR.defaultBlockState(), 3);
                    } catch (Exception ignored) {}
                }
            }
            entity.discard();
            cleared++;
        }
        sendResult(player, "Removed " + cleared + " showcase entities and blocks", eco);
    }

    private static void sendResult(ServerPlayer player, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("msg", message);
        ComputerDataPayload response = new ComputerDataPayload(
            "furniture_showcase_result", obj.toString(),
            eco.getWallet(player.getUUID()),
            eco.getBank(player.getUUID())
        );
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) response, (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}
