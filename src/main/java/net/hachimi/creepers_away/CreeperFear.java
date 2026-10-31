package net.hachimi.creepers_away;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

public final class CreeperFear {
    /** Same distance as vanilla's fear-of-cats goal: 6 blocks. */
    public static final float FEAR_DISTANCE = 6.0F;
    private static final double WALK_SPEED = 1.0D;
    private static final double SPRINT_SPEED = 1.2D;
    private static final int GOAL_PRIORITY = 3;

    private CreeperFear() {
    }

    /** Called from the registerGoals mixin, i.e. once per creeper instance. */
    public static void attach(Creeper creeper) {
        creeper.goalSelector.addGoal(GOAL_PRIORITY, new AvoidEntityGoal<>(creeper, LivingEntity.class,
                FEAR_DISTANCE, WALK_SPEED, SPRINT_SPEED, entity -> isFeared(creeper, entity)));
    }

    private static boolean isFeared(Creeper creeper, LivingEntity entity) {
        if (entity == creeper) {
            return false;
        }
        if (entity instanceof Player player && CuriosSupport.wearingFearedItem(player)) {
            return true;
        }
        return Config.fearedIds().contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }
}