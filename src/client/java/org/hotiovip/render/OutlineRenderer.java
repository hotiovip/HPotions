package org.hotiovip.render;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.hotiovip.ColorProvider;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OutlineRenderer {
    private static OutlineRenderer instance;

    public static OutlineRenderer getInstance() {
        if (instance == null) instance = new OutlineRenderer();
        return instance;
    }

    // Performance optimization: scan blocks periodically instead of every frame
    private int scanTick = 0;
    // Dynamic lists for effect-based rendering
    private final List<BlockPos> cachedBlocks = new ArrayList<>();

    /**
     * Main rendering entry point called every frame.
     * Checks player effects and delegates to rendering methods.
     */
    public void searchForBlocksAndDrawOutline(WorldRenderContext context, List<Block> blocksToCheckFor) {
        renderBlocks(context, blocksToCheckFor);
    }

    /**
     * Scans for target blocks and renders wireframe outlines.
     * Uses performance optimization by scanning only every 10 ticks.
     */
    private void renderBlocks(WorldRenderContext context, List<Block> blocksToCheckFor) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }

        // Performance optimization: scan every 10 ticks (0.5 seconds at 20 TPS)
        if (scanTick++ % 10 == 0) {
            cachedBlocks.clear();
            BlockPos playerPos = client.player.getOnPos();

            // Scan 32x32x32 area around player
            for (int dx = -32; dx <= 32; dx++) {
                for (int dy = -32; dy <= 32; dy++) {
                    for (int dz = -32; dz <= 32; dz++) {
                        BlockPos pos = playerPos.mutable().set(dx, dy, dz);
                        if (blocksToCheckFor.contains(client.level.getBlockState(pos).getBlock())) {
                            cachedBlocks.add(pos.immutable());
                        }
                    }
                }
            }
        }

        VertexFormat vertexFormat = VertexFormat.builder()
                .add("Position", VertexFormatElement.POSITION)
                .add("Color", VertexFormatElement.COLOR)
                .add("Normal", VertexFormatElement.NORMAL)
                .build();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, vertexFormat);

        // Render cached ore positions
        Iterator<BlockPos> iterator = cachedBlocks.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            // Check if block was destroyed
            if (client.level.getBlockState(pos).isAir()) {
                iterator.remove();
                continue;
            }

            float[] color = ColorProvider.getInstance().getNormalizedColorForBlock(
                    client.level.getBlockState(pos).getBlock());

            if (color != null) {
                renderLineBoxWithCulling(blocksToCheckFor, context.positionMatrix(), buffer, pos, client.level, color[0], color[1], color[2], color[3]);
            }
        }

        // Setup rendering state for lines through walls
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(4.0f);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

        // Draw the buffer
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    /**
     * Renders a wireframe box with face culling.
     * Only renders edges for faces that are exposed (adjacent block is not the same ore type).
     */
    private void renderLineBoxWithCulling(List<Block> blocksToCheckFor, Matrix4f matrix, BufferBuilder buffer, BlockPos pos, Level level, float r, float g, float b, float a) {
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = pos.getX() + 1f;
        float maxY = pos.getY() + 1f;
        float maxZ = pos.getZ() + 1f;

        // Check which neighbors are NOT matching ores (face is exposed)
        boolean renderNorth = !blocksToCheckFor.contains(level.getBlockState(pos.north()).getBlock());
        boolean renderSouth = !blocksToCheckFor.contains(level.getBlockState(pos.south()).getBlock());
        boolean renderWest  = !blocksToCheckFor.contains(level.getBlockState(pos.west()).getBlock());
        boolean renderEast  = !blocksToCheckFor.contains(level.getBlockState(pos.east()).getBlock());
        boolean renderUp    = !blocksToCheckFor.contains(level.getBlockState(pos.above()).getBlock());
        boolean renderDown  = !blocksToCheckFor.contains(level.getBlockState(pos.below()).getBlock());

        // Render only edges of exposed faces (12 edges total per box)

        // Bottom edges (4)
        if (renderDown && renderNorth) addLine(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        if (renderDown && renderSouth) addLine(buffer, matrix, minX, minY, maxZ, maxX, minY, maxZ, r, g, b, a);
        if (renderDown && renderWest)  addLine(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, r, g, b, a);
        if (renderDown && renderEast)  addLine(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);

        // Top edges (4)
        if (renderUp && renderNorth) addLine(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        if (renderUp && renderSouth) addLine(buffer, matrix, minX, maxY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        if (renderUp && renderWest)  addLine(buffer, matrix, minX, maxY, minZ, minX, maxY, maxZ, r, g, b, a);
        if (renderUp && renderEast)  addLine(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);

        // Vertical edges (4)
        if (renderNorth && renderWest) addLine(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        if (renderNorth && renderEast) addLine(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        if (renderSouth && renderWest) addLine(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
        if (renderSouth && renderEast) addLine(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
    }

    /**
     * Adds a single line segment to the buffer.
     */
    private void addLine(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        // Start vertex
        buffer.addVertex(matrix, x1, y1, z1)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);

        // End vertex
        buffer.addVertex(matrix, x2, y2, z2)
                .setColor(r, g, b, a)
                .setNormal(0, 1, 0);
    }

    /**
     * Cleanup (no GPU buffers needed).
     */
    public void close() {
        cachedBlocks.clear();
    }
}
