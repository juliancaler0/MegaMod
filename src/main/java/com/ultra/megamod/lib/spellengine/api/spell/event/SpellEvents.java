package com.ultra.megamod.lib.spellengine.api.spell.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import com.ultra.megamod.lib.spellengine.api.event.Event;
import com.ultra.megamod.lib.spellengine.api.event.StagedEvent;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.entity.SpellProjectile;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellEvents {
    public static final StagedEvent<CastingAttemptEvent> CASTING_ATTEMPT = new StagedEvent<>();
    public interface CastingAttemptEvent {
        record Args(Player caster, Holder<Spell> spell, ItemStack itemStack) {}
        @Nullable SpellCast.Attempt onCastingAttempt(Args args);
    }

    public static final Event<SpellCostConsumeEvent> COST_CONSUME = new Event<>();
    public interface SpellCostConsumeEvent {
        record Args(Player caster, Holder<Spell> spell, ItemStack itemStack) {}
        void onSpellCostConsume(Args args);
    }

    public static final Event<SpellCastEvent> SPELL_CAST = new Event<SpellCastEvent>();
    public interface SpellCastEvent {
        record Args(Player caster, Holder<Spell> spell, List<Entity> targets, SpellCast.Action action, float progress) {}
        void onSpellCast(Args args);
    }

    public static final Event<HealEvent> HEAL = new Event<HealEvent>();
    public interface HealEvent {
        record Args(LivingEntity caster, Holder<Spell> spell, LivingEntity target, float amount) {}
        void onHeal(Args args);
    }

    // Projectile Launch event
    public static final Event<ProjectileLaunch> PROJECTILE_SHOOT = new Event<ProjectileLaunch>();
    public static final Event<ProjectileLaunch> PROJECTILE_FALL = new Event<ProjectileLaunch>();
    public record ProjectileLaunchEvent(SpellProjectile projectile,
                                        Spell.LaunchProperties mutableLaunchProperties,
                                        LivingEntity caster,
                                        @Nullable Entity target,
                                        Holder<Spell> spellEntry,
                                        SpellHelper.ImpactContext context,
                                        int sequenceIndex) { }
    public interface ProjectileLaunch {
        void onProjectileLaunch(ProjectileLaunchEvent event);
    }
}