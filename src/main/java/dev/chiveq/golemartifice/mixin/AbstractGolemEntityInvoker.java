package dev.chiveq.golemartifice.mixin;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * 访问 {@code AbstractGolemEntity#aggregateInventories()} 的桥接。
 *
 * <p>1.20.1 的 Modular Golems 把该方法声明为 {@code protected}（1.21.1 中是 {@code public}），
 * 本模组需要从外部读取傀儡的聚合物品栏，因此用 Mixin 的 {@link Invoker} 在目标类内部
 * 生成一个 {@code public} 的转发方法：生成的字节码就位于 {@code AbstractGolemEntity} 自身，
 * 因此既不需要 AccessTransformer，也不会改动原方法的可见性。
 *
 * <p>目标方法不是 {@code private}，Mixin 会以 {@code INVOKEVIRTUAL} 转发，
 * 与直接调用 {@code golem.aggregateInventories()} 完全等价——包括
 * {@code MetalGolemEntity}、{@code SweepGolemEntity} 等子类的覆写都会照常生效。
 *
 * <p>调用入口见 {@code GolemAmmoUtil.aggregateInventories(AbstractGolemEntity)}。
 */
@Mixin(AbstractGolemEntity.class)
public interface AbstractGolemEntityInvoker {

    // remap = false：目标是另一个模组的方法，不在 Minecraft 的混淆映射里，
    // 运行时按字面名字解析即可（Forge 1.20.1 的 Mixin AP 否则会报找不到映射）。
    @Invoker(value = "aggregateInventories", remap = false)
    List<IItemHandlerModifiable> GA$aggregateInventories();

}
