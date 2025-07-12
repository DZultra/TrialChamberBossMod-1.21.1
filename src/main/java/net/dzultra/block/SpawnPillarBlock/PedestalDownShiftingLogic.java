package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PedestalDownShiftingLogic {
    protected static void shiftPedestalDown(ServerWorld world, BlockEntity blockEntity) {
        BlockPos pos = blockEntity.getPos();

        // I hate myself
        BlockPos pos1 = pos.add(3, 0 ,3);
        BlockPos pos2 = pos.add(3, 0 ,0);
        BlockPos pos3 = pos.add(0, 0 ,3);
        List<BlockPos> additionalBlocks = List.of(pos.add(0,-1,-2), pos.add(0,0,-2), pos.add(0,-1,-3), pos.add(0,0,-3), pos.add(-2,-2,0), pos.add(-2,-1,0), pos.add(-3,-2,0), pos.add(-3,-1,0), pos1.add(0,-1,2), pos1.add(0,0,2), pos1.add(0,-1,3), pos1.add(0,0,3), pos1.add(2,-2,0), pos1.add(2,-1,0), pos1.add(3,-2,0), pos1.add(3,-1,0), pos2.add(2,-1,0), pos2.add(2,0,0), pos2.add(3,-1,0), pos2.add(3,0,0), pos2.add(0,-2,-2), pos2.add(0,-1,-2), pos2.add(0,-2,-3), pos2.add(0,-1,-3), pos3.add(-2,-1,0), pos3.add(-2,0,0), pos3.add(-3,-1,0), pos3.add(-3,0,0), pos3.add(0,-2,2), pos3.add(0,-1,2), pos3.add(0,-2,3), pos3.add(0,-1,3));

        shiftAreaDownByOne(world, blockEntity.getPos(), additionalBlocks);
    }

    public static void shiftAreaDownByOne(ServerWorld world, BlockPos origin, List<BlockPos> additionalBlocks) {
        origin = origin.add(1,0,1);

        // Define area boundaries
        int minX = origin.getX() - 2;
        int maxX = origin.getX() + 3;
        int minZ = origin.getZ() - 2;
        int maxZ = origin.getZ() + 3;
        int minY = origin.getY() - 3;
        int maxY = origin.getY();

        // Temporary storage for block data
        Map<BlockPos, Pair<BlockState, NbtCompound>> blockData = new HashMap<>();

        // Step 1: Handle blocks that would be moved out of bounds (bottom layer)
        // For area blocks
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos bottomPos = new BlockPos(x, minY, z);
                handleBottomLayerBlock(world, bottomPos);
            }
        }
        // For additional blocks at bottom layer
        additionalBlocks.stream()
                .filter(pos -> pos.getY() == minY)
                .forEach(pos -> handleBottomLayerBlock(world, pos));

        // Step 2: Collect all block data from remaining layers
        // For area blocks
        for (int y = minY + 1; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    storeBlockData(world, blockData, pos);
                }
            }
        }
        // For additional blocks
        additionalBlocks.stream()
                .filter(pos -> pos.getY() > minY && pos.getY() <= maxY)
                .forEach(pos -> storeBlockData(world, blockData, pos));

        // Step 3: Shift everything down from top to bottom
        // For area blocks
        for (int y = maxY; y > minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    BlockPos belowPos = currentPos.down();
                    shiftSingleBlock(world, blockData, currentPos, belowPos);
                }
            }
        }
        // For additional blocks
        additionalBlocks.stream()
                .filter(pos -> pos.getY() > minY && pos.getY() <= maxY)
                .sorted((a, b) -> Integer.compare(b.getY(), a.getY())) // Process from top to bottom
                .forEach(pos -> {
                    BlockPos belowPos = pos.down();
                    shiftSingleBlock(world, blockData, pos, belowPos);
                });

        // Step 4: Clear topmost layer
        // For area blocks
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos topPos = new BlockPos(x, maxY, z);
                handleTopLayerBlock(world, topPos);
            }
        }
        // For additional blocks at top layer
        additionalBlocks.stream()
                .filter(pos -> pos.getY() == maxY)
                .forEach(pos -> handleTopLayerBlock(world, pos));
    }

    private static void handleBottomLayerBlock(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof SpawnPillarBlockEntity) {
            world.removeBlockEntity(pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }
        else if (blockEntity instanceof Inventory) {
            world.breakBlock(pos, true);
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            if (blockEntity != null) {
                world.removeBlockEntity(pos);
            }
        }
    }

    private static void storeBlockData(ServerWorld world, Map<BlockPos, Pair<BlockState, NbtCompound>> blockData, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        NbtCompound nbt = null;
        if (blockEntity != null) {
            nbt = blockEntity.createNbtWithIdentifyingData(world.getRegistryManager());
            world.removeBlockEntity(pos);
        }

        blockData.put(pos, new Pair<>(state, nbt));
    }

    private static void shiftSingleBlock(ServerWorld world, Map<BlockPos, Pair<BlockState, NbtCompound>> blockData, BlockPos currentPos, BlockPos belowPos) {
        Pair<BlockState, NbtCompound> data = blockData.get(currentPos);
        if (data == null) return;

        world.setBlockState(belowPos, data.getLeft(), Block.NOTIFY_ALL);

        if (data.getRight() != null) {
            BlockEntity newEntity = BlockEntity.createFromNbt(belowPos, data.getLeft(), data.getRight(), world.getRegistryManager());
            if (newEntity != null) {
                world.addBlockEntity(newEntity);
            }
        }
    }

    private static void handleTopLayerBlock(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof SpawnPillarBlockEntity) {
            world.removeBlockEntity(pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        }
        else if (blockEntity instanceof Inventory) {
            world.breakBlock(pos, true);
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            if (blockEntity != null) {
                world.removeBlockEntity(pos);
            }
        }
    }
}
