package shiroroku.theaurorian.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import shiroroku.theaurorian.DataGen.DataGenItemsTags;
import shiroroku.theaurorian.Portal.AurorianPortalShape;

import java.util.Optional;

public class AurorianPortalFrame extends Block {

    public AurorianPortalFrame(Properties pProperties) {
        super(pProperties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        ItemStack usedItem = pPlayer.getItemInHand(pHand);
        if (usedItem.is(DataGenItemsTags.PORTAL_LIGHTERS)) {
            BlockPos placePos = pPos.relative(pHit.getDirection());
            Optional<AurorianPortalShape> optional = AurorianPortalShape.findEmptyPortalShape(pLevel, placePos, Direction.Axis.X);
            if (optional.isPresent()) {
                pLevel.playSound(pPlayer, pPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, pLevel.getRandom().nextFloat() * 0.4F + 0.8F);
                //usedItem.hurtAndBreak(1, pPlayer, (player) -> player.broadcastBreakEvent(pHand));
                optional.get().createPortalBlocks();
                return InteractionResult.sidedSuccess(pLevel.isClientSide());
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
