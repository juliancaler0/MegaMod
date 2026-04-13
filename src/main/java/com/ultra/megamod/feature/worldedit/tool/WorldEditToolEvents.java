package com.ultra.megamod.feature.worldedit.tool;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.worldedit.EditSession;
import com.ultra.megamod.feature.worldedit.WorldEditManager;
import com.ultra.megamod.feature.worldedit.WorldEditRegistry;
import com.ultra.megamod.feature.worldedit.brush.BrushBinding;
import com.ultra.megamod.feature.worldedit.history.ChangeSet;
import com.ultra.megamod.feature.worldedit.pattern.BlockPattern;
import com.ultra.megamod.feature.worldedit.pattern.Pattern;
import com.ultra.megamod.feature.worldedit.session.LocalSession;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Dispatches PlayerInteractEvent to WorldEdit tool items:
 * the wand, far-wand, brush, super pickaxe, info tool, tree planter.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public final class WorldEditToolEvents {

    private WorldEditToolEvents() {}

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;
        ItemStack held = sp.getItemInHand(event.getHand());
        if (held.isEmpty()) return;
        var item = held.getItem();

        if (item == WorldEditRegistry.WAND.get()) {
            LocalSession session = WorldEditManager.getSession(sp);
            session.setPos1(event.getPos().immutable(), sp.level().dimension());
            sp.sendSystemMessage(Component.literal("pos1 set: " + fmt(event.getPos())).withStyle(ChatFormatting.LIGHT_PURPLE));
            event.setCanceled(true);
            return;
        }
        if (item == WorldEditRegistry.SUPER_PICKAXE.get()) {
            ServerLevel lvl = sp.level();
            BlockPos p = event.getPos();
            BlockState st = lvl.getBlockState(p);
            if (!st.isAir()) {
                EditSession es = new EditSession(lvl);
                es.setBlock(p, Blocks.AIR.defaultBlockState());
                recordIfChanges(sp, es, "super pickaxe");
            }
            event.setCanceled(true);
            return;
        }
        if (item == WorldEditRegistry.INFO_TOOL.get()) {
            ServerLevel lvl = sp.level();
            BlockPos p = event.getPos();
            BlockState st = lvl.getBlockState(p);
            String id = BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString();
            int light = lvl.getMaxLocalRawBrightness(p);
            sp.sendSystemMessage(Component.literal("[Info] ").withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(id).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" @ " + fmt(p) + " light=" + light).withStyle(ChatFormatting.GRAY)));
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;
        ItemStack held = sp.getMainHandItem();
        if (held.getItem() == WorldEditRegistry.FAR_WAND.get()) {
            BlockHitResult hit = raycast(sp, 300);
            if (hit.getType() == HitResult.Type.BLOCK) {
                LocalSession session = WorldEditManager.getSession(sp);
                session.setPos1(hit.getBlockPos().immutable(), sp.level().dimension());
                sp.sendSystemMessage(Component.literal("pos1 (far): " + fmt(hit.getBlockPos())).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;
        ItemStack held = sp.getItemInHand(event.getHand());
        if (held.isEmpty()) return;
        var item = held.getItem();

        if (item == WorldEditRegistry.WAND.get()) {
            LocalSession session = WorldEditManager.getSession(sp);
            session.setPos2(event.getPos().immutable(), sp.level().dimension());
            sp.sendSystemMessage(Component.literal("pos2 set: " + fmt(event.getPos())).withStyle(ChatFormatting.LIGHT_PURPLE));
            event.setCanceled(true);
            return;
        }
        if (item == WorldEditRegistry.BRUSH.get()) {
            applyBrush(sp, event.getPos());
            event.setCanceled(true);
            return;
        }
        if (item == WorldEditRegistry.TREE_PLANTER.get()) {
            ServerLevel lvl = sp.level();
            BlockPos above = event.getPos().above();
            if (lvl.getBlockState(event.getPos()).isSolid()) {
                EditSession es = new EditSession(lvl);
                placeSimpleOak(es, above, lvl.random);
                recordIfChanges(sp, es, "tree planter");
            }
            event.setCanceled(true);
        }
    }

    private static void placeSimpleOak(EditSession es, BlockPos base, net.minecraft.util.RandomSource rand) {
        int trunkHeight = 4 + rand.nextInt(3);
        var log = net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState();
        var leaves = net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState();
        for (int i = 0; i < trunkHeight; i++) es.setBlock(base.above(i), log);
        BlockPos top = base.above(trunkHeight);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
            for (int dy = -1; dy <= 0; dy++) {
                int dist = Math.abs(dx) + Math.abs(dz) + Math.abs(dy);
                if (dist <= 3) es.setBlock(top.offset(dx, dy, dz), leaves);
            }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 || dz == 0) es.setBlock(top.offset(dx, 1, dz), leaves);
        }
        es.setBlock(top.above(), leaves);
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (!AdminSystem.isAdmin(sp)) return;
        ItemStack held = sp.getMainHandItem();
        if (held.getItem() == WorldEditRegistry.FAR_WAND.get()) {
            BlockHitResult hit = raycast(sp, 300);
            if (hit.getType() == HitResult.Type.BLOCK) {
                LocalSession session = WorldEditManager.getSession(sp);
                session.setPos2(hit.getBlockPos().immutable(), sp.level().dimension());
                sp.sendSystemMessage(Component.literal("pos2 (far): " + fmt(hit.getBlockPos())).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            return;
        }
        if (held.getItem() == WorldEditRegistry.BRUSH.get()) {
            // Brush via raycast
            BlockHitResult hit = raycast(sp, 300);
            if (hit.getType() == HitResult.Type.BLOCK) {
                applyBrush(sp, hit.getBlockPos());
            }
        }
    }

    private static void applyBrush(ServerPlayer sp, BlockPos pos) {
        LocalSession session = WorldEditManager.getSession(sp);
        BrushBinding binding = session.getBrush(LocalSession.BrushSlot.MAIN);
        if (binding == null) {
            sp.sendSystemMessage(Component.literal("No brush bound. Use /we_brush sphere 2 stone").withStyle(ChatFormatting.RED));
            return;
        }
        EditSession es = new EditSession(sp.level());
        es.setMask(binding.getMask() != null ? binding.getMask() : session.getActiveMask());
        Pattern pattern = binding.getPattern();
        if (pattern == null) pattern = session.getActivePattern();
        if (pattern == null) pattern = new BlockPattern(Blocks.STONE.defaultBlockState());
        binding.getBrush().apply(es, sp, pos, pattern);
        recordIfChanges(sp, es, "brush");
    }

    private static void recordIfChanges(ServerPlayer sp, EditSession es, String desc) {
        if (es.getBlocksChanged() == 0) return;
        ChangeSet cs = es.getChangeSet();
        cs.setDescription(desc);
        WorldEditManager.getSession(sp).getHistory().record(cs);
        sp.sendSystemMessage(Component.literal("[WE] " + desc + ": " + es.getBlocksChanged() + " blocks").withStyle(ChatFormatting.GREEN));
    }

    private static String fmt(BlockPos p) {
        return p.getX() + ", " + p.getY() + ", " + p.getZ();
    }

    private static BlockHitResult raycast(ServerPlayer sp, double range) {
        Vec3 eye = sp.getEyePosition(1.0f);
        Vec3 look = sp.getViewVector(1.0f);
        Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
        return sp.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, sp));
    }
}
