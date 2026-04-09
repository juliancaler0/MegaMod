package com.ultra.megamod.feature.museum.paintings;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MasterpieceRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "megamod");

    /* 16 masterpiece paintings — dungeon exclusive, museum collectible */
    public static final DeferredItem<MasterpiecePaintingItem> MONA_LISA = reg("mona_lisa", "Mona Lisa", "Leonardo da Vinci");
    public static final DeferredItem<MasterpiecePaintingItem> STARRY_NIGHT = reg("starry_night", "The Starry Night", "Vincent van Gogh");
    public static final DeferredItem<MasterpiecePaintingItem> THE_GREAT_WAVE = reg("the_great_wave", "The Great Wave", "Katsushika Hokusai");
    public static final DeferredItem<MasterpiecePaintingItem> GIRL_PEARL_EARRING = reg("girl_pearl_earring", "Girl with a Pearl Earring", "Johannes Vermeer");
    public static final DeferredItem<MasterpiecePaintingItem> THE_SCREAM = reg("the_scream", "The Scream", "Edvard Munch");
    public static final DeferredItem<MasterpiecePaintingItem> PERSISTENCE_OF_MEMORY = reg("persistence_of_memory", "The Persistence of Memory", "Salvador Dali");
    public static final DeferredItem<MasterpiecePaintingItem> BIRTH_OF_VENUS = reg("birth_of_venus", "The Birth of Venus", "Sandro Botticelli");
    public static final DeferredItem<MasterpiecePaintingItem> AMERICAN_GOTHIC = reg("american_gothic", "American Gothic", "Grant Wood");
    public static final DeferredItem<MasterpiecePaintingItem> THE_KISS = reg("the_kiss", "The Kiss", "Gustav Klimt");
    public static final DeferredItem<MasterpiecePaintingItem> WATER_LILIES = reg("water_lilies", "Water Lilies", "Claude Monet");
    public static final DeferredItem<MasterpiecePaintingItem> CREATION_OF_ADAM = reg("creation_of_adam", "The Creation of Adam", "Michelangelo");
    public static final DeferredItem<MasterpiecePaintingItem> SUNDAY_GRANDE_JATTE = reg("sunday_grande_jatte", "A Sunday on La Grande Jatte", "Georges Seurat");
    public static final DeferredItem<MasterpiecePaintingItem> NIGHTHAWKS = reg("nighthawks", "Nighthawks", "Edward Hopper");
    public static final DeferredItem<MasterpiecePaintingItem> SON_OF_MAN = reg("son_of_man", "The Son of Man", "Rene Magritte");
    public static final DeferredItem<MasterpiecePaintingItem> LIBERTY_LEADING = reg("liberty_leading", "Liberty Leading the People", "Eugene Delacroix");
    public static final DeferredItem<MasterpiecePaintingItem> THE_LAST_SUPPER = reg("the_last_supper", "The Last Supper", "Leonardo da Vinci");

    /* 16 more masterpiece paintings */
    public static final DeferredItem<MasterpiecePaintingItem> THE_NIGHT_WATCH = reg("the_night_watch", "The Night Watch", "Rembrandt van Rijn");
    public static final DeferredItem<MasterpiecePaintingItem> GIRL_WITH_BALLOON = reg("girl_with_balloon", "The Bat-Woman", "Albert Joseph Penot");
    public static final DeferredItem<MasterpiecePaintingItem> WANDERER_ABOVE_FOG = reg("wanderer_above_fog", "Wanderer Above the Sea of Fog", "Caspar David Friedrich");
    public static final DeferredItem<MasterpiecePaintingItem> SUNFLOWERS = reg("sunflowers", "Sunflowers", "Vincent van Gogh");
    public static final DeferredItem<MasterpiecePaintingItem> WHISTLERS_MOTHER = reg("whistlers_mother", "Whistler's Mother", "James McNeill Whistler");
    public static final DeferredItem<MasterpiecePaintingItem> THE_THINKER = reg("the_thinker", "The Thinker", "Auguste Rodin");
    public static final DeferredItem<MasterpiecePaintingItem> GUERNICA = reg("guernica", "Guernica", "Pablo Picasso");
    public static final DeferredItem<MasterpiecePaintingItem> SELF_PORTRAIT_VANGOGH = reg("self_portrait_vangogh", "Self-Portrait", "Vincent van Gogh");
    public static final DeferredItem<MasterpiecePaintingItem> OLYMPIA = reg("olympia", "Olympia", "Edouard Manet");
    public static final DeferredItem<MasterpiecePaintingItem> THE_ARNOLFINI = reg("the_arnolfini", "The Arnolfini Portrait", "Jan van Eyck");
    public static final DeferredItem<MasterpiecePaintingItem> CAFE_TERRACE = reg("cafe_terrace", "Cafe Terrace at Night", "Vincent van Gogh");
    public static final DeferredItem<MasterpiecePaintingItem> IMPRESSION_SUNRISE = reg("impression_sunrise", "Impression, Sunrise", "Claude Monet");
    public static final DeferredItem<MasterpiecePaintingItem> DANCE_AT_MOULIN = reg("dance_at_moulin", "Dance at Le Moulin de la Galette", "Pierre-Auguste Renoir");
    public static final DeferredItem<MasterpiecePaintingItem> SLEEPING_GYPSY = reg("sleeping_gypsy", "The Sleeping Gypsy", "Henri Rousseau");
    public static final DeferredItem<MasterpiecePaintingItem> VITRUVIAN_MAN = reg("vitruvian_man", "Vitruvian Man", "Leonardo da Vinci");
    public static final DeferredItem<MasterpiecePaintingItem> GRANDE_ODALISQUE = reg("grande_odalisque", "La Grande Odalisque", "Jean-Auguste-Dominique Ingres");

    /* 32 more masterpiece paintings */
    public static final DeferredItem<MasterpiecePaintingItem> LAS_MENINAS = reg("las_meninas", "Las Meninas", "Diego Velazquez");
    public static final DeferredItem<MasterpiecePaintingItem> SCHOOL_OF_ATHENS = reg("school_of_athens", "The School of Athens", "Raphael");
    public static final DeferredItem<MasterpiecePaintingItem> GARDEN_OF_EARTHLY_DELIGHTS = reg("garden_of_earthly_delights", "The Garden of Earthly Delights", "Hieronymus Bosch");
    public static final DeferredItem<MasterpiecePaintingItem> THE_MILKMAID = reg("the_milkmaid", "The Milkmaid", "Johannes Vermeer");
    public static final DeferredItem<MasterpiecePaintingItem> NAPOLEON_CROSSING_ALPS = reg("napoleon_crossing_alps", "Napoleon Crossing the Alps", "Jacques-Louis David");
    public static final DeferredItem<MasterpiecePaintingItem> RAFT_OF_THE_MEDUSA = reg("raft_of_the_medusa", "The Raft of the Medusa", "Theodore Gericault");
    public static final DeferredItem<MasterpiecePaintingItem> SATURN_DEVOURING_SON = reg("saturn_devouring_son", "Saturn Devouring His Son", "Francisco Goya");
    public static final DeferredItem<MasterpiecePaintingItem> THE_HAY_WAIN = reg("the_hay_wain", "The Hay Wain", "John Constable");
    public static final DeferredItem<MasterpiecePaintingItem> RAIN_STEAM_SPEED = reg("rain_steam_speed", "Rain Steam and Speed", "J.M.W. Turner");
    public static final DeferredItem<MasterpiecePaintingItem> OPHELIA = reg("ophelia", "Ophelia", "John Everett Millais");
    public static final DeferredItem<MasterpiecePaintingItem> BAR_AT_FOLIES_BERGERE = reg("bar_at_folies_bergere", "A Bar at the Folies-Bergere", "Edouard Manet");
    public static final DeferredItem<MasterpiecePaintingItem> THE_CARD_PLAYERS = reg("the_card_players", "The Card Players", "Paul Cezanne");
    public static final DeferredItem<MasterpiecePaintingItem> THE_OLD_GUITARIST = reg("the_old_guitarist", "The Old Guitarist", "Pablo Picasso");
    public static final DeferredItem<MasterpiecePaintingItem> THE_DREAM_ROUSSEAU = reg("the_dream_rousseau", "The Dream", "Henri Rousseau");
    public static final DeferredItem<MasterpiecePaintingItem> TOWER_OF_BABEL = reg("tower_of_babel", "The Tower of Babel", "Pieter Bruegel the Elder");
    public static final DeferredItem<MasterpiecePaintingItem> CHRISTINAS_WORLD = reg("christinas_world", "Christina's World", "Andrew Wyeth");
    public static final DeferredItem<MasterpiecePaintingItem> THE_TWO_FRIDAS = reg("the_two_fridas", "The Two Fridas", "Frida Kahlo");
    public static final DeferredItem<MasterpiecePaintingItem> BROADWAY_BOOGIE_WOOGIE = reg("broadway_boogie_woogie", "Broadway Boogie Woogie", "Piet Mondrian");
    public static final DeferredItem<MasterpiecePaintingItem> COMPOSITION_VIII = reg("composition_viii", "Composition VIII", "Wassily Kandinsky");
    public static final DeferredItem<MasterpiecePaintingItem> DOGS_PLAYING_POKER = reg("dogs_playing_poker", "Dogs Playing Poker", "Cassius Marcellus Coolidge");
    public static final DeferredItem<MasterpiecePaintingItem> THIRD_OF_MAY = reg("third_of_may", "The Third of May 1808", "Francisco Goya");
    public static final DeferredItem<MasterpiecePaintingItem> LADY_OF_SHALOTT = reg("lady_of_shalott", "The Lady of Shalott", "John William Waterhouse");
    public static final DeferredItem<MasterpiecePaintingItem> GIRL_BEFORE_MIRROR = reg("girl_before_mirror", "Girl Before a Mirror", "Pablo Picasso");
    public static final DeferredItem<MasterpiecePaintingItem> LUNCHEON_BOATING_PARTY = reg("luncheon_boating_party", "Luncheon of the Boating Party", "Pierre-Auguste Renoir");
    public static final DeferredItem<MasterpiecePaintingItem> BIRTH_OF_THE_WORLD = reg("birth_of_the_world", "The Birth of the World", "Joan Miro");
    public static final DeferredItem<MasterpiecePaintingItem> A_BIGGER_SPLASH = reg("a_bigger_splash", "A Bigger Splash", "David Hockney");
    public static final DeferredItem<MasterpiecePaintingItem> CAMPBELL_SOUP_CANS = reg("campbell_soup_cans", "Campbell's Soup Cans", "Andy Warhol");
    public static final DeferredItem<MasterpiecePaintingItem> NUMBER_FIVE_1948 = reg("number_five_1948", "Fumee d'Ambre Gris", "John Singer Sargent");
    public static final DeferredItem<MasterpiecePaintingItem> ANATOMY_LESSON = reg("anatomy_lesson", "The Anatomy Lesson", "Rembrandt van Rijn");
    public static final DeferredItem<MasterpiecePaintingItem> THE_BLUE_BOY = reg("the_blue_boy", "The Blue Boy", "Thomas Gainsborough");
    public static final DeferredItem<MasterpiecePaintingItem> TREACHERY_OF_IMAGES = reg("treachery_of_images", "The Treachery of Images", "Rene Magritte");
    public static final DeferredItem<MasterpiecePaintingItem> LUNCHEON_ON_THE_GRASS = reg("luncheon_on_the_grass", "Le Dejeuner sur l'herbe", "Edouard Manet");

    public static final List<DeferredItem<MasterpiecePaintingItem>> ALL_PAINTINGS = List.of(
        MONA_LISA, STARRY_NIGHT, THE_GREAT_WAVE, GIRL_PEARL_EARRING,
        THE_SCREAM, PERSISTENCE_OF_MEMORY, BIRTH_OF_VENUS, AMERICAN_GOTHIC,
        THE_KISS, WATER_LILIES, CREATION_OF_ADAM, SUNDAY_GRANDE_JATTE,
        NIGHTHAWKS, SON_OF_MAN, LIBERTY_LEADING, THE_LAST_SUPPER,
        THE_NIGHT_WATCH, GIRL_WITH_BALLOON, WANDERER_ABOVE_FOG, SUNFLOWERS,
        WHISTLERS_MOTHER, THE_THINKER, GUERNICA, SELF_PORTRAIT_VANGOGH,
        OLYMPIA, THE_ARNOLFINI, CAFE_TERRACE, IMPRESSION_SUNRISE,
        DANCE_AT_MOULIN, SLEEPING_GYPSY, VITRUVIAN_MAN, GRANDE_ODALISQUE,
        LAS_MENINAS, SCHOOL_OF_ATHENS, GARDEN_OF_EARTHLY_DELIGHTS, THE_MILKMAID,
        NAPOLEON_CROSSING_ALPS, RAFT_OF_THE_MEDUSA, SATURN_DEVOURING_SON, THE_HAY_WAIN,
        RAIN_STEAM_SPEED, OPHELIA, BAR_AT_FOLIES_BERGERE, THE_CARD_PLAYERS,
        THE_OLD_GUITARIST, THE_DREAM_ROUSSEAU, TOWER_OF_BABEL, CHRISTINAS_WORLD,
        THE_TWO_FRIDAS, BROADWAY_BOOGIE_WOOGIE, COMPOSITION_VIII, DOGS_PLAYING_POKER,
        THIRD_OF_MAY, LADY_OF_SHALOTT, GIRL_BEFORE_MIRROR, LUNCHEON_BOATING_PARTY,
        BIRTH_OF_THE_WORLD, A_BIGGER_SPLASH, CAMPBELL_SOUP_CANS, NUMBER_FIVE_1948,
        ANATOMY_LESSON, THE_BLUE_BOY, TREACHERY_OF_IMAGES, LUNCHEON_ON_THE_GRASS
    );

    public static final Supplier<CreativeModeTab> MASTERPIECES_TAB = CREATIVE_MODE_TABS.register(
        "megamod_masterpieces_tab",
        () -> CreativeModeTab.builder()
            .title(Component.literal("MegaMod - Museum"))
            .icon(() -> new ItemStack(MONA_LISA.get()))
            .displayItems((parameters, output) -> {
                output.accept(com.ultra.megamod.feature.museum.MuseumRegistry.MUSEUM_BLOCK_ITEM.get());
                output.accept(com.ultra.megamod.feature.museum.MuseumRegistry.MUSEUM_DOOR_ITEM.get());
                output.accept(com.ultra.megamod.feature.museum.MuseumRegistry.MOB_NET_ITEM.get());
                output.accept(com.ultra.megamod.feature.museum.MuseumRegistry.CAPTURED_MOB_ITEM.get());
                for (DeferredItem<MasterpiecePaintingItem> painting : ALL_PAINTINGS) {
                    output.accept(painting.get());
                }
            })
            .build()
    );

    private static DeferredItem<MasterpiecePaintingItem> reg(String id, String title, String artist) {
        return ITEMS.registerItem(id, props -> new MasterpiecePaintingItem(id, title, artist, props.stacksTo(1)));
    }

    public static ItemStack getRandomPainting(RandomSource random) {
        int index = random.nextInt(ALL_PAINTINGS.size());
        return new ItemStack(ALL_PAINTINGS.get(index).get());
    }

    public static ItemStack getItemForVariant(String variantName) {
        for (DeferredItem<MasterpiecePaintingItem> painting : ALL_PAINTINGS) {
            if (painting.get().getVariantName().equals(variantName)) {
                return new ItemStack(painting.get());
            }
        }
        return ItemStack.EMPTY;
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
    }
}
