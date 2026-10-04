package dev.chiveq.golemartifice.content;

import dev.xkmc.mob_weapon_api.api.ai.IWeaponHolder;
import dev.xkmc.mob_weapon_api.api.goals.IMeleeGoal;
import dev.xkmc.mob_weapon_api.example.goal.SmartRangedAttackGoal;
import io.redspace.irons_artifice.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 效仿taczgolemcompat
 * */
@Deprecated
public class IAASmartGoal<T extends Mob> extends SmartRangedAttackGoal<T> {

    private int attackDelay;
    private double attackCount;
    private int ammoCount;

    public IAASmartGoal(T mob, IWeaponHolder holder, IMeleeGoal melee, double speed, double radius) {
        super(mob, holder, melee, speed, radius);
    }

    @Override
    public boolean canUse() {
        return isHoldingGun() && (isValidTarget() || needsReload());
    }

    @Override
    public boolean canContinueToUse() {
        return isHoldingGun() && (isValidTarget() || needsReload());
    }


    @Override
    public void stop() {
        super.stop();
        if(isHoldingGun()){
            ReloadState.remove(mob.getMainHandItem()); // 去除装填状态
        }
        mob.getNavigation().stop();
    }

    @Nullable
    private GunItem getGun(){
        return mob.getMainHandItem().getItem() instanceof GunItem gunItem? gunItem : null;
    }

    private boolean isHoldingGun(){
        return getGun() != null;
    }

    private boolean isValidTarget(){
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    private boolean needsReload(){
        if(isHoldingGun()){
            ItemStack gun = mob.getMainHandItem();
            return GunItem.getMagazine(gun).isEmpty() && !GunItem.isReloading(gun);
        }
        return false;
    }

    private int getAmmoCount(ItemStack stack){
        if(stack.getItem() instanceof GunItem gun){
//            gun.getGun().
        }
        return 0;
    }

    private boolean hasAmmo(){
        return getAmmoCount(mob.getItemInHand(InteractionHand.MAIN_HAND)) != 0;
    }

    @Override
    public void tick(){
        LivingEntity target = mob.getTarget();
        if(!isHoldingGun()) return;
        // 看向目标
        if(isValidTarget()){
            mob.getLookControl().setLookAt(target,30,90);
        }
        ItemStack gun = mob.getMainHandItem();
        // 等待装填
        if(GunItem.isReloading(gun)) return;
        // 弹夹空则请求装填
        if(GunItem.getMagazine(gun).isEmpty()){
            GunplayManager.attemptStartReload(mob,gun);
            return;
        }
        // 无目标则不打
        if(!isValidTarget()) return;
        // 走位
        strafing();
        // 等待冷却
        if(FireDelayState.isActive(mob,gun)) return;
        // 射击
        if(isAimedAtTarget()){
            Vec3 aim = target.getEyePosition().subtract(mob.getEyePosition());
            if (aim.lengthSqr() < 1.0E-6) {
                return;
            }
            FireOutcome outcome = GunplayManager.tryFire(mob, aim.normalize());
            if (outcome == FireOutcome.FIRE_DELAY_ACTIVE) {
                // 冷却恰好在两次调用之间归零：入队、下 tick 再发。
                GunplayManager.queueEarlyShot(mob, aim.normalize());
            }
        }
    }

    /**
     * 视线与目標是否大致对齐。角度阈值参考 {@code TaczSmartGoal#isRightAngle}：
     * 距离越近越宽容、因为近距离下弹道散布自然更宽。
     */
    private boolean isAimedAtTarget() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        Vec3 diff = target.getEyePosition().subtract(mob.getEyePosition());
        if (diff.lengthSqr() < 1.0E-6) {
            return false;
        }
        Vec3 look = mob.getViewVector(1);
        double deg = vectorDegreeCalculate(look, diff);
        return deg < 10 + Math.max(0, 64 - mob.distanceToSqr(target));
    }

    public static double vectorDegreeCalculate(Vec3 a, Vec3 b) {
        double cos = a.dot(b) / a.length() / b.length();
        cos = Math.max(-1.0, Math.min(1.0, cos));
        return Math.toDegrees(Math.acos(cos));
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float v, ItemStack itemStack, InteractionHand interactionHand) {

    }


}
