package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class SpawnPillarBlockEntityRenderer implements BlockEntityRenderer<SpawnPillarBlockEntity> {
    private static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/end_gateway_beam.png");

    public SpawnPillarBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(SpawnPillarBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        renderItemAboveBlock(matrices, vertexConsumers, entity);
        if (entity.getCachedState().get(SpawnPillarBlock.SHOULD_RENDER_BEAM)) {
            renderAllBeams(matrices, vertexConsumers, entity, tickDelta);
        }
    }

    private void renderItemAboveBlock(MatrixStack matrices, VertexConsumerProvider vertexConsumers, SpawnPillarBlockEntity entity) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack stack = entity.getStack(0);

        float x_item_offset = entity.getXItemRenderOffset() * entity.getX_render_sign();
        float y_item_offset = entity.getYItemRenderOffset();
        float z_item_offset = entity.getZItemRenderOffset() * entity.getZ_render_sign();

        matrices.push();
        matrices.translate(0.5f + x_item_offset, 1.5f + y_item_offset, 0.5f + z_item_offset); // Position
        matrices.scale(0.5f, 0.5f, 0.5f); // Size
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRenderingRotation())); // Rotation

        if (entity.getWorld() != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            OutlineVertexConsumerProvider outlineProvider = client.getBufferBuilders().getOutlineVertexConsumers();

            int ticks = entity.getSpawnTickCounter();

            // Blink orange between 290–340
            if (ticks >= 290 && ticks <= 340) {
                // Every 3 ticks
                boolean blinkOn = ((ticks / 3) % 2) == 0;

                if (blinkOn) {
                    outlineProvider.setColor(255, 255, 255, 0); // orange outline
                    renderItem(itemRenderer, stack, matrices, outlineProvider, entity);
                } else {
                    renderItem(itemRenderer, stack, matrices, vertexConsumers, entity);
                }
            } else if (ticks > 340) {
                outlineProvider.setColor(255, 255, 255, 0);
                renderItem(itemRenderer, stack, matrices, outlineProvider, entity);
            } else {
                renderItem(itemRenderer, stack, matrices, vertexConsumers, entity);
            }
        }
        matrices.pop();
    }

    private void renderItem(ItemRenderer itemRenderer, ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, SpawnPillarBlockEntity entity) {
        if (entity.getWorld() == null) return;
        itemRenderer.renderItem(
                stack,
                ModelTransformationMode.GUI,
                getLightLevel(entity.getWorld(), entity.getPos()),
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                1
        );
    }

    private void renderAllBeams(MatrixStack matrices, VertexConsumerProvider vertexConsumers, SpawnPillarBlockEntity entity, float tickDelta) {
        matrices.push();
        matrices.translate(0f, 0f, 0f);
        renderSingleBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos());
        matrices.pop();

        matrices.push();
        matrices.translate(3f, 0f, 0f);
        renderSingleBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos().add(3, 0, 0));
        matrices.pop();

        matrices.push();
        matrices.translate(0f, 0f, 3f);
        renderSingleBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos().add(3, 0, 3));
        matrices.pop();

        matrices.push();
        matrices.translate(3f, 0f, 3f);
        renderSingleBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos().add(0, 0, 3));
        matrices.pop();
    }

    private void renderSingleBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, BlockEntity entity, BlockPos pos) {
        if (entity.getWorld() == null) return;

        int maxY = getFirstNonAirBlockAboveY(pos, entity.getWorld());
        //TrialChamberBossMod.LOGGER.info("MaxY: {}", maxY);
        BeaconBlockEntityRenderer.renderBeam(
                matrices, vertexConsumers, BEAM_TEXTURE, tickDelta,
                1, // Height Scale
                entity.getWorld().getTime(),
                0, // YOffset
                maxY,
                DyeColor.ORANGE.getEntityColor(),
                0.15F, // Inner Radius
                0F // Outer Radius
        );
    }

    public int getFirstNonAirBlockAboveY(BlockPos pos, World world) {
        int x = pos.getX();
        int z = pos.getZ();
        int y = pos.getY() + 1;

        // Stop at world height limit to prevent overflow
        while (y < world.getTopY()) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (!world.getBlockState(checkPos).isAir()) {

                return y - pos.getY();
            }
            y++;
        }

        // Return -1 if no non-air block was found
        return -1;
    }

    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}
