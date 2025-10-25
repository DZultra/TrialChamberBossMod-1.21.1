package net.dzultra.entity.client;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.entity.custom.ChainPillarEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

// Made with Blockbench 5.0.2
public class ChainPillarEntityModel<T extends ChainPillarEntity> extends EntityModel<T> {
	public static final EntityModelLayer CHAIN_PILLAR = new EntityModelLayer(Identifier.of(TrialChamberBossMod.MOD_ID, "bb_main"), "main");
	private final ModelPart bb_main;
	public ChainPillarEntityModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData bb_main = modelPartData.addChild("bb_main", ModelPartBuilder.create().uv(0, 0).cuboid(-32.0F, -64.0F, 0.0F, 32.0F, 64.0F, 32.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 24.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}
	@Override
	public void setAngles(ChainPillarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}
	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
		bb_main.render(matrices, vertexConsumer, light, overlay);
	}
}