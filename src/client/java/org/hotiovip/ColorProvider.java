package org.hotiovip;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class that provides RGB color mappings for different ore blocks.
 */
public class ColorProvider {
    // Private static instance - initialized on first access
    private static ColorProvider instance;

    // Color mappings for ore blocks (RGB values 0-255)
    private final Map<Block, int[]> colors;

    /**
     * Private constructor prevents external instantiation.
     */
    private ColorProvider() {
        colors = new HashMap<>();
        initializeColors();
    }

    /**
     * Thread-safe lazy initialization singleton getter.
     * @return The singleton instance
     */
    public static synchronized ColorProvider getInstance() {
        if (instance == null) {
            instance = new ColorProvider();
        }
        return instance;
    }

    /**
     * Initialize color mappings for ore blocks.
     */
    private void initializeColors() {
        colors.put(Blocks.COPPER_ORE, new int[]{138, 64, 0, 255});
        colors.put(Blocks.DEEPSLATE_COPPER_ORE, new int[]{138, 64, 0, 255});

        colors.put(Blocks.IRON_ORE, new int[]{184, 184, 184, 255});
        colors.put(Blocks.DEEPSLATE_IRON_ORE, new int[]{184, 184, 184, 255});

        colors.put(Blocks.GOLD_ORE, new int[]{255, 255, 0, 255});
        colors.put(Blocks.DEEPSLATE_GOLD_ORE, new int[]{255, 255, 0, 255});

        colors.put(Blocks.LAPIS_ORE, new int[]{0, 0, 255, 255});
        colors.put(Blocks.DEEPSLATE_LAPIS_ORE, new int[]{0, 0, 255, 255});

        colors.put(Blocks.REDSTONE_ORE, new int[]{255, 0, 0, 255});
        colors.put(Blocks.DEEPSLATE_REDSTONE_ORE, new int[]{255, 0, 0, 255});

        colors.put(Blocks.DIAMOND_ORE, new int[]{0, 255, 255, 255});
        colors.put(Blocks.DEEPSLATE_DIAMOND_ORE, new int[]{0, 255, 255, 255});

        colors.put(Blocks.EMERALD_ORE, new int[]{0, 255, 0, 255});
        colors.put(Blocks.DEEPSLATE_EMERALD_ORE, new int[]{0, 255, 0, 255});

        colors.put(Blocks.ANCIENT_DEBRIS, new int[]{255, 165, 0, 255});
    }

    /**
     * Get the RGB color array for a specific block.
     * @param block The block to get color for
     * @return int array {R, G, B} with values 0-255, or null if not found
     */
    public int[] getColorForBlock(Block block) {
        return colors.get(block);
    }
    /**
     * Get normalized color values (0.0-1.0) for rendering.
     * @param block The block to get color for
     * @return float array {R, G, B} with values 0.0-1.0, or null if not found
     */
    public float[] getNormalizedColorForBlock(Block block) {
        int[] color = colors.get(block);
        if (color == null) {
            return null;
        }
        return new float[]{color[0] / 255f, color[1] / 255f, color[2] / 255f, color[3] / 255f};
    }

    /**
     * Check if a block has a registered color.
     * @param block The block to check
     * @return true if color is registered
     */
    public boolean hasColor(Block block) {
        return colors.containsKey(block);
    }
}
