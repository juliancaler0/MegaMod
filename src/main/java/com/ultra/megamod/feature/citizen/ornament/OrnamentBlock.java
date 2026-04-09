package com.ultra.megamod.feature.citizen.ornament;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic ornament block that supports retexturing via the Architects Cutter.
 * Each instance is parameterized by an OrnamentBlockType which determines how many
 * texture components it has and its visual identity.
 *
 * The block entity (MateriallyTexturedBlockEntity) stores the actual texture data.
 * When placed from an item that carries OrnamentData in its tag, the block entity
 * is initialized with that data.
 */
public class OrnamentBlock extends BaseEntityBlock implements IMateriallyTexturedBlock {

    public static final MapCodec<OrnamentBlock> CODEC = OrnamentBlock.simpleCodec(OrnamentBlock::new);

    private final OrnamentBlockType ornamentType;
    private List<IMateriallyTexturedBlockComponent> components;

    /**
     * Constructor used by the registration system. The ornament type is set afterwards via {@link #setOrnamentType}.
     */
    public OrnamentBlock(BlockBehaviour.Properties props) {
        super(props);
        this.ornamentType = null;
    }

    /**
     * Full constructor with type.
     */
    public OrnamentBlock(OrnamentBlockType type, BlockBehaviour.Properties props) {
        super(props);
        this.ornamentType = type;
        buildComponents();
    }

    /**
     * Returns the ornament type, or null if not set (shouldn't happen in practice).
     */
    @Nullable
    public OrnamentBlockType getOrnamentType() {
        return ornamentType;
    }

    @Override
    protected MapCodec<? extends OrnamentBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Full block for now — subtypes can override for slabs, fences, etc.
        return Block.box(0, 0, 0, 16, 16, 16);
    }

    // ==================== Block Entity ====================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriallyTexturedBlockEntity(pos, state);
    }

    /**
     * When placed, transfer the OrnamentData from the item to the block entity.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MateriallyTexturedBlockEntity texturedBE) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    CompoundTag tag = customData.copyTag();
                    if (tag.contains("OrnamentData")) {
                        MaterialTextureData data = MaterialTextureData.fromNbt(tag.getCompoundOrEmpty("OrnamentData"));
                        texturedBE.setTextureData(data);
                    }
                }
            }
        }
    }

    /**
     * When broken, preserve the ornament data on the dropped item.
     */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MateriallyTexturedBlockEntity texturedBE && !texturedBE.getTextureData().isEmpty()) {
            CompoundTag ornamentNbt = texturedBE.getTextureData().toNbt();
            for (ItemStack drop : drops) {
                if (drop.getItem() == this.asItem()) {
                    CustomData customData = drop.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();
                    tag.put("OrnamentData", ornamentNbt);
                    drop.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }
        }
        return drops;
    }

    // ==================== IMateriallyTexturedBlock ====================

    @Override
    public List<IMateriallyTexturedBlockComponent> getComponents() {
        if (components == null) {
            buildComponents();
        }
        return components;
    }

    private void buildComponents() {
        components = new ArrayList<>();
        if (ornamentType == null) {
            // Fallback: 1 component
            components.add(new SimpleRetexturableComponent(
                    Identifier.fromNamespaceAndPath("megamod", "ornament_main"),
                    Blocks.OAK_PLANKS,
                    false
            ));
            return;
        }

        int count = ornamentType.getComponentCount();
        String typeId = ornamentType.getId();

        // Generate named components based on type
        switch (count) {
            case 1:
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_main"),
                        Blocks.OAK_PLANKS,
                        false
                ));
                break;
            case 2:
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_frame"),
                        Blocks.OAK_PLANKS,
                        false
                ));
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_panel"),
                        Blocks.OAK_LOG,
                        false
                ));
                break;
            case 3:
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_roof"),
                        Blocks.OAK_PLANKS,
                        false
                ));
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_support"),
                        Blocks.OAK_LOG,
                        false
                ));
                components.add(new SimpleRetexturableComponent(
                        Identifier.fromNamespaceAndPath("megamod", typeId + "_cover"),
                        Blocks.COBBLESTONE,
                        true
                ));
                break;
            default:
                for (int i = 0; i < count; i++) {
                    components.add(new SimpleRetexturableComponent(
                            Identifier.fromNamespaceAndPath("megamod", typeId + "_component_" + i),
                            Blocks.OAK_PLANKS,
                            i > 0
                    ));
                }
                break;
        }
    }
}
