package org.hotiovip;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import org.hotiovip.effect.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HPotions implements ModInitializer {
	public static final String MOD_ID = "hpotions";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Holder<@NotNull MobEffect> ENDER_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "ender_eye"), new EnderEffect());
    public static final Holder<@NotNull Potion> ENDER_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "ender_eye"),
                    new Potion("ender_eye", new MobEffectInstance(HPotions.ENDER_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> COPPER_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "copper_eye"), new CopperEffect());
    public static final Holder<@NotNull Potion> COPPER_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "copper_eye"),
                    new Potion("copper_eye", new MobEffectInstance(HPotions.COPPER_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> IRON_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "iron_eye"), new IronEffect());
    public static final Holder<@NotNull Potion> IRON_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "iron_eye"),
                    new Potion("iron_eye", new MobEffectInstance(HPotions.IRON_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> GOLD_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "gold_eye"), new GoldEffect());
    public static final Holder<@NotNull Potion> GOLD_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "gold_eye"),
                    new Potion("gold_eye", new MobEffectInstance(HPotions.GOLD_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> LAPIS_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "lapis_eye"), new LapisEffect());
    public static final Holder<@NotNull Potion> LAPIS_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "lapis_eye"),
                    new Potion("lapis_eye", new MobEffectInstance(HPotions.LAPIS_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> REDSTONE_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "redstone_eye"), new RedstoneEffect());
    public static final Holder<@NotNull Potion> REDSTONE_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "redstone_eye"),
                    new Potion("redstone_eye", new MobEffectInstance(HPotions.REDSTONE_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> DIAMOND_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "diamond_eye"), new DiamondEffect());
    public static final Holder<@NotNull Potion> DIAMOND_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "diamond_eye"),
                    new Potion("diamond_eye", new MobEffectInstance(HPotions.DIAMOND_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> EMERALD_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "emerald_eye"), new EmeraldEffect());
    public static final Holder<@NotNull Potion> EMERALD_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "emerald_eye"),
                    new Potion("emerald_eye", new MobEffectInstance(HPotions.EMERALD_EFFECT, 3600, 0)));

    public static final Holder<@NotNull MobEffect> ANCIENT_DEBRIS_EFFECT =
            Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "ancient_debris_eye"), new AncientDebrisEffect());
    public static final Holder<@NotNull Potion> ANCIENT_DEBRIS_POTION =
            Registry.registerForHolder(
                    BuiltInRegistries.POTION,
                    Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "ancient_debris_eye"),
                    new Potion("ancient_debris_eye", new MobEffectInstance(HPotions.ANCIENT_DEBRIS_EFFECT, 3600, 0)));

    @Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("HEnchanting loaded!");

        // Ender potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                Potions.AWKWARD,
                // Ingredient
                Items.ENDER_PEARL,
                // Output potion.
                ENDER_POTION
        ));

        // Copper potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.COPPER_INGOT,
                // Output potion.
                COPPER_POTION
        ));

        // Iron potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.IRON_INGOT,
                // Output potion.
                IRON_POTION
        ));

        // Gold potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.GOLD_INGOT,
                // Output potion.
                GOLD_POTION
        ));

        // Lapis potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.LAPIS_LAZULI,
                // Output potion.
                LAPIS_POTION
        ));

        // Redstone potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.REDSTONE,
                // Output potion.
                REDSTONE_POTION
        ));

        // Diamond potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.DIAMOND,
                // Output potion.
                DIAMOND_POTION
        ));

        // Emerald potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.EMERALD,
                // Output potion.
                EMERALD_POTION
        ));

        // Ancient debris potion brewing recipes
        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder -> builder.addMix(
                // Input potion.
                ENDER_POTION,
                // Ingredient
                Items.NETHERITE_SCRAP,
                // Output potion.
                ANCIENT_DEBRIS_POTION
        ));
	}
}