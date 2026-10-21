package dev.chiveq.golemartifice.init;

import com.mojang.logging.LogUtils;
import dev.chiveq.golemartifice.content.GolemRangedGunAttackGoal;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
import dev.xkmc.modulargolems.content.entity.humanoid.weapon.GolemWeaponRegistry;
import io.redspace.irons_artifice.item.GunItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(GolemArtifice.MODID)
public class GolemArtifice {

    public static final String MODID = "golemartifice";

    private static final Logger LOGGER = LogUtils.getLogger();

    public GolemArtifice(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,GAConfig.SPEC);
    }

    /**
     * 注册远程武器
     * 此处效仿taczgolemcompat
     * */
    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(()->{
            GolemWeaponRegistry.HUMANOID.register(loc("irons_artifice_gun"),
                    (golem,stack,hand) -> WeaponStatus.RANGED.of(stack.getItem() instanceof GunItem),
                    (golem,melee) -> new GolemRangedGunAttackGoal(golem,melee,35/*GAConfig.golemGunShootRange*/)
            );
        });
    }

    public static ResourceLocation loc(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
