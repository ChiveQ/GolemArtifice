package dev.chiveq.golemartifice.util;

import dev.chiveq.golemartifice.init.GAConfig;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Unique;

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
     * 傀儡所携弹总数。
     */
    public static int countAmmo(List<IItemHandlerModifiable> inventories) {
        int total = 0;
        for (IItemHandlerModifiable inv : inventories) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (stack.is(IronsArtificeTags.AMMO)) {
                    total += stack.getCount();
                }
            }
        }
        return total;
    }

    /**
     * 傀儡所携弹总数
     * */
    public static int countAmmo(AbstractGolemEntity<?,?> golem){
        List<IItemHandlerModifiable> available = golem.aggregateInventories();
        return countAmmo(available);
    }

    /**
     * 按物品栏顺序扣除子弹，返回实际扣除的数量。
     */
    @Unique
    public static int consumeAmmo(List<IItemHandlerModifiable> inventories, int amount) {
        int remaining = amount;
        for (IItemHandlerModifiable inv : inventories) {
            if (remaining <= 0) {
                break;
            }
            for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
                ItemStack stack = inv.getStackInSlot(i);
                if (!stack.is(IronsArtificeTags.AMMO)) {
                    continue;
                }
                int take = Math.min(remaining, stack.getCount());
                remaining -= inv.extractItem(i, take, false).getCount();
            }
        }
        return amount - remaining;
    }

}
