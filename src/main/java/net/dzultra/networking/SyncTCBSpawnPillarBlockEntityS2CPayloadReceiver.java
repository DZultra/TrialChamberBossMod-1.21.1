package net.dzultra.networking;

import net.dzultra.block.SpawnPillarBlock.SpawnPillarBlockEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;

public class SyncTCBSpawnPillarBlockEntityS2CPayloadReceiver {
    public static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(SyncTCBSpawnPillarBlockEntityS2CPayload.ID, (payload, context) -> {
            ClientWorld world = context.client().world;

            if (world == null) return;

            // Retrieve the BlockEntity at the specified BlockPos
            BlockEntity blockEntity = world.getBlockEntity(payload.blockPos());
            if (blockEntity instanceof SpawnPillarBlockEntity spawnPillarBlockEntity) {
                // Update the BlockEntity's inventory with the payload data
                spawnPillarBlockEntity.setStack(0, payload.inventory().getFirst());

                int xItemRenderSign = 1;
                int zItemRenderSign = 1;

                switch (payload.itemRenderSign()) {
                    case 1:
                        xItemRenderSign = -1;
                        break;
                    case 2:
                        zItemRenderSign = -1;
                        break;
                    case 3:
                        xItemRenderSign = -1;
                        zItemRenderSign = -1;
                        break;
                }

                spawnPillarBlockEntity.setX_render_sign(xItemRenderSign);
                spawnPillarBlockEntity.setZ_render_sign(zItemRenderSign);

                spawnPillarBlockEntity.setXItemRenderOffset(payload.itemRenderOffset().x());
                spawnPillarBlockEntity.setYItemRenderOffset(payload.itemRenderOffset().y());
                spawnPillarBlockEntity.setZItemRenderOffset(payload.itemRenderOffset().z());

                spawnPillarBlockEntity.setSpawnTickCounter(payload.spawnTickCounter());

                world.updateListeners(payload.blockPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), Block.NOTIFY_ALL);
            }
        });
    }
}
