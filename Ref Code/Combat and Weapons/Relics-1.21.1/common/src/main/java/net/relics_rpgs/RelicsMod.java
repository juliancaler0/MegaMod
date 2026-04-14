package net.relics_rpgs;

import net.relics_rpgs.config.ItemConfig;
import net.relics_rpgs.item.RelicItems;
import net.relics_rpgs.spell.RelicEffects;
import net.relics_rpgs.spell.RelicMechanics;
import net.relics_rpgs.spell.RelicSounds;
import net.spell_engine.api.config.ConfigFile;
import net.tiny_config.ConfigManager;

public class RelicsMod {
    public static final String NAMESPACE = "relics_rpgs";
    public static final String DIRECTORY = "relics";
    public static ConfigManager<ItemConfig> itemConfig = new ConfigManager<>
            ("items_v2", new ItemConfig())
            .builder()
            .setDirectory(DIRECTORY)
            .sanitize(true)
            .build();

    public static ConfigManager<ConfigFile.Effects> effectConfig = new ConfigManager<>
            ("effects", new ConfigFile.Effects())
            .builder()
            .setDirectory(DIRECTORY)
            .sanitize(true)
            .build();

    public static void init() {
        itemConfig.refresh();
        effectConfig.refresh();
        RelicMechanics.init();
        itemConfig.save();
        effectConfig.save();
    }

    public static void registerSounds() {
        RelicSounds.register();
    }

    public static void registerItems() {
        RelicItems.register(itemConfig.value.entries);
        itemConfig.save();
    }

    public static void registerEffects() {
        RelicEffects.register(effectConfig.value);
        effectConfig.save();
    }
}
