package net.dzultra;

import net.dzultra.block.ModBlockEntities;
import net.dzultra.block.SpawnPillarBlock.SpawnPillarBlockEntityRenderer;
import net.dzultra.networking.SyncTCBSpawnPillarBlockEntityS2CPayloadReceiver;
import net.fabricmc.api.ClientModInitializer;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class TrialChamberBossModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.SPAWN_PILLAR_BE, SpawnPillarBlockEntityRenderer::new);

        SyncTCBSpawnPillarBlockEntityS2CPayloadReceiver.registerReceiver();
    }
}
