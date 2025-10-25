package net.dzultra.entity.client;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.TrialChamberBossModClient;
import net.dzultra.entity.custom.ChainPillarEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

public class ChainPillarEntityRenderer extends EntityRenderer<ChainPillarEntity>  {
    private static final Identifier TEXTURE = Identifier.of(TrialChamberBossMod.MOD_ID, "textures/entity/chain_pillar_entity_texture.png");
    private static final RenderLayer CHAIN_PILLAR = RenderLayer.getEntityCutoutNoCull(TEXTURE);
    private final ModelPart entityModel;

    public ChainPillarEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        ModelPart modelPart = ctx.getPart(ChainPillarEntityModel.CHAIN_PILLAR);
        this.entityModel = modelPart.getChild("bb_main");
    }

    @Override
    public Identifier getTexture(ChainPillarEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ChainPillarEntity entity, float f, float g, MatrixStack matrixStack,
                       VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.push();
        matrixStack.translate(1, 1.5, 1);
        matrixStack.multiply(new Quaternionf().rotateX((float) Math.PI));
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(CHAIN_PILLAR);
        entityModel.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV);
        matrixStack.pop();
        super.render(entity, f, g, matrixStack, vertexConsumerProvider, i);
    }

}
