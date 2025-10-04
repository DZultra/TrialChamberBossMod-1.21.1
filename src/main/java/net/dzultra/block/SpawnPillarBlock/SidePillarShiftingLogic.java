package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class SidePillarShiftingLogic {
    public static void shiftPillarDown(ServerWorld world, SpawnPillarBlockEntity blockEntity ,List<BlockPos> origins , int spawnTickCounter, int sidePillarDownShiftStart) {
        int ticksSinceStart = spawnTickCounter - sidePillarDownShiftStart;

        if (ticksSinceStart % 10 == 0) {
            for (BlockPos origin : origins) {
                for (int x = 0; x < 2; x++) {
                    for (int z = 0; z < 2; z++) {
                        shiftBlockDown(world, origin.add(x, blockEntity.getSide_pillar_y(), z));
                    }
                }
            }
            blockEntity.decrementSide_pillar_y();
            if (blockEntity.getSide_pillar_y() == -5) {
                blockEntity.setSide_pillar_y(0);
            }
        }
    }

    private static void shiftBlockDown(ServerWorld world, BlockPos origin) {
        var state = world.getBlockState(origin);
        world.removeBlock(origin, false);
        world.setBlockState(origin.down(), state);
    }
}
