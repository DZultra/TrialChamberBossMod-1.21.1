package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
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

public class BossSpawnPillarBlockEntityRenderer implements BlockEntityRenderer<BossSpawnPillarBlockEntity> {
    private static final Identifier BEAM_TEXTURE = Identifier.ofVanilla("textures/entity/end_gateway_beam.png");

    public BossSpawnPillarBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(BossSpawnPillarBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemStack stack = entity.getStack(0);
        BlockPos entity_pos = entity.getPos();
        matrices.push();
        matrices.translate(0.5f, 1.5f, 0.5f); // Position
        matrices.scale(0.5f, 0.5f, 0.5f); // Size
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getRenderingRotation())); // Rotation

        itemRenderer.renderItem(stack, ModelTransformationMode.GUI, getLightLevel(entity.getWorld(),
                entity_pos), OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, entity.getWorld(), 1);
        matrices.pop();

        if (entity.spawnTickCounter >= 200
                && entity.spawnTickCounter <= 300
                && entity.getWorld().getBlockState(entity.getPos()) == BossSpawnPillarBlockEntity.getBlockState(entity.getWorld(), entity.getPos(), BossSpawnPillarBlock.HAS_STARTED_SPAWNED, true)) {
            matrices.translate(0f, 0f, 0f); // Position
            renderBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos());

            matrices.translate(3f, 0f, 0f); // Position
            renderBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos().add(3, 0, 0));

            matrices.translate(0f, 0f, 3f); // Position
            renderBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos().add(3, 0, 3));

            matrices.translate(-3f, 0f, 0f); // Position
            renderBeam(matrices, vertexConsumers, tickDelta, entity, entity.getPos().add(0, 0, 3));

        }
    }

    private void renderBeam(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta, BlockEntity entity, BlockPos pos) {
        BeaconBlockEntityRenderer.renderBeam(
                matrices, vertexConsumers, BEAM_TEXTURE, tickDelta,
                1, // Height Scale
                entity.getWorld().getTime(),
                0, // YOffset
                getFirstNonAirBlockAboveY(pos, entity.getWorld()), // maxY
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
