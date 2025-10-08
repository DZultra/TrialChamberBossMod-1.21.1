package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Comparator;
import java.util.List;

public class PedestalRodShiftingLogic {

    public static void shiftPedestalRods(
            ServerWorld world,
            SpawnPillarBlockEntity blockEntity,
            List<BlockPos> positiveXBlocks,
            List<BlockPos> negativeXBlocks,
            List<BlockPos> positiveZBlocks,
            List<BlockPos> negativeZBlocks,
            int spawnTickCounter,
            int pedestalRodShiftStart
    ) {
        int ticksSinceStart = spawnTickCounter - pedestalRodShiftStart;

        // Shift every 5 ticks
        if (ticksSinceStart % 5 != 0) return;

        // Sort all directions properly to avoid overwriting blocks
        positiveXBlocks.sort(Comparator.comparingInt(BlockPos::getX).reversed());
        negativeXBlocks.sort(Comparator.comparingInt(BlockPos::getX));
        positiveZBlocks.sort(Comparator.comparingInt(BlockPos::getZ).reversed());
        negativeZBlocks.sort(Comparator.comparingInt(BlockPos::getZ));

        int offset = blockEntity.getPedestal_rods_offset();

        // Shift in each direction
        shiftBlocks(world, positiveXBlocks, Direction.EAST, offset, 0);
        shiftBlocks(world, negativeXBlocks, Direction.WEST, -offset, 0);
        shiftBlocks(world, positiveZBlocks, Direction.SOUTH, 0, offset);
        shiftBlocks(world, negativeZBlocks, Direction.NORTH, 0, -offset);

        // Update offset
        blockEntity.incrementPedestal_rods_offset();
        if (blockEntity.getPedestal_rods_offset() >= 5) {
            blockEntity.setPedestal_rods_offset(0);
        }
    }

    private static void shiftBlocks(ServerWorld world, List<BlockPos> positions, Direction direction, int xOffset, int zOffset) {
        for (BlockPos pos : positions) {
            BlockPos shiftedPos = pos.add(xOffset, 0, zOffset);
            shiftBlock(world, shiftedPos, direction);
        }
    }

    private static void shiftBlock(ServerWorld world, BlockPos pos, Direction direction) {
        var state = world.getBlockState(pos);
        if (state.isAir()) return;

        world.removeBlock(pos, false);
        world.setBlockState(pos.offset(direction), state);
    }
}
