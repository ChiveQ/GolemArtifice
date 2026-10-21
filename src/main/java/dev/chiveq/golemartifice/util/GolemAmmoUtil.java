package dev.chiveq.golemartifice.util;

import dev.chiveq.golemartifice.init.GAConfig;
import dev.chiveq.golemartifice.mixin.AbstractGolemEntityInvoker;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;

public class GolemAmmoUtil {

    /**
     * 是否启用无限弹药
     * */
    public static boolean isInfinite(Mob mob){
        if(mob instanceof AbstractGolemEntity<?,?> golem){
            if(golem.isHostile()){
                return GAConfig.hostileGolemInfiniteAmmo;
            }else{
                return GAConfig.playerGolemInfiniteAmmo;
            }
        }
        return true; // 非傀儡则如原版生物
    }

    /**
     * 傀儡所携弹总数。对应GunplayManager::countBullets
     */
    public static int countAmmo(List<IItemHandlerModifiable> inventories) {
        int total = 0;
        for (IItemHandlerModifiable inv : inventories) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.is(IronsArtificeTags.AMMO)) {
                    total += stack.getCount();
                }else if(stack.getItem() instanceof BulletContainerItem){
                    total += BulletContainerItem.count(stack); // 弹药盒
                }
            }
        }
        return total;
    }

    /**
     * 傀儡所携弹总数
     * */
    public static int countAmmo(AbstractGolemEntity<?,?> golem){
        return countAmmo(aggregateInventories(golem));
    }

    /**
     * 傀儡的聚合物品栏，即本体对"傀儡身上带着什么"的定义：
     * 主手与副手 → 装备槽 → 由 {@code GolemCollectInventoryEvent} 注入的其它容器。
     *
     * <p>1.20.1 的 {@code AbstractGolemEntity#aggregateInventories()} 是 protected，
     * 这里经由 {@link AbstractGolemEntityInvoker} 生成的 public 转发方法访问，
     * 与 1.21.1 中直接调用该方法完全等价（虚拟分派，子类覆写照常生效），
     * 且不需要 AccessTransformer。
     */
    public static List<IItemHandlerModifiable> aggregateInventories(AbstractGolemEntity<?,?> golem){
        return ((AbstractGolemEntityInvoker) golem).GA$aggregateInventories();
    }

    /**
     * 按物品栏顺序扣除子弹，返回实际扣除的数量。
     */
    public static int consumeAmmo(List<IItemHandlerModifiable> inventories, int amount) {
        int remaining = amount;
        for (IItemHandlerModifiable inv : inventories) {
            if (remaining <= 0) {
                break;
            }
            // 零弹
            for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.is(IronsArtificeTags.AMMO)) {
                    continue;
                }
                int take = Math.min(remaining, stack.getCount());
                remaining -= inv.extractItem(i, take, false).getCount();
            }
            // 弹盒
            for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!(stack.getItem() instanceof  BulletContainerItem)) {
                    continue;
                }
                remaining -= BulletContainerItem.drain(stack,remaining);
            }
        }
        return amount - remaining;
    }

}
