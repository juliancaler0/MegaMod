package com.ultra.megamod.feature.combat.wizards;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.wizards.config.Default;
import com.ultra.megamod.feature.combat.wizards.config.TweaksConfig;
import com.ultra.megamod.feature.combat.wizards.content.WizardBooks;
import com.ultra.megamod.feature.combat.wizards.content.WizardSounds;
import com.ultra.megamod.feature.combat.wizards.item.Group;
import com.ultra.megamod.feature.combat.wizards.item.WizardWeapons;
import com.ultra.megamod.feature.combat.wizards.item.armor.Armors; // kept for Group creative-tab icon reference
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;
import net.neoforged.bus.api.IEventBus;

/**
 * Wizards class content initialization.
 * Ports the Fabric Wizards mod (net.wizards) to NeoForge 1.21.11.
 *
 * <p>Registers wand and staff weapons via SpellEngine's
 * {@link com.ultra.megamod.lib.spellengine.rpg_series.item.Weapons} factories,
 * the seven wizard robe armor sets, sounds, and the {@code megamod:wizards}
 * creative tab. Status-effect mixins (FrostShield, Frozen) live under
 * {@code com.ultra.megamod.mixin.wizards} and are wired via
 * {@code megamod.mixins.json}.</p>
 */
public class WizardsMod {
    public static final String ID = MegaMod.MODID;

    // Configs - using static defaults instead of tiny_config ConfigManager
    public static final ConfigFile.Equipment itemConfig = Default.itemConfig;
    public static final TweaksConfig tweaksConfig = new TweaksConfig();

    /**
     * Initialize wizard sound DeferredRegister and config defaults.
     * Called from {@link MegaMod}'s constructor before
     * {@link com.ultra.megamod.lib.spellengine.rpg_series.item.RPGItemRegistry#init(IEventBus)}.
     */
    public static void init(IEventBus modEventBus) {
        // In dev/MegaMod environment we always include compat-mod weapon variants
        // so the assets/recipes resolve even without BetterEnd / BetterNether / Aether.
        tweaksConfig.ignore_items_required_mods = true;

        WizardSounds.init(modEventBus);
    }

    /**
     * Register the wizard creative tab and weapons.
     * Must run after {@link com.ultra.megamod.lib.spellengine.SpellEngineNeoForge#init(IEventBus)}
     * so SpellDataComponents are registered, but before {@code RPGItemRegistry.init}.
     *
     * <p><b>Note on armor:</b> Wizard robes currently remain registered through
     * {@code ClassArmorRegistry} (using the legacy {@code RpgArmorItem} with
     * {@code _boots} piece naming). Migrating them to SpellEngine's
     * {@link Armors} factory registration (which emits {@code _feet} piece IDs)
     * would break ~30 advancement / loot-table / recipe / shop references; the
     * legacy {@code EquipmentSetManager} also already provides comparable set
     * bonuses. Spell-set {@code MODIFIER} bonuses that specifically require the
     * SpellEngine armor pipeline are tracked as follow-up work.</p>
     */
    public static void registerItems(IEventBus modEventBus) {
        Group.init(modEventBus);
        WizardBooks.register();
        WizardWeapons.init(modEventBus);
        // Armors.init(modEventBus); // see note above — kept in ClassArmorRegistry for now
    }
}
