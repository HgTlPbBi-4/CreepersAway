package net.hgtlpbbi.creepers_away;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public final class CuriosSupport {
    private static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");

    private CuriosSupport() {
    }

    /** True when the player wears/holds a configured item in armor slots, hands or Curios slots. */
    public static boolean wearingFearedItem(Player player) {
        if (player.isSpectator() || !Config.anyWearTargets()) {
            return false;
        }
        Config.ItemTargets armor = Config.fearedArmor();
        if (!armor.isEmpty()
                && (armor.matches(player.getItemBySlot(EquipmentSlot.HEAD))
                || armor.matches(player.getItemBySlot(EquipmentSlot.CHEST))
                || armor.matches(player.getItemBySlot(EquipmentSlot.LEGS))
                || armor.matches(player.getItemBySlot(EquipmentSlot.FEET)))) {
            return true;
        }
        Config.ItemTargets held = Config.fearedHeld();
        if (!held.isEmpty()
                && (held.matches(player.getItemBySlot(EquipmentSlot.MAINHAND))
                || held.matches(player.getItemBySlot(EquipmentSlot.OFFHAND)))) {
            return true;
        }
        return CURIOS_LOADED && !Config.fearedCurios().isEmpty() && CuriosCompat.wearingFearedCurio(player);
    }

    /** True when any player wearing a feared item is within {@code radius} blocks. */
    public static boolean hasFearedWearerNearby(Entity center, double radius) {
        if (!Config.anyWearTargets()) {
            return false;
        }
        return !center.level()
                .getEntitiesOfClass(Player.class, center.getBoundingBox().inflate(radius),
                        CuriosSupport::wearingFearedItem)
                .isEmpty();
    }
}