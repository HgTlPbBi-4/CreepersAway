package net.hachimi.creepers_away;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.atomic.AtomicInteger;

public final class CreeperFear {
    private static final AtomicInteger ATTACHED = new AtomicInteger();
    private static final AtomicInteger MATCHED = new AtomicInteger();

    private CreeperFear() {
    }

    /** Same parameters as vanilla's fear-of-cats goal: 6 blocks, 1.0 walk speed, 1.2 sprint speed, priority 3. */
    public static void attach(Creeper creeper) {
        if (ATTACHED.incrementAndGet() <= 50) {
            CreepersAway.LOGGER.info("[ca-debug] attach goal to creeper {} at {}", creeper.getUUID(), creeper.blockPosition());
        }
        creeper.goalSelector.addGoal(3, new AvoidEntityGoal<>(creeper, LivingEntity.class, 6.0F, 1.0D, 1.2D,
                entity -> isFeared(creeper, entity)));
    }

    private static boolean isFeared(Creeper creeper, LivingEntity entity) {
        EntityType<?> type = entity.getType();
        boolean match;
        if (entity == creeper) {
            match = false;
        } else if (entity instanceof Player player && CuriosSupport.wearingFearedItem(player)) {
            match = true;
        } else {
            match = Config.fearedIds().contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
        }
        if (match && MATCHED.incrementAndGet() <= 50) {
            CreepersAway.LOGGER.info("[ca-debug] match: creeper {} fears {} (ids={}, curioIds={}, curiosLoaded={})",
                    creeper.getUUID(), type, Config.fearedIds(), Config.fearedCurios().ids(), CuriosSupport.isCuriosLoaded());
        }
        return match;
    }
}