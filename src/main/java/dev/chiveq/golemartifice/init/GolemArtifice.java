package dev.chiveq.golemartifice.init;

import com.mojang.logging.LogUtils;
import dev.chiveq.golemartifice.content.GolemRangedGunAttackGoal;
import dev.xkmc.mob_weapon_api.registry.WeaponStatus;
//import dev.xkmc.modulargolems.content.entity.humanoid.weapon.GolemWeaponRegistry;
import dev.xkmc.modulargolems.content.entity.weapon.GolemWeaponRegistry;
import io.redspace.irons_artifice.item.GunItem;
//import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(GolemArtifice.MODID)
public class GolemArtifice {

    public static final String MODID = "golemartifice";

    private static final Logger LOGGER = LogUtils.getLogger();

    public GolemArtifice(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::setup);
        modContainer.registerConfig(ModConfig.Type.COMMON,GAConfig.SPEC);
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

    public static Identifier loc(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }
}
