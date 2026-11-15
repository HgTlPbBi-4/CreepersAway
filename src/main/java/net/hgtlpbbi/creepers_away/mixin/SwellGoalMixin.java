package net.hgtlpbbi.creepers_away.mixin;

import net.hgtlpbbi.creepers_away.CreeperFear;
import net.hgtlpbbi.creepers_away.CuriosSupport;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwellGoal.class)
public abstract class SwellGoalMixin {
    @Shadow
    @Final
    private Creeper creeper;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void creepers_away$dontSwellNearWearer(CallbackInfo ci) {
        if (CuriosSupport.hasFearedWearerNearby(this.creeper, CreeperFear.FEAR_DISTANCE)) {
            this.creeper.setSwellDir(-1);
            ci.cancel();
        }
    }
}