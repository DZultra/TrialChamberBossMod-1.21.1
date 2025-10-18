package net.dzultra;

import net.dzultra.block.ModBlockEntities;
import net.dzultra.block.ModBlocks;
import net.dzultra.block.SpawnPillarBlock.SpawnPillarBlockEntityRenderer;
import net.dzultra.entity.ModEntities;
import net.dzultra.entity.client.ChainPillarEntityRenderer;
import net.dzultra.networking.SyncTCBSpawnPillarBlockEntityS2CPayloadReceiver;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class TrialChamberBossModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.SPAWN_PILLAR_BE, SpawnPillarBlockEntityRenderer::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.REINFORCED_CHAIN, RenderLayer.getCutout());

        SyncTCBSpawnPillarBlockEntityS2CPayloadReceiver.registerReceiver();

        EntityRendererRegistry.register(ModEntities.CHAIN_PILLAR_ENTITY, ChainPillarEntityRenderer::new);
    }
}
