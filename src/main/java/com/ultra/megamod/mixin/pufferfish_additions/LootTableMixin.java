package com.ultra.megamod.mixin.pufferfish_additions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.ultra.megamod.lib.pufferfish_additions.experience.HarvestExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LootTable.class, priority = /* In case mods auto-cancel the event by setting a return value */ 750)
public class LootTableMixin {
    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At(value = "RETURN"))
    private ObjectArrayList<ItemStack> pufferfish_unofficial_additions$applyExperienceSource(final ObjectArrayList<ItemStack> loot, @Local(argsOnly = true) final LootContext context) {
        if (!context.hasParameter(LootContextParams.BLOCK_STATE) || !context.hasParameter(LootContextParams.THIS_ENTITY)) {
            return loot;
        }

        BlockState state = context.getParameter(LootContextParams.BLOCK_STATE);
        Entity entity = context.getParameter(LootContextParams.THIS_ENTITY);
        ItemStack tool;

        if (context.hasParameter(LootContextParams.TOOL)) {
            tool = context.getParameter(LootContextParams.TOOL);
        } else {
            tool = ItemStack.EMPTY;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            SkillsAPI.updateExperienceSources(serverPlayer, HarvestExperienceSource.class, source -> source.getValue(serverPlayer, state, tool, loot));
        }

        return loot;
    }
}
