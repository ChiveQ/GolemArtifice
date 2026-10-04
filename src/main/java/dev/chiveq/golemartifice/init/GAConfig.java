package dev.chiveq.golemartifice.init;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = GolemArtifice.MODID)
public class GAConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue HOSTILE_GOLEM_INFINITE_AMMO = BUILDER.comment("Whether to allow hostile golems to use infinite ammo").define("hostileGolemInfiniteAmmo", true);
    private static final ModConfigSpec.BooleanValue PLAYER_GOLEM_INFINITE_AMMO = BUILDER.comment("Whether to allow player golems to use infinite ammo").define("playerGolemInfiniteAmmo", false);

//    private static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER.comment("A magic number").defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("What you want the introduction message to be for the magic number").define("magicNumberIntroduction", "The magic number is... ");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean hostileGolemInfiniteAmmo,playerGolemInfiniteAmmo;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        hostileGolemInfiniteAmmo = HOSTILE_GOLEM_INFINITE_AMMO.get();
        playerGolemInfiniteAmmo = PLAYER_GOLEM_INFINITE_AMMO.get();
    }

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
}
