package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SidePillarShiftingLogic {
    private static int y = 0;

    public static void shiftPillarDown(ServerWorld world, List<BlockPos> origins , int spawnTickCounter, int sidePillarDownShiftStart) {
        int ticksSinceStart = spawnTickCounter - sidePillarDownShiftStart;

        if (ticksSinceStart % 10 == 0) {
            for (BlockPos origin : origins) {
                for (int x = 0; x < 2; x++) {
                    for (int z = 0; z < 2; z++) {
                        shiftBlockDown(world, origin.add(x, y, z));
                    }
                }
            }
            y--;
            if (y == -5) {
                y = 0;
            }
        }
    }

    private static void shiftBlockDown(ServerWorld world, BlockPos origin) {
        var state = world.getBlockState(origin);
        world.removeBlock(origin, false);
        world.setBlockState(origin.down(), state);
    }
}
