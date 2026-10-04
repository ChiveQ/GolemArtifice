package dev.chiveq.golemartifice.mixin;

import dev.chiveq.golemartifice.init.GAConfig;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import io.redspace.irons_artifice.entity.IGunslingerMob;
import io.redspace.irons_artifice.gun.ShotProfile;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 处理削弱
 * */
@Mixin(HumanoidGolemEntity.class)
public class HumanoidGolemEntityMixin implements IGunslingerMob {

    @Override
    public void customizeMobShot(@NotNull Mob mob, @NotNull ShotProfile shotProfile) {
        if(mob instanceof HumanoidGolemEntity golem){
            boolean hostile = golem.isHostile() && GAConfig.applyMobNerfsToHostileGolems;
            boolean player = !golem.isHostile() && GAConfig.applyMobNerfsToPlayerGolems;
            if(hostile || player) IGunslingerMob.applyDefaultMobNerfs(mob, shotProfile);
        }
    }

}
