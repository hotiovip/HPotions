package org.hotiovip;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.hotiovip.render.OutlineRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Client-side mod initializer for HEnchanting.
 * Handles rendering of ore outlines when effects are active.
 */
public class HPotionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register pipeline on Iris
        if (FabricLoader.getInstance().isModLoaded("iris")) SupportIris.assignPipelines();

        // Register world render event before translucent rendering
        WorldRenderEvents.END_MAIN.register(this::onWorldEndMainEvent);
        // Register item tooltip callback event
        ItemTooltipCallback.EVENT.register(this::changeTooltip);
    }

    private void onWorldEndMainEvent(WorldRenderContext context) {
        if (!shouldDrawOutline()) return;

        List<Block> blocksToCheckFor = getBlocksToCheckFor();
        if (blocksToCheckFor.isEmpty()) return;

        OutlineRenderer.getInstance().searchForBlocksAndDrawOutline(context, blocksToCheckFor);
     }
    private boolean shouldDrawOutline() {
        Minecraft client = Minecraft.getInstance();

        // Check if player and world are not null and if it has any effect
        return client.player != null && client.level != null && !client.player.getActiveEffects().isEmpty();
    }
    private List<Block> getBlocksToCheckFor() {
        List<Block> blocksToCheckFor = new ArrayList<>();

        assert Minecraft.getInstance().player != null;
        for (MobEffectInstance effect : Minecraft.getInstance().player.getActiveEffects()) {
            // Copper effect: highlight copper ores
            if (effect.is(HPotions.COPPER_EFFECT)) {
                blocksToCheckFor.add(Blocks.COPPER_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_COPPER_ORE);
            }
            // Iron effect: highlight iron ores
            else if (effect.is(HPotions.IRON_EFFECT)) {
                blocksToCheckFor.add(Blocks.IRON_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_IRON_ORE);
            }
            // Gold effect: highlight gold ores
            else if (effect.is(HPotions.GOLD_EFFECT)) {
                blocksToCheckFor.add(Blocks.GOLD_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_GOLD_ORE);
            }
            // Lapis effect: highlight lapis ores
            else if (effect.is(HPotions.LAPIS_EFFECT)) {
                blocksToCheckFor.add(Blocks.LAPIS_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_LAPIS_ORE);
            }
            // Redstone effect: highlight redstone ores
            else if (effect.is(HPotions.REDSTONE_EFFECT)) {
                blocksToCheckFor.add(Blocks.REDSTONE_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_REDSTONE_ORE);
            }
            // Diamond effect: highlight diamond ores
            else if (effect.is(HPotions.DIAMOND_EFFECT)) {
                blocksToCheckFor.add(Blocks.DIAMOND_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_DIAMOND_ORE);
            }
            // Emerald effect: highlight emerald ores
            else if (effect.is(HPotions.EMERALD_EFFECT)) {
                blocksToCheckFor.add(Blocks.EMERALD_ORE);
                blocksToCheckFor.add(Blocks.DEEPSLATE_EMERALD_ORE);
            }
            // Ancient debris effect: highlight ancient debris
            else if (effect.is(HPotions.ANCIENT_DEBRIS_EFFECT)) {
                blocksToCheckFor.add(Blocks.ANCIENT_DEBRIS);
            }
        }

        return blocksToCheckFor;
    }

    private void changeTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> components) {
        if (itemStack.is(Items.SPLASH_POTION)) {
            PotionContents potions = itemStack.get(DataComponents.POTION_CONTENTS);
            // Check if there is a potion
            if (potions != null && potions.potion().isPresent()) {
                Identifier id = BuiltInRegistries.POTION.getKey(potions.potion().get().value());

                // Check if it is one of my potions
                if (id != null && id.getNamespace().equals("hpotions")) {

                    if (components.size() > 1) {
                        // Get effect line (Copper Eye (3:00))
                        MutableComponent newLine = getHalvedDurationTooltip(components);

                        // Replace
                        components.set(1, newLine);
                    }
                }
            }
        }
    }
    private @NotNull MutableComponent getHalvedDurationTooltip(List<Component> components) {
        Component oldLine = components.get(1);
        String oldText = oldLine.getString();

        // Extract effect name (Copper Eye)
        String name = oldText.substring(0, oldText.indexOf(" ("));

        // Halve duration - parse "03:00" -> 180s -> 90s -> "01:30"
        String durationPart = oldText.substring(oldText.indexOf("(") + 1, oldText.indexOf(")"));
        String[] timeParts = durationPart.split(":");
        int minutes = Integer.parseInt(timeParts[0]);
        int seconds = Integer.parseInt(timeParts[1]);
        int totalSeconds = minutes * 60 + seconds;
        int halvedSeconds = totalSeconds / 2;
        String newDuration = String.format("%02d:%02d", halvedSeconds / 60, halvedSeconds % 60);

        // Build line with old style
        MutableComponent newLine = Component.literal(name + " (" + newDuration + ")");
        newLine.setStyle(oldLine.getStyle());  // Copies color, formatting (BLUE)
        return newLine;
    }
}
