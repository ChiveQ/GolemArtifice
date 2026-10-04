package dev.chiveq.golemartifice.mixin;

import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemModel;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidGolemModel.class)
public class HumanoidGolemModelMixin {

//    @Shadow @Final public ModelPart leftArm;
//    @Shadow @Final public ModelPart rightArm;
//    @Shadow @Final public ModelPart leftSleeve;
//    @Shadow @Final public ModelPart rightSleeve;

    @SuppressWarnings("unchecked")
    @Inject(method = "setupAnim*", at = @At("TAIL"))
    private void irons_artifice$applyGunPose(HumanoidGolemEntity entity,
                                             float limbSwing, float limbSwingAmount,
                                             float ageInTicks, float netHeadYaw, float headPitch,
                                             CallbackInfo ci) {
        // 只在傀儡真的持枪时才动、避免干扰其他武器。
        boolean holdingGun = entity.getMainHandItem().getItem() instanceof GunItem
                || entity.getOffhandItem().getItem() instanceof GunItem;
        if (!holdingGun) {
            return;
        }

        GunArmPoses.applyHeldGunPoses((HumanoidModel<HumanoidGolemEntity>) (Object) this, entity);

        // 袖子在 super.setupAnim 和傀儡自己的 setupAnim 里已被 copy 过。
        // 我们刚才改动了手臂、需要重新同步一次、否则袖子会留在改动前的位置。
//        this.leftSleeve.copyFrom(this.leftArm);
//        this.rightSleeve.copyFrom(this.rightArm);
    }

}
