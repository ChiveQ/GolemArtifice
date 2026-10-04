package dev.chiveq.golemartifice.mixin;

import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemModel;
import io.redspace.irons_artifice.client.gun.GunArmPoses;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.client.model.HumanoidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidGolemModel.class)
public abstract class HumanoidGolemModelMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "setupAnim(Ldev/xkmc/modulargolems/content/entity/humanoid/HumanoidGolemEntity;FFFFF)V", at = @At("TAIL"))
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
        ((PlayerModelAccessor)this).leftSleeve().copyFrom(((HumanoidModelAccessor)this).leftArm());
        ((PlayerModelAccessor)this).rightSleeve().copyFrom(((HumanoidModelAccessor)this).rightArm());
//        this.leftSleeve.copyFrom(this.leftArm);
//        this.rightSleeve.copyFrom(this.rightArm);
    }

}
