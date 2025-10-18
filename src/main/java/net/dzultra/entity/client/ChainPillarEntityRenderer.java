package net.dzultra.entity.client;

import net.dzultra.entity.custom.ChainPillarEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class ChainPillarEntityRenderer extends EntityRenderer<ChainPillarEntity>  {

    public ChainPillarEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(ChainPillarEntity entity) {
        return null;
    }
}
