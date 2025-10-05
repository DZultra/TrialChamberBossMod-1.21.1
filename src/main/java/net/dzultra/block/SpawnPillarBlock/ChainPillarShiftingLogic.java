package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ChainPillarShiftingLogic {
    private static List<BlockPos> shapeBlocks = new ArrayList<>();

    public static void shiftChainPillar(ServerWorld world, List<BlockPos> blockPosList, int spawnTickCounter) {
        shapeBlocks.clear();

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 2; z++) {
                    for (int i = 0; i < 4; i++) {
                        shapeBlocks.add(blockPosList.get(i).add(x, y, z));
                    }
                }
            }
        }

        List<BlockPos> newPositions = new ArrayList<>();

        // Save states first
        List<BlockState> states = new ArrayList<>();
        for (BlockPos pos : shapeBlocks) {
            states.add(world.getBlockState(pos));
            world.removeBlock(pos, false);
        }

        // Place blocks 1 higher
        for (int i = 0; i < shapeBlocks.size(); i++) {
            BlockPos oldPos = shapeBlocks.get(i);
            BlockPos newPos = oldPos.up();
            world.setBlockState(newPos, states.get(i));
            newPositions.add(newPos);
        }

        // Update shape list
        shapeBlocks.clear();
        shapeBlocks.addAll(newPositions);

    }
}

