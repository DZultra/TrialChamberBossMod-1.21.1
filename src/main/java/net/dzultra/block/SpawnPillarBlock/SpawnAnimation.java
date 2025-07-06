package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnAnimation {
    protected static boolean shouldRenderBeams = false;

    protected static void tickBossSpawnAnimation(World world, BlockState state, SpawnPillarBlockEntity spawnPillarBlockEntity, BlockPos pos) {
        // Server Side
        int spawnTickCounter = spawnPillarBlockEntity.getAndIncrementSpawnTickCounter();

    }
}
