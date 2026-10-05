package dev.chiveq.golemartifice.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = GolemArtifice.MODID)
public class GAConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue HOSTILE_GOLEM_INFINITE_AMMO = BUILDER.comment("Whether to allow hostile golems to use infinite ammo").define("hostileGolemInfiniteAmmo", true);
    private static final ModConfigSpec.BooleanValue PLAYER_GOLEM_INFINITE_AMMO = BUILDER.comment("Whether to allow player golems to use infinite ammo").define("playerGolemInfiniteAmmo", false);
//    private static final ModConfigSpec.IntValue GOLEM_GUN_SHOOT_RANGE = BUILDER.comment("The range by blocks that golems can reach when shooting with guns. Requires reload.").defineInRange("golemGunShootRange", 35, 1, Integer.MAX_VALUE);
    private static final ModConfigSpec.BooleanValue APPLY_MOB_NERFS_TO_PLAYER_GOLEMS = BUILDER.comment("Whether to apply the damage, speed, spread nerfs used on mobs to player golems").define("applyMobNerfsToPlayerGolems",true);
    private static final ModConfigSpec.BooleanValue APPLY_MOB_NERFS_TO_HOSTILE_GOLEMS = BUILDER.comment("Whether to apply the damage, speed, spread nerfs used on mobs to hostile golems").define("applyMobNerfsToHostileGolems",true);

//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean hostileGolemInfiniteAmmo,playerGolemInfiniteAmmo;
//    public static int golemGunShootRange;
    public static boolean applyMobNerfsToPlayerGolems,applyMobNerfsToHostileGolems;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        hostileGolemInfiniteAmmo = HOSTILE_GOLEM_INFINITE_AMMO.get();
        playerGolemInfiniteAmmo = PLAYER_GOLEM_INFINITE_AMMO.get();
//        golemGunShootRange = GOLEM_GUN_SHOOT_RANGE.get();
        applyMobNerfsToPlayerGolems = APPLY_MOB_NERFS_TO_PLAYER_GOLEMS.get();
        applyMobNerfsToHostileGolems = APPLY_MOB_NERFS_TO_HOSTILE_GOLEMS.get();
    }
}
