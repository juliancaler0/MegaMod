package com.ultra.megamod.lib.accessories.impl.option;

import com.google.common.collect.Streams;
import com.ultra.megamod.lib.accessories.AccessoriesInternals;
import com.ultra.megamod.lib.accessories.utils.InstanceEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.Endec;
import com.ultra.megamod.lib.accessories.endec.adapter.SerializationContext;
import com.ultra.megamod.lib.accessories.endec.adapter.format.edm.EdmElement;
import com.ultra.megamod.lib.accessories.endec.adapter.format.edm.EdmEndec;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierDecodable;
import com.ultra.megamod.lib.accessories.endec.adapter.util.MapCarrierEncodable;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccessoriesPlayerOptionsHolder implements InstanceEndec, PlayerOptionsAccess {

    public static final Endec<@Nullable AccessoriesPlayerOptionsHolder> ENDEC = new Endec<>() {
        @Override
        public com.mojang.serialization.Codec<@Nullable AccessoriesPlayerOptionsHolder> codec() {
            return com.mojang.serialization.Codec.STRING.xmap(
                s -> (AccessoriesPlayerOptionsHolder) null,
                holder -> ""
            );
        }
    };

    private Map<PlayerOption<?>, Object> optionToValue = new LinkedHashMap<>();

    public AccessoriesPlayerOptionsHolder() {}

    public static AccessoriesPlayerOptionsHolder getOptions(Player player) {
        return AccessoriesInternals.INSTANCE.getPlayerOptions(player);
    }

    public boolean hasData(PlayerOption<?> option) {
        return optionToValue.containsKey(option);
    }

    public <T> Optional<T> getData(PlayerOption<T> option) {
        return (this.optionToValue.containsKey(option))
            ? Optional.of((T) this.optionToValue.get(option))
            : Optional.empty();
    }

    public <T> void setData(PlayerOption<T> option, T data) {
        Objects.requireNonNull(option, "Unable to set data as the given PlayerOption instance is null!");

        this.optionToValue.put(option, data);
    }

    @Override
    public void encode(MapCarrierEncodable carrier, SerializationContext ctx) {
        optionToValue.forEach((playerOption, object) -> playerOption.writeToCarrierCasted(carrier, object));
    }

    @Override
    public void decode(MapCarrierDecodable carrier, SerializationContext ctx) {
        this.optionToValue.clear();

        for (PlayerOption<?> option : PlayerOption.getAllOptions()) {
            if (!carrier.has(option.keyEndec())) continue;

            this.optionToValue.put(option, carrier.get(option.keyEndec()));
        }
    }

    public boolean isDefaultedValues() {
        return this.optionToValue.keySet().stream().allMatch(option -> Objects.equals(getDefaultedData(option), option.defaultValue()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof AccessoriesPlayerOptionsHolder otherHolder)) return false;

        var allOptions = Streams.concat(this.optionToValue.keySet().stream(), otherHolder.optionToValue.keySet().stream())
            .collect(Collectors.toSet());

        for (var playerOption : allOptions) {
            if (!Objects.equals(otherHolder.getDefaultedData(playerOption), getDefaultedData(playerOption))) {
                return false;
            }
        }

        return true;
    }

    public static AccessoriesPlayerOptionsHolder createOrCopy(@Nullable AccessoriesPlayerOptionsHolder holder) {
        if (holder == null) return new AccessoriesPlayerOptionsHolder();

        return holder.copy();
    }

    public AccessoriesPlayerOptionsHolder copy() {
        var holder = new AccessoriesPlayerOptionsHolder();

        holder.optionToValue.putAll(this.optionToValue);

        return holder;
    }
}
