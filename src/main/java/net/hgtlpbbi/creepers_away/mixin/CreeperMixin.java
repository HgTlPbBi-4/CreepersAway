package net.hgtlpbbi.creepers_away.mixin;

import net.hgtlpbbi.creepers_away.CreeperFear;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void creepers_away$replaceVanillaCatGoals(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        // Vanilla hardcodes Cat/Ocelot avoidance; the config list owns that behavior now.
        creeper.goalSelector.removeAllGoals(goal -> goal instanceof AvoidEntityGoal);
        CreeperFear.attach(creeper);
    }
}