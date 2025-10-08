package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ChainPillarShiftingLogic {
    private static List<BlockPos> shapeBlocks = new ArrayList<>();

    public static void shiftChainPillar(ServerWorld world, SpawnPillarBlockEntity blockEntity, List<BlockPos> blockPosList, int spawnTickCounter, int chainPillarShiftStart) {
        shapeBlocks.clear();

        int ticksSinceStart = spawnTickCounter - chainPillarShiftStart;

        if (ticksSinceStart % 5 != 0) return;

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 2; z++) {
                    for (int i = 0; i < 4; i++) {
                        shapeBlocks.add(blockPosList.get(i).add(x, y + blockEntity.getChain_pillar_offset(), z));
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

        blockEntity.incrementChain_pillar_offset();

        if (blockEntity.getChain_pillar_offset() == 6) {
            blockEntity.setChain_pillar_offset(0);
        }
    }
}

