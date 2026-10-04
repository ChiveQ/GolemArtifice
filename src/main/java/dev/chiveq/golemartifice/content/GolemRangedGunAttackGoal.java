package dev.chiveq.golemartifice.content;

import dev.chiveq.golemartifice.init.GAConfig;
import dev.chiveq.golemartifice.mixin.GunplayManagerMixin;
import dev.chiveq.golemartifice.util.GolemAmmoUtil;
import dev.xkmc.mob_weapon_api.api.goals.IMeleeGoal;
import dev.xkmc.mob_weapon_api.api.goals.IRangedWeaponGoal;
import dev.xkmc.mob_weapon_api.api.goals.IWeaponGoal;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import io.redspace.irons_artifice.entity.ai.RangedGunAttackGoal;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 参考傀儡本体compat.musket实现
 * */
public class GolemRangedGunAttackGoal extends RangedGunAttackGoal<HumanoidGolemEntity>
        implements IRangedWeaponGoal<HumanoidGolemEntity> {

    /**
     * 近战回退接口
     * */
    private final IMeleeGoal meleeGoal;
    private int meleeCooldown = 0;

    public GolemRangedGunAttackGoal(HumanoidGolemEntity mob,IMeleeGoal melee,float range) {
        super(mob,range,
                10,40,
                30,60);
        this.meleeGoal = melee;
    }

    // IWeaponGoal

    @Override
    public double range(ItemStack itemStack) {
        return bands.maxRange();
    }

    /**
     * 持枪而有弹则可用
     * */
    @Override
    public boolean mayActivate(ItemStack stack){
        if(stack.getItem() instanceof GunItem){
            return hasAmmo();
        }
        return false;
    }

    @Override
    public boolean isAvailable(ItemStack stack) {
        return mayActivate(stack);
    }

    @Override
    public boolean shouldUseForMelee(ItemStack other) {
        return true;
    }

    // 生命周期
    @Override
    public void start(){
        super.start();
        meleeCooldown = 0;
    }

    @Override
    public void stop(){
        super.stop();
        meleeCooldown = 0;
    }

    // 行刻
    @Override
    public void tick(){
        if(meleeCooldown>0) meleeCooldown--;
        if(tryMeleeInterrupt()) return;
        super.tick();
    }

    private boolean tryMeleeInterrupt(){
        if(meleeCooldown>0) return false;
        if(phase == ShootPhase.CHARGING_BAYONET) return false;
        if(!isValidTarget()) return false;
        LivingEntity target = mob.getTarget();
        if(!meleeGoal.canReachTarget(target)) return false;
        mob.swing(InteractionHand.MAIN_HAND);
        mob.doHurtTarget(target);
        meleeCooldown = meleeGoal.getMeleeInterval();
//        if(phase == ShootPhase.VOLLEY || phase == ShootPhase.TELEGRAPHING_VOLLEY) endVolley();
        return true;
    }

    private boolean isValidTarget(){
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    /**
     * 验有余弹否
     * */
    private boolean hasAmmo(){
        if(GolemAmmoUtil.isInfinite(mob)) return true;
        return GolemAmmoUtil.countAmmo(mob) > 0;
    }

    /**
     * 此処留空
     * */
    @Override
    public void performRangedAttack(LivingEntity livingEntity, float v, ItemStack itemStack, InteractionHand interactionHand) {
    }
}
