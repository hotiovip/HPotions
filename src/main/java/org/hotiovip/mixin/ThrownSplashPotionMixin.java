package org.hotiovip.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrownSplashPotion.class)
public class ThrownSplashPotionMixin {

    @ModifyVariable(
            method = "onHitAsPotion",
            at = @At(value = "STORE", ordinal = 0)
    )
    private MobEffectInstance hpotions$halfDuration(MobEffectInstance effect) {
        // Fix: Handle Holder<MobEffect> correctly
        Identifier id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());

        if (id != null && id.getNamespace().equals("hpotions")) {
            return new MobEffectInstance(
                    effect.getEffect(),
                    effect.getDuration() / 2,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.isVisible()
            );
        }

        return effect;
    }
}
