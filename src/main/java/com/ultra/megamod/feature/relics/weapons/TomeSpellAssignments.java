package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase G.2 — maps each legendary tome's item registry ID to its SpellEngine spell IDs.
 * Used by {@link #props(Item.Properties, String)} to attach a {@link SpellContainer}
 * data component to the tome so right-click casts via SpellEngine's active-cast path.
 *
 * <p>The container uses {@link SpellContainer.ContentType#CONTAINED} so only the exact
 * spell IDs on the tome resolve for the player — tomes do not participate in pool-based
 * spell browsing. Cycling between multiple spells on one tome works via SpellEngine's
 * spell-choice UI (bound to the tome's main slot).
 */
public final class TomeSpellAssignments {
    private TomeSpellAssignments() {}

    public static final Map<String, List<String>> SPELLS;

    static {
        Map<String, List<String>> s = new HashMap<>();
        s.put("megamod:vampiric_tome", List.of(
                "megamod:tome_vampiric_tome_drain_life",
                "megamod:tome_vampiric_tome_blood_pact"));
        s.put("megamod:static_seeker", List.of(
                "megamod:tome_static_seeker_chain_lightning",
                "megamod:tome_static_seeker_overcharge"));
        s.put("megamod:battledancer", List.of(
                "megamod:tome_battledancer_whirlwind",
                "megamod:tome_battledancer_riposte"));
        s.put("megamod:ebonchill", List.of(
                "megamod:tome_ebonchill_frost_nova",
                "megamod:tome_ebonchill_icicle_lance"));
        s.put("megamod:lightbinder", List.of(
                "megamod:tome_lightbinder_holy_smite",
                "megamod:tome_lightbinder_sacred_shield"));
        s.put("megamod:crescent_blade", List.of(
                "megamod:tome_crescent_blade_crescent_slash",
                "megamod:tome_crescent_blade_shadow_dash"));
        s.put("megamod:ghost_fang", List.of(
                "megamod:tome_ghost_fang_spectral_bite",
                "megamod:tome_ghost_fang_phase"));
        s.put("megamod:terra_warhammer", List.of(
                "megamod:tome_terra_warhammer_earthquake",
                "megamod:tome_terra_warhammer_fortify"));
        s.put("megamod:voidreaver", List.of(
                "megamod:tome_voidreaver_nether_rend",
                "megamod:tome_voidreaver_dimensional_collapse"));
        s.put("megamod:solaris", List.of(
                "megamod:tome_solaris_consecrate",
                "megamod:tome_solaris_judgment"));
        s.put("megamod:stormfury", List.of(
                "megamod:tome_stormfury_lightning_dash",
                "megamod:tome_stormfury_thunder_gods_descent"));
        s.put("megamod:briarthorn", List.of(
                "megamod:tome_briarthorn_entangling_roots",
                "megamod:tome_briarthorn_natures_wrath"));
        s.put("megamod:abyssal_trident", List.of(
                "megamod:tome_abyssal_trident_tidal_surge",
                "megamod:tome_abyssal_trident_maelstrom"));
        s.put("megamod:pyroclast", List.of(
                "megamod:tome_pyroclast_molten_strike",
                "megamod:tome_pyroclast_eruption"));
        s.put("megamod:whisperwind", List.of(
                "megamod:tome_whisperwind_piercing_gale",
                "megamod:tome_whisperwind_cyclone_volley"));
        s.put("megamod:soulchain", List.of(
                "megamod:tome_soulchain_soul_lash",
                "megamod:tome_soulchain_reaping_harvest"));
        s.put("megamod:soka_singing_blade", List.of(
                "megamod:tome_soka_singing_blade_annihilating_slash",
                "megamod:tome_soka_singing_blade_grand_arcanum",
                "megamod:tome_soka_singing_blade_archons_judgment"));
        SPELLS = Map.copyOf(s);
    }

    public static List<String> forItem(String itemId) {
        return SPELLS.getOrDefault(itemId, List.of());
    }

    /**
     * Attaches a SpellContainer data component to the given {@link Item.Properties}
     * with the spell IDs mapped for the given tome registry ID.
     */
    public static Item.Properties props(Item.Properties props, String itemId) {
        List<String> spellIds = forItem(itemId);
        if (spellIds.isEmpty()) {
            return props;
        }
        SpellContainer container = new SpellContainer(
                SpellContainer.ContentType.CONTAINED,
                "",
                "",
                "",
                spellIds.size(),
                spellIds,
                0
        );
        return props.component(SpellDataComponents.SPELL_CONTAINER, container);
    }
}
