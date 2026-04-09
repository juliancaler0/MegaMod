package shiroroku.theaurorian.Entities.DungeonKeeper;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class DungeonKeeperModel extends SkeletonModel<AbstractSkeleton> {
    public DungeonKeeperModel(ModelPart pRoot) {
        super(pRoot);
    }

    @Override
    public void setupAnim(AbstractSkeleton pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

    }
}
