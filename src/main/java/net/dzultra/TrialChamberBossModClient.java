package net.dzultra;

import net.dzultra.block.BossSpawnPillarBlockEntityRenderer;
import net.dzultra.block.ModBlockEntities;
import net.dzultra.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class TrialChamberBossModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModEntities.registerModEntities();
        BlockEntityRendererFactories.register(ModBlockEntities.SPAWN_PILLAR_BE, BossSpawnPillarBlockEntityRenderer::new);
    }
}
