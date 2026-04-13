package com.ultra.megamod.lib.spellengine.internals.casting;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.internals.Ammo;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class SpellCast {
    public record Attempt(Result result,
                          @Nullable MissingItemInfo missingItem,
                          @Nullable OnCooldownInfo onCooldown) {
        public enum Result { SUCCESS, MISSING_ITEM, ON_COOLDOWN, NONE }
        public record MissingItemInfo(Ammo.Searched item) { }
        public record OnCooldownInfo() { }

        public static Attempt none() {
            return new Attempt(Result.NONE, null, null);
        }

        public static Attempt success() {
            return new Attempt(Result.SUCCESS, null, null);
        }

        public static Attempt failMissingItem(MissingItemInfo missingItem) {
            return new Attempt(Result.MISSING_ITEM, missingItem, null);
        }

        public static Attempt failOnCooldown(OnCooldownInfo onCooldown) {
            return new Attempt(Result.ON_COOLDOWN, null, onCooldown);
        }

        public boolean isSuccess() {
            return result == Result.SUCCESS;
        }
        public boolean isFail() {
            return result != Result.SUCCESS && result != Result.NONE;
        }
    }

    public record Duration(float speed, int length) {
        public static final Duration EMPTY = new Duration(0, 0);
    }
    public static class TickHolder {
        public ArrayList<Float> ticks = new ArrayList<>();
    }
    public record Process(Holder<Spell> spell, Item item, float speed, int length, long startedAt, TickHolder tickHolder) {
        public Process(LivingEntity caster, Holder<Spell> spell, Item item, float speed, int length, long startedAt) {
            this(spell, item, speed, length, startedAt, new TickHolder());
            if (SpellHelper.isChanneled(spell.value())) {
                var channelCount = SpellHelper.channelTicks(caster, spell);
                var interval = channelInterval(caster);
                var offset = -interval * 0.5F;
                for (int i = 1; i <= channelCount; i++) {
                    tickHolder.ticks.add((interval * i) + offset);
                }
            }
        }

        public int spellCastTicksSoFar(long worldTime) {
            // At least zero
            // The difference must fit into an integer
            return (int)Math.max(worldTime - startedAt, 0);
        }

        public Progress progress(int castTicks) {
            if (length <= 0) {
                return new Progress(1F, this);
            }
            float ratio = Math.min(((float)castTicks) / length(), 1F);
            return new Progress(ratio, this);
        }

        public Progress progress(long worldTime) {
            int castTicks = spellCastTicksSoFar(worldTime);
            return progress(castTicks);
        }

        public Identifier id() {
            return spell.getKey().identifier();
        }

        public SyncFormat sync() {
            return new SyncFormat(id().toString(), speed, length);
        }

        public String fastSyncJSON() {
            return "{\"i\":" + '"' + id().toString() + '"'  + ",\"s\":" + speed + ",\"l\":" + length + "}";
        }

        @Nullable
        public static Process fromSync(LivingEntity caster, Level world, SyncFormat sync, Item item, long startedAt) {
            var spellId = sync.i();
            if (spellId.isEmpty()) {
                return null;
            }
            var id = Identifier.parse(spellId);
            var spellEntry = SpellRegistry.from(world).get(id).orElse(null);
            return new Process(caster, spellEntry, item, sync.s(), sync.l(), startedAt);
        }

        /**
         * Represents the spell cast process in a format that can be sent to the client.
         * Short field names are used to improve JSON performance.
         */
        public record SyncFormat(String i, float s, int l) { }

        public float channelInterval(LivingEntity caster) {
            var ticks = SpellHelper.channelTicks(caster, spell());
            if (ticks > 0) {
                return length / (float)ticks;
            } else {
                return length;
            }
        }

        public boolean isDue(long time) {
            var castTicks = spellCastTicksSoFar(time);
            if (tickHolder.ticks.isEmpty()) {
                return false;
            } else {
                return castTicks >= tickHolder.ticks.getFirst();
            }
        }
        public void markDue() {
            if (!tickHolder.ticks.isEmpty()) {
                tickHolder.ticks.removeFirst();
            }
        }
    }
    public record Progress(float ratio, Process process) { }

    public enum Mode {
        INSTANT,
        CHARGE,
        CHANNEL,
        PASSIVE,
        ITEM_USE; // This one is never produced by mapping, only manually from SpellHotbar logic
        public static Mode from(Spell spell) {
            if (spell.active != null) {
                if (spell.active.cast.duration <= 0) {
                    return INSTANT;
                }
                return SpellHelper.isChanneled(spell) ? CHANNEL : CHARGE;
            } else {
                return PASSIVE;
            }
        }
    }

    public enum Action {
        CHANNEL,
        RELEASE,
        TRIGGER
    }

    public enum Animation {
        CASTING, RELEASE, MISC
    }
}
