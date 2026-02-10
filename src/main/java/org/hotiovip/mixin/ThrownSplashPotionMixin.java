package org.hotiovip.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrownPotion.class)
public class ThrownSplashPotionMixin {

    @ModifyVariable(
            method = "applySplash",
            at = @At(value = "STORE", ordinal = 0)
    )
    private MobEffectInstance hpotions$halfDuration(MobEffectInstance effect) {
        // Fix: Handle Holder<MobEffect> correctly
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());

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
