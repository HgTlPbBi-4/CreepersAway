package net.hachimi.creepers_away;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class CuriosSupport {
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    private CuriosSupport() {
    }

    public static boolean isCuriosLoaded() {
        return CURIOS_LOADED;
    }

    /** True when the player wears/holds a configured item in armor slots, hands or Curios slots. */
    public static boolean wearingFearedItem(Player player) {
        Config.ItemTargets armor = Config.fearedArmor();
        if (armor.matches(player.getItemBySlot(EquipmentSlot.HEAD))
                || armor.matches(player.getItemBySlot(EquipmentSlot.CHEST))
                || armor.matches(player.getItemBySlot(EquipmentSlot.LEGS))
                || armor.matches(player.getItemBySlot(EquipmentSlot.FEET))) {
            return true;
        }
        Config.ItemTargets held = Config.fearedHeld();
        if (held.matches(player.getItemBySlot(EquipmentSlot.MAINHAND))
                || held.matches(player.getItemBySlot(EquipmentSlot.OFFHAND))) {
            return true;
        }
        return CURIOS_LOADED && CuriosCompat.wearingFearedCurio(player);
    }

    /** True when any non-spectator player wearing a feared item is within {@code radius} blocks. */
    public static boolean hasFearedWearerNearby(Entity center, double radius) {
        return !center.level()
                .getEntitiesOfClass(Player.class, center.getBoundingBox().inflate(radius),
                        player -> !player.isSpectator() && wearingFearedItem(player))
                .isEmpty();
    }
}