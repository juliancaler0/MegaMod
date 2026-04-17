package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks;

import com.ultra.megamod.lib.pufferfish_additions.PUA;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.AbstractSpell;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.CastType;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.MagicManager;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SchoolRegistry;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SchoolType;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SpellOnCastEvent;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SpellRarity;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SpellRegistry;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.Utils;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
// Registry kept for generic <Registry<T>> type parameter only
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ISSEvents {
    public static void grantSpellExperience(final SpellOnCastEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer caster)) {
            return;
        }

        Holder<SchoolType> school = getHolder(caster.registryAccess(), SchoolRegistry.SCHOOL_REGISTRY_KEY, event.getSchoolType().getId());
        Holder<AbstractSpell> spellHolder = getHolder(caster.registryAccess(), SpellRegistry.SPELL_REGISTRY_KEY, Identifier.tryParse(event.getSpellId()));

        ItemStack mainHand = caster.getMainHandItem();
        ItemStack spellbook = Utils.getPlayerSpellbookStack(caster);

        if (spellbook == null) {
            spellbook = ItemStack.EMPTY;
        }

        AbstractSpell spell = spellHolder.value();
        int spellLevel = event.getSpellLevel();

        SpellRarity rarity = spell.getRarity(spellLevel);

        int minLevelRarity = spell.getMinLevelForRarity(rarity);
        int manaCost = event.getCastSource().consumesMana() ? spell.getManaCost(spellLevel) : 0;
        int manaCostPerSecond = event.getCastSource().consumesMana() ? spell.getCastType() == CastType.CONTINUOUS ? manaCost * (20 / MagicManager.CONTINUOUS_CAST_TICK_INTERVAL) : 0 : 0;
        int castDurationTicks = spell.getCastType() == CastType.CONTINUOUS ? spell.getEffectiveCastTime(spellLevel, caster) : 0;
        double castDuration = castDurationTicks / 20d;
        double castChargeTime = spell.getCastType() == CastType.LONG ? spell.getEffectiveCastTime(spellLevel ,caster) / 20d : 0;
        double cooldown = MagicManager.getEffectiveSpellCooldown(spell, caster, event.getCastSource()) / 20d;
        int expectedTicks = spell.getCastType() == CastType.CONTINUOUS ? (castDurationTicks / 10) : 1;

        Data data = new Data(caster, mainHand, spellbook, school, spellHolder, rarity, spellLevel, minLevelRarity, manaCost, manaCostPerSecond, castDuration, castChargeTime, cooldown, expectedTicks);
        PUA.LOG.debug("Spell experience source data: [{}]", data);

        SkillsAPI.updateExperienceSources(caster, SpellCastingExperienceSource.class, source -> source.getValue(data));
    }

    private static <T> Holder<T> getHolder(final RegistryAccess access, final ResourceKey<Registry<T>> key, final Identifier resource) {
        return access.lookupOrThrow(key).getOrThrow(ResourceKey.create(key, resource));
    }
}
