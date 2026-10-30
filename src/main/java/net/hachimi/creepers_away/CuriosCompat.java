package net.hachimi.creepers_away;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

final class CuriosCompat {
    private CuriosCompat() {
    }

    /** True when the player wears a configured curio in any Curios slot. */
    static boolean wearingFearedCurio(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.isEquipped(CuriosCompat::isFearedItem))
                .orElse(false);
    }

    /** True when any non-spectator feared-curio wearer is within {@code radius} blocks of the entity. */
    static boolean hasFearedWearerNearby(Entity center, double radius) {
        return !center.level()
                .getEntitiesOfClass(Player.class, center.getBoundingBox().inflate(radius),
                        player -> !player.isSpectator() && wearingFearedCurio(player))
                .isEmpty();
    }

    private static boolean isFearedItem(ItemStack stack) {
        if (Config.fearedCurioIds().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return true;
        }
        for (TagKey<Item> tag : Config.fearedCurioTags()) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }
}