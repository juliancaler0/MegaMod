package com.ultra.megamod.lib.etf.features.property_reading;

import com.ultra.megamod.lib.etf.ETFApi;
import com.ultra.megamod.lib.etf.ETFException;
import com.ultra.megamod.lib.etf.features.ETFManager;
import com.ultra.megamod.lib.etf.features.ETFRenderContext;
import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperties;
import com.ultra.megamod.lib.etf.features.property_reading.properties.generic_properties.SimpleIntegerArrayProperty;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import com.ultra.megamod.lib.etf.features.texture_handlers.ETFDirectory;
import com.ultra.megamod.lib.etf.utils.ETFUtils2;
import com.ultra.megamod.lib.etf.utils.EntityBooleanLRU;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * {@link ETFApi.ETFVariantSuffixProvider} implementation driven by an OptiFine-format
 * random-entity {@code .properties} file.
 * <p>
 * Ported 1:1 from Entity_Texture_Features (traben).
 */
public class PropertiesRandomProvider implements ETFApi.ETFVariantSuffixProvider {

    protected final List<RandomPropertyRule> propertyRules;

    protected final EntityBooleanLRU entityCanUpdate = new EntityBooleanLRU(1000);

    protected final @NotNull String packname;

    protected BiConsumer<ETFEntityRenderState, @Nullable RandomPropertyRule> onMeetsRule =
            (entity, rule) -> { };

    private PropertiesRandomProvider(Identifier propertiesFileIdentifier, List<RandomPropertyRule> propertyRules) {
        this.propertyRules = propertyRules;
        this.packname = Minecraft.getInstance().getResourceManager().getResource(propertiesFileIdentifier)
                .map(Resource::sourcePackId)
                .orElse("vanilla");
    }

    @Nullable
    public static PropertiesRandomProvider of(Identifier initialPropertiesFileIdentifier, Identifier vanillaIdentifier, String... suffixKeyName) {
        Identifier propertiesFileIdentifier = ETFDirectory.getDirectoryVersionOf(initialPropertiesFileIdentifier);
        if (propertiesFileIdentifier == null) return null;

        try {
            Properties props = ETFUtils2.readAndReturnPropertiesElseNull(propertiesFileIdentifier);
            if (props == null) {
                ETFUtils2.logMessage("Ignoring properties file that was null @ " + propertiesFileIdentifier, false);
                return null;
            }
            if (vanillaIdentifier.getPath().endsWith(".png")) {
                ETFManager.getInstance().grabSpecialProperties(props, ETFRenderContext.getCurrentEntityState());
            }

            List<RandomPropertyRule> propertyRules = PropertiesRandomProvider.getAllValidPropertyObjects(props, propertiesFileIdentifier, suffixKeyName);
            if (propertyRules.isEmpty()) {
                ETFUtils2.logMessage("Ignoring properties file that failed to load any cases @ " + propertiesFileIdentifier, false);
                return null;
            }

            // assure default return always present
            if (!propertyRules.get(propertyRules.size() - 1).isAlwaysMet()) {
                propertyRules.add(RandomPropertyRule.DEFAULT_RETURN);
            }

            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            String properties = resourceManager.getResource(propertiesFileIdentifier).map(Resource::sourcePackId).orElse(null);
            String vanillaPack = resourceManager.getResource(vanillaIdentifier).map(Resource::sourcePackId).orElse(null);

            if (properties != null
                    && properties.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(properties, vanillaPack))) {
                return new PropertiesRandomProvider(propertiesFileIdentifier, propertyRules);
            }
        } catch (ETFException etf) {
            if (!propertiesFileIdentifier.toString().contains("optifine/cit/")) {
                ETFUtils2.logWarn("Ignoring properties file with problem: " + propertiesFileIdentifier + "\n" + etf, false);
            }
        } catch (Exception e) {
            ETFUtils2.logWarn("Ignoring properties file that caused unexpected Exception: " + propertiesFileIdentifier + "\n" + e, false);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    public static List<RandomPropertyRule> getAllValidPropertyObjects(Properties properties, Identifier propertiesFilePath, String... suffixToTest) throws ETFException {

        List<Integer> numbersList = getCaseNumbers(properties.stringPropertyNames());

        validateRuleNumbers(propertiesFilePath, numbersList);

        List<RandomPropertyRule> allRulesOfProperty = new ArrayList<>();
        for (Integer ruleNumber : numbersList) {
            Integer[] suffixesOfRule = getSuffixes(properties, ruleNumber, suffixToTest);

            if (suffixesOfRule != null && suffixesOfRule.length != 0) {
                allRulesOfProperty.add(new RandomPropertyRule(
                        propertiesFilePath.toString(),
                        ruleNumber,
                        suffixesOfRule,
                        getWeights(properties, ruleNumber),
                        getSeedOffset(properties, ruleNumber),
                        properties.getProperty("seedSource." + ruleNumber),
                        RandomProperties.getAllRegisteredRandomPropertiesOfIndex(properties, ruleNumber)
                ));
            } else {
                ETFUtils2.logWarn("property number \"" + ruleNumber + ". in file \"" + propertiesFilePath + ". failed to read.");
            }
        }
        return allRulesOfProperty;
    }

    private static int getSeedOffset(Properties props, int num) {
        String seedOffsetStr = props.getProperty("seedOffset." + num);
        if (seedOffsetStr != null) {
            try {
                return Integer.parseInt(seedOffsetStr);
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    private static void validateRuleNumbers(final Identifier propertiesFilePath, final List<Integer> numbersList) {
        if (numbersList.isEmpty()) {
            ETFUtils2.logWarn("Properties file [" + propertiesFilePath + "] contains no rules, this is invalid.", false);
            throw new ETFException("Properties file [" + propertiesFilePath + "] contains no rules, this is invalid.");
        }

        if (numbersList.get(0) < 1) {
            ETFUtils2.logWarn("Properties file [" + propertiesFilePath + "] contains rule numbers less than 1, this is invalid.", false);
            throw new ETFException("Properties file [" + propertiesFilePath + "] contains rule numbers less than 1, this is invalid.");
        }

        // send log message if skipping rule numbers — the gap->10 OptiFine legacy check is a no-op in 1.21.11
        int last = 0;
        for (Integer i : numbersList) {
            if (i >= last + 10) {
                last = -1;
                break;
            }
            last = i;
        }
        // the "skipped numbers > 10" warning is disabled in upstream ETF for MC >= 1.21.11; match that behaviour
    }

    @NotNull
    private static List<Integer> getCaseNumbers(final Set<String> propIds) {
        Set<Integer> foundRuleNumbers = new HashSet<>();

        for (String str : propIds) {
            String[] split = str.split("\\.");
            if (split.length >= 2 && !split[1].isBlank()) {
                String possibleRuleNumber = split[1].replaceAll("\\D", "");
                if (!possibleRuleNumber.isBlank()) {
                    try {
                        foundRuleNumbers.add(Integer.parseInt(possibleRuleNumber));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return foundRuleNumbers.stream().sorted().toList();
    }

    @Nullable
    private static Integer[] getSuffixes(Properties props, int num, String... suffixToTest) throws ETFException {
        var suffixes = SimpleIntegerArrayProperty.getGenericIntegerSplitWithRanges(props, num, suffixToTest);
        if (suffixes != null) {
            for (Integer suffix : suffixes) {
                if (suffix < 1) {
                    throw new ETFException("Invalid suffix: [" + suffix + "] in " + Arrays.toString(suffixes));
                }
            }
        }
        return suffixes;
    }

    @Nullable
    private static Integer[] getWeights(Properties props, int num) {
        return SimpleIntegerArrayProperty.getGenericIntegerSplitWithRanges(props, num, "weights");
    }

    @SuppressWarnings("unused")
    public void setOnMeetsRuleHook(BiConsumer<ETFEntityRenderState, RandomPropertyRule> onMeetsRule) {
        if (onMeetsRule != null)
            this.onMeetsRule = onMeetsRule;
    }

    public @NotNull String getPackName() {
        return packname;
    }

    public boolean isHigherPackThan(@Nullable String packNameOther) {
        return packname.equals(ETFUtils2.returnNameOfHighestPackFromTheseTwo(packname, packNameOther));
    }

    @Override
    public boolean entityCanUpdate(UUID uuid) {
        return entityCanUpdate.getBoolean(uuid);
    }

    @SuppressWarnings("unused")
    @Override
    public IntOpenHashSet getAllSuffixes() {
        IntOpenHashSet allSuffixes = new IntOpenHashSet();
        for (RandomPropertyRule rule : propertyRules) {
            allSuffixes.addAll(rule.getSuffixSet());
        }
        return allSuffixes;
    }

    @Override
    public int size() {
        return propertyRules.size();
    }

    @Override
    public int getSuffixForETFEntity(ETFEntityRenderState entityToBeTested) {
        if (entityToBeTested == null) return 0;
        UUID id = entityToBeTested.uuid();
        boolean entityHasBeenTestedBefore = entityCanUpdate.containsKey(id);

        int result = testEntityAgainstRules(entityToBeTested, entityHasBeenTestedBefore);

        if (!entityHasBeenTestedBefore) {
            if (entityCanUpdate.getBoolean(id)) {
                for (RandomPropertyRule rule : propertyRules) {
                    rule.cacheEntityInitialResultsOfNonUpdatingProperties(entityToBeTested);
                }
            }
        }
        if (result > 0) return result;

        onMeetsRule.accept(entityToBeTested, null);
        return 0;
    }

    private int testEntityAgainstRules(final ETFEntityRenderState entityToBeTested, boolean isUpdate) {
        for (RandomPropertyRule rule : propertyRules) {
            if (rule.doesEntityMeetConditionsOfThisCase(entityToBeTested, isUpdate, entityCanUpdate)) {
                onMeetsRule.accept(entityToBeTested, rule);
                return rule.getVariantSuffixFromThisCase(entityToBeTested);
            }
        }
        return 0;
    }
}
