package com.ultra.megamod.feature.ambientsounds.block;

import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.ultra.megamod.feature.ambientsounds.engine.AmbientEngineLoadException;
import com.ultra.megamod.feature.ambientsounds.util.SimpleTupleList;

public abstract class AmbientBlock {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("megamod");

    public static AmbientBlock parse(String data) throws AmbientEngineLoadException {
        if (data.contains("[")) {
            if (!data.endsWith("]"))
                throw new AmbientEngineLoadException("Cannot parse block entry " + data);
            String[] parts = data.split("\\[");
            if (parts.length > 2)
                throw new AmbientEngineLoadException("Cannot parse block entry " + data);

            AmbientBlock block = parseFirst(parts[0]);
            String[] properties = parts[1].substring(0, parts[1].length() - 1).split(",");

            SimpleTupleList<String, String> found = new SimpleTupleList<>();
            for (int i = 0; i < properties.length; i++) {
                String[] property = properties[i].split("=");
                if (property.length != 2) {
                    LOGGER.error("Found invalid property condition '{}'. It will be ignored. {}", properties[i], data);
                    continue;
                }

                found.add(property[0], property[1]);
            }
            return new AmbientBlockProperty(block, found);
        }
        return parseFirst(data);
    }

    private static AmbientBlock parseFirst(String data) {
        if (data.startsWith("#"))
            return new AmbientBlockTag(TagKey.create(Registries.BLOCK, Identifier.parse(data.substring(1))));
        return new AmbientBlockBlock(Identifier.parse(data));
    }

    public abstract boolean is(BlockState state);

    public static class AmbientBlockTag extends AmbientBlock {

        public final TagKey<Block> tag;

        public AmbientBlockTag(TagKey<Block> tag) {
            this.tag = tag;
        }

        @Override
        public boolean is(BlockState state) {
            return state.is(tag);
        }

    }

    public static class AmbientBlockBlock extends AmbientBlock {

        public final Identifier block;

        public AmbientBlockBlock(Identifier block) {
            this.block = block;
        }

        @Override
        public boolean is(BlockState state) {
            return state.getBlockHolder().is(block);
        }

    }

    public static class AmbientBlockProperty extends AmbientBlock {

        public final AmbientBlock block;
        public final SimpleTupleList<String, String> properties;

        public AmbientBlockProperty(AmbientBlock block, SimpleTupleList<String, String> properties) {
            this.block = block;
            this.properties = properties;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        public boolean is(BlockState state) {
            if (!block.is(state))
                return false;

            for (int i = 0; i < properties.size(); i++) {
                Property<? extends Comparable> property = state.getBlock().getStateDefinition().getProperty(properties.get(i).key());
                if (property == null)
                    return false;

                Optional value = property.getValue(properties.get(i).value());
                if (!value.isPresent() || !state.getValue(property).equals(value.get()))
                    return false;
            }

            return true;
        }

    }

}
