package dev.chiveq.golemartifice.mixin;

import dev.chiveq.golemartifice.util.GolemAmmoUtil;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.data.ShotComponents;
import io.redspace.irons_artifice.gun.ShotProfile;
import io.redspace.irons_artifice.item.GunItem;
import io.redspace.irons_artifice.item.GunplayManager;
import io.redspace.irons_artifice.item.MagazineContents;
import io.redspace.irons_artifice.item.ReloadState;
import io.redspace.irons_artifice.item.TopLoadConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 让傀儡在装填枪械时真正消耗自己携带的子弹。
 *
 * <p>IAA 原版的 {@code GunplayManager#requiresAmmo} 只对 Player 返回 true，
 * 所以包括傀儡在内的所有生物都是"无限弹药"。这里从装填的两个入口整体接管：
 * 当目标是非无限弹药的傀儡（判定见 {@link GolemAmmoUtil#isInfinite}）时，
 * 用傀儡自己的物品栏取代原版的 {@code Player} 背包来统计与扣减子弹，
 * 其余情况（非傀儡、以及配置为无限弹药的傀儡）直接放行给原逻辑。
 *
 * <p>弹药来源取自 {@link AbstractGolemEntity#aggregateInventories()}，
 * 也就是本体对"傀儡身上带着什么"的定义，顺序为：
 * 主手与副手 → 装备槽 → 箭矢槽（arrowSlot）→ 备用副手槽（backupHand）。
 * 因此副手槽与箭矢槽里的子弹天然会被消耗，无需单独开槽；
 * 同时该列表末尾会由 {@code GolemCollectInventoryEvent} 补上其它模组注入的容器，
 * L2Backpack 的空间存储就是通过这个事件挂进来的，
 * 所以这类外置容器同样无需额外适配，也不必在编译期依赖它们。
 *
 * <p>敌方傀儡（{@code isHostile() == true}）默认保持无限弹药，
 * 由配置项 {@code hostileGolemInfiniteAmmo} 控制。
 */
@Mixin(GunplayManager.class)
public abstract class GunplayManagerMixin {

    /**
     * 接管装填的起始：没有可用子弹就不进入装填状态。
     */
    @Inject(method = "attemptStartReload", at = @At("HEAD"), cancellable = true)
    private static void GA$golemAmmoStartReload(LivingEntity living, ItemStack gun, CallbackInfoReturnable<ReloadResult> cir) {
        if(!(living instanceof HumanoidGolemEntity)) return;
        if(GolemAmmoUtil.isInfinite((HumanoidGolemEntity)living)) return;
        if(living.level().isClientSide()) return;
        HumanoidGolemEntity golem = (HumanoidGolemEntity) living;
        if (!(gun.getItem() instanceof GunItem gunItem)) {
            cir.setReturnValue(ReloadResult.NO_AMMO);
            return;
        }

        int capacity = gunItem.magazineCapacity();
        MagazineContents magazine = GunItem.getMagazine(gun);
        int missing = magazine.missing(capacity);
        if (missing <= 0) {
            cir.setReturnValue(ReloadResult.ALREADY_FULL);
            return;
        }

        // 没有子弹就不开始装填；装不下的部分不参与动画时长计算
        List<IItemHandlerModifiable> inventories = golem.aggregateInventories();
        int available = GolemAmmoUtil.countAmmo(inventories);
        if (available <= 0) {
            cir.setReturnValue(ReloadResult.NO_AMMO);
            return;
        }
        missing = Math.min(missing, available);

        ShotProfile shotProfile = GunplayManager.compose(living, gunItem.getGun(), gun);
        double speed = shotProfile.value(ShotComponents.RELOAD_SPEED_MULTIPLIER);
        TopLoadConfig topLoad = gunItem.getGun().topLoadConfig();
        // 只装一部分时走"逐发填装"的动画片段，与原版一致
        boolean topOff = topLoad != null && missing < capacity;
        ReloadState.start(gun, gunItem.getGun().reloadTimeTicks(), speed, missing, topOff ? topLoad : null);
        GunplayManager.playReloadAnimation(living, gun);

        if (living.isUsingItem()) {
            living.stopUsingItem();
        }

        cir.setReturnValue(ReloadResult.STARTING_RELOAD);
    }

    /**
     * 接管装填的结束：按实际扣得到的数量填进弹匣。
     */
    @Inject(method = "attemptFinishReload", at = @At("HEAD"), cancellable = true)
    private static void GA$golemAmmoFinishReload(LivingEntity living, ItemStack gun, int roundsToLoad, CallbackInfoReturnable<ReloadResult> cir) {
        if(!(living instanceof HumanoidGolemEntity)) return;
        if(GolemAmmoUtil.isInfinite((HumanoidGolemEntity)living)) return;
        HumanoidGolemEntity golem = (HumanoidGolemEntity) living;

        if (!(gun.getItem() instanceof GunItem gunItem)) {
            cir.setReturnValue(ReloadResult.NO_AMMO);
            return;
        }

        int capacity = gunItem.magazineCapacity();
        MagazineContents magazine = GunItem.getMagazine(gun);
        int missing = magazine.missing(capacity);
        if (missing <= 0) {
            cir.setReturnValue(ReloadResult.ALREADY_FULL);
            return;
        }

        // 装填期间子弹可能被拿走，此时按无弹处理，等下一次装填
        List<IItemHandlerModifiable> inventories = golem.aggregateInventories();
        int available = GolemAmmoUtil.countAmmo(inventories);
        if (available <= 0) {
            cir.setReturnValue(ReloadResult.NO_AMMO);
            return;
        }

        int toLoad = Math.min(missing, available);
        if (roundsToLoad > 0) {
            toLoad = Math.min(toLoad, roundsToLoad);
        }
        // 按实际扣除的数量装填，避免扣不出来却把子弹算进弹匣
        toLoad = GolemAmmoUtil.consumeAmmo(inventories, toLoad);
        if (toLoad <= 0) {
            cir.setReturnValue(ReloadResult.NO_AMMO);
            return;
        }

        GunItem.setMagazine(gun, magazine.with(magazine.count() + toLoad));
        cir.setReturnValue(ReloadResult.FINISHED_RELOAD);
    }

}