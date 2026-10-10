package dev.chiveq.golemartifice.util;

import dev.chiveq.golemartifice.init.GAConfig;
import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import io.redspace.irons_artifice.item.BulletContainerItem;
import io.redspace.irons_artifice.utils.IronsArtificeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


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
    }

    /**
     * 傀儡所攜彈總數
     * */
    public static int countAmmo(ResourceHandler<@NotNull ItemResource> handler){
        int total = 0;
        for(int i = 0; i < handler.size(); i++){
            ItemResource res = handler.getResource(i);
            if(res.is(IronsArtificeTags.AMMO)) total += handler.getAmountAsInt(i);
            // 弹药盒：盒内的子弹数记在数据组件里，读一份副本就够了，不涉及写回
            else if(res.getItem() instanceof BulletContainerItem) total += BulletContainerItem.count(res.toStack());
        }
        return total;
    }

    /**
     * 按物品栏顺序扣除子弹，先零散子弹、后弹药盒，返回实际扣除的数量。
     *
     * <p>26.1.2 的傀儡物品栏是 {@link ResourceHandler}，{@link ItemResource} 是不可变值对象：
     * {@code getResource(i)} 拿到的是该槽当前内容的快照，{@code res.toStack()} 更是
     * {@code copyWithCount(1)} 出来的独立副本。想真正改动物品栏，只能经由
     * {@code extract}/{@code insert} 这类事务化接口。
     *
     * <p>所以弹药盒不能再照搬 1.21.1 的写法"取出 ItemStack 就地 drain"。IAA 原版
     * {@code GunplayManager#consumeBullets} 之所以能直接 drain，是因为
     * {@code inventory.getItem(i)} 返回的就是背包里那个 ItemStack 本体，改它即是改背包；
     * 而这里若在副本上调用 {@code BulletContainerItem.drain(box, remaining)}，
     * 被改写的自始至终是那份脱离物品栏的副本——这正是"参与计数却不被消耗"的原因。
     * 这里改为"取出旧盒 → 换入倒过弹的新盒"的同槽位原子交换。
     */
    public static int consumeAmmo(AbstractGolemEntity<?,?> golem,int amount){
        ResourceHandler<@NotNull ItemResource> handler = golem.getItemHandler();
        int remaining = amount;
        // 零弹：逐槽提取，extract 会如实返回实际扣到的数量
        for(int i = 0; i < handler.size() && remaining > 0; i++){
            ItemResource res = handler.getResource(i);
            if(!res.is(IronsArtificeTags.AMMO)) continue;
            int take = Math.min(remaining, handler.getAmountAsInt(i));
            if(take <= 0) continue;
            try(Transaction trans = Transaction.openRoot()){
                remaining -= handler.extract(i, res, take, trans);
                trans.commit();
            }
        }
        // 弹药盒：先在副本上算出这一轮能倒出多少，再用原子交换写回
        for(int i = 0; i < handler.size() && remaining > 0; i++){
            ItemResource res = handler.getResource(i);
            if(!(res.getItem() instanceof BulletContainerItem)) continue;
            ItemStack original = res.toStack();
            ItemStack drained = original.copy();
            int removed = BulletContainerItem.drain(drained, remaining);
            if(removed <= 0) continue;

            // 盒内仍然只有同一件物品，只是 BULLET_POUCH 组件变了
            ItemResource drainedRes = ItemResource.of(drained);
            boolean swapped = false;
            try(Transaction trans = Transaction.openRoot()){
                // 两步同进同退；只要第一步取不出来或第二步塞不回去，事务关闭时自动回滚
                if(handler.extract(i, res, 1, trans) == 1
                        && handler.insert(i, drainedRes, 1, trans) == 1){
                    trans.commit();
                    swapped = true;
                }
            }
            if(swapped){
                remaining -= removed;
                continue;
            }

            // 回退路径：非玩家的装备槽包装器用 canEquip 判合法性，而没有 EQUIPPABLE 组件的
            // 物品只被认作合法的主手物品，副手与护甲槽的写回必被拦下。
            // 这类槽位退回本体自己的写口——MG 装备物品走的也是 setItemSlot。
            // 此举在事务回滚之后进行，此时槽内已复原成原来的盒子。
            EquipmentSlot slot = findEquipmentSlot(golem, original);
            if(slot != null){
                golem.setItemSlot(slot, drained);
                remaining -= removed;
            }
            // 两条路都走不通：本次不扣减，留待下一次判定
        }
        return amount - remaining;
    }

    /**
     * 找出正装着这盒弹药的装备槽。若同时有多个槽位装着同样的盒子则不猜，返回 null。
     * */
    @Nullable
    private static EquipmentSlot findEquipmentSlot(AbstractGolemEntity<?,?> golem,ItemStack box){
        EquipmentSlot found = null;
        for(EquipmentSlot slot : EquipmentSlot.values()){
            if(!ItemStack.isSameItemSameComponents(golem.getItemBySlot(slot), box)) continue;
            if(found != null) return null;
            found = slot;
        }
        return found;
    }

}