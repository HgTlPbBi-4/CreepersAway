package net.hachimi.creepers_away;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class CuriosSupport {
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    private CuriosSupport() {
    }

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }

    public static boolean wearingFearedCurio(Player player) {
        return CURIOS_LOADED && CuriosCompat.wearingFearedCurio(player);
    }

    public static boolean hasFearedWearerNearby(Entity center, double radius) {
        return CURIOS_LOADED && CuriosCompat.hasFearedWearerNearby(center, radius);
    }
}