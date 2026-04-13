package com.ultra.megamod.lib.spellengine.api.spell.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellpower.api.SpellPower;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellHandlers {
    public static final Map<String, CustomDelivery> customDelivery = new HashMap<>();
    public interface CustomDelivery {
        boolean onSpellDelivery(Level world, Holder<Spell> spellEntry, LivingEntity caster,
                                List<SpellHelper.DeliveryTarget> targets, SpellHelper.ImpactContext context,
                                @Nullable Vec3 targetLocation);
    }
    public static void registerCustomDelivery(Identifier id, CustomDelivery delivery) {
        customDelivery.put(id.toString(), delivery);
    }

    public static final Map<String, CustomImpact> customImpact = new HashMap<>();
    public record ImpactResult(boolean success, boolean critical) { }
    public interface CustomImpact {
        ImpactResult onSpellImpact(Holder<Spell> spellEntry, SpellPower.Result spellPower,
                           LivingEntity caster, @Nullable Entity target,
                           SpellHelper.ImpactContext context);
    }
    public static void registerCustomImpact(Identifier id, CustomImpact impact) {
        customImpact.put(id.toString(), impact);
    }
}
