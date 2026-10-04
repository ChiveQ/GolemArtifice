package dev.chiveq.golemartifice.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HumanoidModel.class)
public interface HumanoidModelAccessor {

    @Accessor("leftArm")
    ModelPart leftArm();

    @Accessor("rightArm")
    ModelPart rightArm();

}
