package org.hotiovip;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

/**
 * Client-side mod initializer for HEnchanting.
 * Handles rendering of ore outlines when effects are active.
 */
public class HPotionsClient implements ClientModInitializer {
    private static HPotionsClient instance;

    // Render pipeline
    public static final RenderPipeline LINES_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(HPotions.MOD_ID, "pipeline/lines_through_walls"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withCull(false)
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withColorWrite(true, true)
                    .withDepthWrite(false)
                    .build()
    );

    // Buffer management
    private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private BufferBuilder buffer;

    // Rendering constants
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    // Vertex buffer for GPU upload
    private MappableRingBuffer vertexBuffer;

    // Performance optimization: scan blocks periodically instead of every frame
    private int scanTick = 0;

    // Dynamic lists for effect-based rendering
    private final List<Block> blocksToCheckFor = new ArrayList<>();
    private final List<BlockPos> cachedBlocks = new ArrayList<>();

    public static HPotionsClient getInstance() {
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;

        // Register pipeline on Iris
        if (FabricLoader.getInstance().isModLoaded("iris")) SupportIris.assignPipelines();

        // Register world render event before translucent rendering
        WorldRenderEvents.END_MAIN.register(this::extractAndDrawBlocks);
        // Register item tooltip callback event
        ItemTooltipCallback.EVENT.register(this::changeTooltip);
    }

    /**
     * Main rendering entry point called every frame.
     * Checks player effects and delegates to rendering methods.
     */
    private void extractAndDrawBlocks(WorldRenderContext context) {
        var client = Minecraft.getInstance();

        // Early exit if player or world is null
        if (client.player == null || client.player.getActiveEffects().isEmpty()) {
            return;
        }

        if (client.level == null) {
            return;
        }

        // Clear and rebuild block list based on active effects
        blocksToCheckFor.clear();

        for (MobEffectInstance effect : client.player.getActiveEffects()) {
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

        // No relevant effects active, skip rendering
        if (blocksToCheckFor.isEmpty()) {
            return;
        }

        // Perform rendering
        renderBlocks(context);

        // Draw the buffer if it contains data
        if (buffer != null && !cachedBlocks.isEmpty()) {
            try {
                drawLinesThroughWalls(client, LINES_THROUGH_WALLS);
            } catch (Exception e) {
                HPotions.LOGGER.error("Error drawing ore outlines", e);
                buffer = null; // Cleanup on error
            }
        }

    }
    /**
     * Scans for target blocks and renders wireframe outlines.
     * Uses performance optimization by scanning only every 10 ticks.
     */
    private void renderBlocks(WorldRenderContext context) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            return;
        }

        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        // Transform to world space
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        // Initialize buffer if needed
        if (buffer == null) {
            buffer = new BufferBuilder(allocator,
                    LINES_THROUGH_WALLS.getVertexFormatMode(),
                    LINES_THROUGH_WALLS.getVertexFormat());
        }

        // Performance optimization: scan every 10 ticks (0.5 seconds at 20 TPS)
        if (scanTick++ % 10 == 0) {
            cachedBlocks.clear();
            BlockPos playerPos = client.player.blockPosition();

            // Scan 32x32x32 area around player
            for (int dx = -32; dx <= 32; dx++) {
                for (int dy = -32; dy <= 32; dy++) {
                    for (int dz = -32; dz <= 32; dz++) {
                        BlockPos pos = new BlockPos(
                                playerPos.getX() + dx,
                                playerPos.getY() + dy,
                                playerPos.getZ() + dz
                        );

                        BlockState state = client.level.getBlockState(pos);

                        // Check if block matches any target block
                        if (blocksToCheckFor.contains(state.getBlock())) {
                            cachedBlocks.add(pos.immutable());
                        }
                    }
                }
            }
        }

        // Render cached ore positions
        Iterator<BlockPos> iterator = cachedBlocks.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();

            // Check if block was destroyed
            if (client.level.getBlockState(pos).isAir()) {
                iterator.remove();
                continue; // Skip rendering this block
            }

            float[] color = ColorProvider.getInstance().getNormalizedColorForBlock(
                    client.level.getBlockState(pos).getBlock());

            if (color != null) {
                renderLineBoxWithCulling(matrices.last().pose(), buffer, pos, client.level,
                        color[0], color[1], color[2], 0.5f); // Note: color[3] doesn't exist (only RGB)
            }
        }

        matrices.popPose();
    }
    /**
     * Renders a wireframe box with face culling.
     * Only renders edges for faces that are exposed (adjacent block is not the same ore type).
     */
    private void renderLineBoxWithCulling(Matrix4fc matrix, BufferBuilder buffer, BlockPos pos, ClientLevel world, float r, float g, float b, float a) {
        float minX = pos.getX();
        float minY = pos.getY();
        float minZ = pos.getZ();
        float maxX = pos.getX() + 1f;
        float maxY = pos.getY() + 1f;
        float maxZ = pos.getZ() + 1f;

        // Check which neighbors are NOT matching ores (face is exposed)
        boolean renderNorth = !isMatchingOre(world.getBlockState(pos.north()));  // -Z direction
        boolean renderSouth = !isMatchingOre(world.getBlockState(pos.south()));  // +Z direction
        boolean renderWest  = !isMatchingOre(world.getBlockState(pos.west()));   // -X direction
        boolean renderEast  = !isMatchingOre(world.getBlockState(pos.east()));   // +X direction
        boolean renderUp    = !isMatchingOre(world.getBlockState(pos.above()));  // +Y direction
        boolean renderDown  = !isMatchingOre(world.getBlockState(pos.below()));  // -Y direction

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
     * Adds a single line segment to the buffer with calculated normals.
     */
    private void addLine(BufferBuilder buffer, Matrix4fc matrix, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        // Calculate line direction vector
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);

        // Normalize direction (use default if zero-length line)
        float nx = length > 0 ? dx / length : 0f;
        float ny = length > 0 ? dy / length : 1f;
        float nz = length > 0 ? dz / length : 0f;

        // Start vertex
        buffer.addVertex(matrix, x1, y1, z1)
                .setColor(r, g, b, a)
                .setNormal(nx, ny, nz)
                .setLineWidth(4.0f);

        // End vertex
        buffer.addVertex(matrix, x2, y2, z2)
                .setColor(r, g, b, a)
                .setNormal(nx, ny, nz)
                .setLineWidth(4.0f);
    }
    /**
     * Checks if a block state matches any of the target ore types.
     */
    private boolean isMatchingOre(BlockState state) {
        return blocksToCheckFor.contains(state.getBlock());
    }

    /**
     * Builds and draws the buffer to GPU.
     */
    private void drawLinesThroughWalls(Minecraft client, RenderPipeline pipeline) {
        // Build the mesh data from buffer
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        // Upload to GPU
        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

        // Execute draw call
        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        // Rotate vertex buffer to avoid GPU conflicts
        vertexBuffer.rotate();
        buffer = null; // Reset buffer for next frame
    }
    /**
     * Uploads vertex data to GPU buffer.
     */
    private GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        // Calculate required buffer size
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        // Initialize or resize vertex buffer as needed
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(
                    () -> HPotions.MOD_ID + " render pipeline",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    vertexBufferSize
            );
        }

        // Copy vertex data into GPU buffer
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
                vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()),
                false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }
    /**
     * Executes the actual GPU draw call.
     */
    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        // Handle index buffer based on vertex format mode
        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            // Sort quads for translucency
            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            // Use sequential index buffer for non-quad modes
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer =
                    RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        // Prepare transform uniforms
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        // Execute render pass
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> HPotions.MOD_ID + " render pass",
                        client.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        client.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty()
                )) {

            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    /**
     * Cleanup resources on mod shutdown.
     */
    public void close() {
        allocator.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
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

                        MutableComponent newLine = Component.literal(name + " (" + newDuration + ")");
                        newLine.setStyle(oldLine.getStyle());  // Copies color, formatting (BLUE)

                        // Replace
                        components.set(1, newLine);
                    }
                }
            }
        }
    }


}
