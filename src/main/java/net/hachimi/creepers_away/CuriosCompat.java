package net.hachimi.creepers_away;

import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

final class CuriosCompat {
    private CuriosCompat() {
    }

    /** True when the player wears a feared item in any Curios slot. */
    static boolean wearingFearedCurio(Player player) {
        Config.ItemTargets targets = Config.fearedCurios();
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(targets::matches))
                .orElse(false);
    }
}