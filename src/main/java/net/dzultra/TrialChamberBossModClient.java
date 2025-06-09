package net.dzultra;

import net.dzultra.block.BossSpawnPillarBlockEntity;
import net.dzultra.block.BossSpawnPillarBlockEntityRenderer;
import net.dzultra.block.ModBlockEntities;
import net.dzultra.networking.SyncTCBSpawnPillarBlockEntityS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.world.ClientWorld;

public class TrialChamberBossModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.SPAWN_PILLAR_BE, BossSpawnPillarBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(SyncTCBSpawnPillarBlockEntityS2CPayload.ID, (payload, context) -> {
            ClientWorld world = context.client().world;

            if (world == null) {
                return; // Ensure the world is not null
            }

            // Retrieve the BlockEntity at the specified BlockPos
            BlockEntity blockEntity = world.getBlockEntity(payload.blockPos());
            if (blockEntity instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
                // Update the BlockEntity's inventory with the payload data
                bossSpawnPillarBlockEntity.setStack(0, payload.inventory().getFirst());

                // Mark the BlockEntity for rerendering (optional, if needed)
                world.updateListeners(payload.blockPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_ALL);
            }
        });
    }
}
