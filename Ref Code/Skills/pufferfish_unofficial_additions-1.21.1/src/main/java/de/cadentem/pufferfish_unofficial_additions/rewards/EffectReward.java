package de.cadentem.pufferfish_unofficial_additions.rewards;

import de.cadentem.pufferfish_unofficial_additions.PUA;
import de.cadentem.pufferfish_unofficial_additions.misc.ModificationHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EffectReward implements Reward {
    public static final ResourceLocation ID = PUA.location("effect");
    public static final Map<UUID, List<Data>> DATA = new HashMap<>();

    private static final DurationModification EMPTY_DURATION_MODIFICATION = new DurationModification(Operation.NONE, 0);

    private final Holder<MobEffect> effect;
    private final Type type;
    private final DurationModification durationModification;
    private final int amplifier;

    private EffectReward(final Holder<MobEffect> effect, final Type type, final DurationModification durationModification, final int amplifier) {
        this.effect = effect;
        this.type = type;
        this.durationModification = durationModification;
        this.amplifier = amplifier;
    }

    public static void register() {
        SkillsAPI.registerReward(ID, EffectReward::parse);
    }

    public static boolean isImmune(final UUID uuid, final Holder<MobEffect> effect, final int amplifier) {
        return getData(uuid).stream().anyMatch(data -> data.type == Type.IMMUNE && data.effect == effect && amplifier <= data.amplifier);
    }

    private static Result<EffectReward, Problem> parse(final RewardConfigContext context) {
        return context.getData().andThen(JsonElement::getAsObject).andThen(EffectReward::parse);
    }

    private static Result<EffectReward, Problem> parse(final JsonObject rootObject) {
        List<Problem> problems = new ArrayList<>();

        Optional<HolderSet<MobEffect>> effect = rootObject.get("effect").andThen(BuiltinJson::parseEffectOrEffectTag).ifFailure(problems::add).getSuccess();
        Optional<String> typeRaw = rootObject.getString("type").ifFailure(problems::add).getSuccess();
        Optional<Integer> amplifierOptional = rootObject.getInt("amplifier").ifFailure(problems::add).getSuccess();

        if (typeRaw.isPresent()) {
            Type type = Type.get(typeRaw.get());
            DurationModification durationModification = EMPTY_DURATION_MODIFICATION;

            if (type == Type.MODIFY && rootObject.getJson().has("duration_modification")) {
                Optional<String> durationModificationRaw = rootObject.getString("duration_modification").ifFailure(problems::add).getSuccess();

                if (durationModificationRaw.isPresent()) {
                    String data = durationModificationRaw.get();

                    if (!data.isBlank()) {
                        durationModification = new DurationModification(Operation.get(data.substring(0, 1)), Double.parseDouble(data.substring(1)));
                    }
                }
            }

            if (amplifierOptional.isPresent()) {
                int amplifier = amplifierOptional.get();

                if ((amplifier < 0 || amplifier > 255) && (type == Type.GRANT || type == Type.IMMUNE)) {
                    problems.add(Problem.message("The amplifier has to be between 0 and 255"));
                }

                if (problems.isEmpty()) {
                    HolderSet<MobEffect> set = effect.orElseThrow();

                    if (set.size() > 0) {
                        // TODO :: support multiple entries?
                        return Result.success(new EffectReward(set.get(0), type, durationModification, amplifier));
                    }
                }
            }
        }

        return Result.failure(Problem.combine(problems));
    }

    public static boolean shouldRemove(final UUID uuid, final Holder<MobEffect> effect, final int amplifier) {
        return getData(uuid).stream().anyMatch(data -> data.type == Type.GRANT && data.effect == effect && data.amplifier == amplifier);
    }

    public static void applyEffects(final ServerPlayer player) {
        getData(player.getUUID()).stream().filter(data -> data.type == Type.GRANT).forEach(data -> {
            MobEffectInstance instance = player.getEffect(data.effect);

            if (instance != null && instance.getAmplifier() > data.amplifier && instance.isInfiniteDuration()) {
                return;
            }

            addEffect(player, data);
        });
    }

    public static @Nullable MobEffectInstance modifyEffect(final ServerPlayer player, final MobEffectInstance instance) {
        if (!((ModificationHandler) instance).pufferfish_unofficial_additions$wasModified()) {
            Holder<MobEffect> effect = instance.getEffect();

            int modifiedAmplifier = EffectReward.getModifiedAmplifier(player.getUUID(), effect, instance.getAmplifier());
            int modifiedDuration = instance.getDuration();

            if (!instance.isInfiniteDuration()) {
                // Infinite duration cannot be properly modified
                modifiedDuration = EffectReward.getModifiedDuration(player.getUUID(), effect, instance.getDuration());
            }

            if (modifiedAmplifier < 0 || (!instance.isInfiniteDuration() && modifiedDuration <= 0)) {
                // If the subtraction lands on a duration of -1 we don't count that as infinite
                return null;
            }

            if (instance.getDuration() != modifiedDuration || instance.getAmplifier() != modifiedAmplifier) {
                MobEffectInstance modifiedInstance = new MobEffectInstance(effect, modifiedDuration, modifiedAmplifier, instance.isAmbient(), instance.isVisible(), instance.showIcon());
                ((ModificationHandler) modifiedInstance).pufferfish_unofficial_additions$setModified(true);
                return modifiedInstance;
            }
        }

        return instance;
    }

    private static int getModifiedDuration(final UUID uuid, final Holder<MobEffect> effect, int duration) {
        List<Data> modifications = getData(uuid).stream().filter(data -> data.type == Type.MODIFY && data.effect == effect).toList();

        for (Data data : modifications) {
            DurationModification modification = data.durationModification;

            if (modification == EMPTY_DURATION_MODIFICATION) {
                continue;
            }

            duration = (int) switch (modification.operation) {
                case ADD -> duration + modification.amount;
                case SUBTRACT -> duration - modification.amount;
                case MULTIPLY -> duration * modification.amount;
                case DIVIDE -> modification.amount != 0 ? duration / modification.amount : duration;
                default -> duration;
            };
        }

        return Math.max(0, duration);
    }

    private static int getModifiedAmplifier(final UUID uuid, final Holder<MobEffect> effect, int amplifier) {
        List<Data> modifications = getData(uuid).stream().filter(data -> data.type == Type.MODIFY && data.effect == effect).toList();

        for (Data data : modifications) {
            amplifier += data.amplifier;
        }

        return amplifier;
    }

    public static void clearData(final UUID uuid) {
        DATA.remove(uuid);
    }

    @Override
    public void update(final RewardUpdateContext context) {
        ServerPlayer player = context.getPlayer();
        int count = context.getCount();

        List<Data> data = getData(player.getUUID());

        if (count == 0) {
            data.removeIf(this::matches);

            if (type == Type.GRANT) {
                removeEffect(player);
            }
        } else {
            int active = count;

            for (int i = 0; i < data.size(); i++) {
                Data entry = data.get(i);

                if (matches(entry)) {
                    if (active == 0) {
                        data.remove(i);
                        i--;
                    } else {
                        active--;
                    }
                }
            }

            for (int i = 0; i < active; i++) {
                data.add(new Data(effect, type, durationModification, amplifier));
            }

            if (type == Type.GRANT) {
                addEffect(player, new Data(effect, type, durationModification, amplifier));
            }
        }
    }

    @Override
    public void dispose(final RewardDisposeContext context) {
        context.getServer().getPlayerList().getPlayers().forEach(player -> {
            getData(player.getUUID()).removeIf(this::matches);

            if (type == Type.GRANT) {
                removeEffect(player);
            }
        });
    }

    private static List<Data> getData(final UUID uuid) {
        return DATA.computeIfAbsent(uuid, key -> new ArrayList<>());
    }

    private void removeEffect(final ServerPlayer player) {
        MobEffectInstance instance = player.getEffect(effect);

        if (instance != null && instance.isInfiniteDuration() && matches(instance)) {
            player.removeEffect(effect);
        }
    }

    private static void addEffect(final ServerPlayer player, final Data data) {
        player.addEffect(new MobEffectInstance(data.effect, MobEffectInstance.INFINITE_DURATION, data.amplifier, false, /* No particles */ false));
    }

    private boolean matches(final Data data) {
        return data.effect == effect && data.type == type && data.durationModification == durationModification && data.amplifier == amplifier;
    }

    private boolean matches(@NotNull final MobEffectInstance instance) {
        return instance.getEffect() == effect && instance.getAmplifier() == amplifier;
    }

    public enum Type {
        GRANT,
        MODIFY,
        IMMUNE;

        public static Type get(final String type) {
            return switch (type.toLowerCase()) {
                case "grant" -> GRANT;
                case "immune" -> IMMUNE;
                case "modify" -> MODIFY;
                default -> throw new IllegalArgumentException("Supplied invalid type: " + type);
            };
        }
    }

    public enum Operation {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("x"),
        DIVIDE("/"),
        NONE("");

        private final String key;

        Operation(final String key) {
            this.key = key;
        }

        public static Operation get(final String key) {
            for (Operation operation : Operation.values()) {
                if (operation.key.equals(key)) {
                    return operation;
                }
            }

            return NONE;
        }
    }

    public record Data(Holder<MobEffect> effect, Type type, DurationModification durationModification, int amplifier) { /* Nothing to do */ }
    public record DurationModification(Operation operation, double amount) { /* Nothing to do */ }
}
