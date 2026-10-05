package dev.chiveq.golemartifice.util;

import dev.chiveq.golemartifice.init.GAConfig;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;


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
     * 傀儡所携弹总数
     * */
    public static int countAmmo(AbstractGolemEntity<?,?> golem){
        return countAmmo(golem.getItemHandler());
        //List<IItemHandlerModifiable> available = golem.aggregateInventories();
        //return countAmmo(available);
    }

    /**
     * 傀儡所攜彈總數
     * */
    public static int countAmmo(ResourceHandler<@NotNull ItemResource> handler){
        int total = 0;
        for(int i = 0; i < handler.size(); i++){
            ItemResource res = handler.getResource(i);
            if(res.is(IronsArtificeTags.AMMO)) total += handler.getAmountAsInt(i);
        }
        return total;
    }

    /**
     * 按物品栏顺序扣除子弹，返回实际扣除的数量。
     */
    public static int consumeAmmo(ResourceHandler<@NotNull ItemResource> handler,int amount){
        int remaining = amount;
        for(int i = 0; i< handler.size(); i++){
            if(remaining <= 0 ) break;
            ItemResource res = handler.getResource(i);
            if(!res.is(IronsArtificeTags.AMMO)) continue;
            int take = Math.min(remaining,handler.getAmountAsInt(i));
            try(Transaction trans = Transaction.openRoot()){
                remaining -= handler.extract(res,take,trans);
                trans.commit();
            }
        }
        return amount  - remaining;
    }

}
