package com.ultra.megamod.feature.combat.spell;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.lib.spellengine.client.gui.SpellTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Bridges NeoForge's {@link ItemTooltipEvent} into SpellEngine's
 * {@link SpellTooltip#addSpellLines(net.minecraft.world.item.ItemStack,
 * net.minecraft.world.item.TooltipFlag, java.util.List)} so items carrying a
 * {@code SpellContainer} data component (wizard/paladin/rogue/archer weapons,
 * relic tomes, Arsenal uniques, spell books, spell scrolls) render their spell
 * list inline with the vanilla tooltip block.
 *
 * <p>Ported from Fabric SpellEngine's {@code ItemTooltipCallback} registration,
 * which on Fabric was a client-side entrypoint. NeoForge has no equivalent
 * library-level hook, so MegaMod owns the bridge here. Runs late (LOW priority)
 * so other mods' stat/attribute tooltips render above the spell block.</p>
 */
@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)
public class SpellEngineTooltipBridge {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().isEmpty()) return;
        SpellTooltip.addSpellLines(event.getItemStack(), event.getFlags(), event.getToolTip());
    }
}
