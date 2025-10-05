package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class PedestalRodShiftingLogic {
    public static void shiftPedestalRods(ServerWorld world, SpawnPillarBlockEntity blockEntity, ArrayList<BlockPos> positiveXBlocks, ArrayList<BlockPos> negativeXBlocks, ArrayList<BlockPos> positiveZBlocks, ArrayList<BlockPos> negativeZBlocks, int spawnTickCounter, int pedestalRodShiftStart) {
        int ticksSinceStart = spawnTickCounter - pedestalRodShiftStart;

        // Sort so you don't override blocks which you want to shift after
        positiveXBlocks.sort((a, b) -> Integer.compare(b.getX(), a.getX()));
        negativeXBlocks.sort((a, b) -> Integer.compare(a.getX(), b.getX()));
        positiveZBlocks.sort((a, b) -> Integer.compare(b.getZ(), a.getZ()));
        negativeZBlocks.sort((a, b) -> Integer.compare(a.getZ(), b.getZ()));

        // Shift every 5 ticks
        if (ticksSinceStart % 5 == 0) {
            for (BlockPos pos : positiveXBlocks) {
                shiftBlockPositiveX(world, pos.add(blockEntity.getPedestal_rods_offset(), 0, 0));
            }
            for (BlockPos pos : negativeXBlocks) {
                shiftBlockNegativeX(world, pos.add(-blockEntity.getPedestal_rods_offset(), 0, 0));
            }
            for (BlockPos pos : positiveZBlocks) {
                shiftBlockPositiveZ(world, pos.add(0, 0, blockEntity.getPedestal_rods_offset()));
            }
            for (BlockPos pos : negativeZBlocks) {
                shiftBlockNegativeZ(world, pos.add(0, 0, -blockEntity.getPedestal_rods_offset()));
            }

            blockEntity.incrementPedestal_rods_offset();

            if (blockEntity.getPedestal_rods_offset() == 5) {
                blockEntity.setPedestal_rods_offset(0);
            }
        }
    }

    private static void shiftBlockPositiveX(ServerWorld world, BlockPos pos) {
        var state = world.getBlockState(pos);
        if (state.isAir()) return;
        world.removeBlock(pos, false);
        world.setBlockState(pos.east(), state);
    }

    private static void shiftBlockNegativeX(ServerWorld world, BlockPos pos) {
        var state = world.getBlockState(pos);
        if (state.isAir()) return;
        world.removeBlock(pos, false);
        world.setBlockState(pos.west(), state);
    }

    private static void shiftBlockPositiveZ(ServerWorld world, BlockPos pos) {
        var state = world.getBlockState(pos);
        if (state.isAir()) return;
        world.removeBlock(pos, false);
        world.setBlockState(pos.south(), state);
    }

    private static void shiftBlockNegativeZ(ServerWorld world, BlockPos pos) {
        var state = world.getBlockState(pos);
        if (state.isAir()) return;
        world.removeBlock(pos, false);
        world.setBlockState(pos.north(), state);
    }
}
