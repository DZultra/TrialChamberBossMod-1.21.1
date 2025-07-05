package net.dzultra.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class BossSpawnAnimation {
    /*
    protected static final int maxSpawnTickTime = 400;
    protected static boolean shouldRenderBeams = false;
    // This function is only executed on the main BossSpawnPillarEntity which all the Spawning Logic executes
    // And we are fully Server Side
    // spawnTickCounter counts upwards 1 per Tick after the Spawning has begun

    private static void spawnParticleRays(ServerWorld world, BlockPos pos0, Integer spawnTickCounter) {
        Position pos = new Position() {
            @Override
            public double getX() {
                return pos0.getX() + 0.5;
            }

            @Override
            public double getY() {
                return pos0.getY() + 1.5;
            }

            @Override
            public double getZ() {
                return pos0.getZ() + 0.5;
            }
        };

        spawnParticle(world, spawnTickCounter, pos,  80, 0.0, 0.0, 0.0);
        spawnParticle(world, spawnTickCounter, pos,  85, 0.1, 0.1, 0.1);
        spawnParticle(world, spawnTickCounter, pos,  90, 0.2, 0.2, 0.2);
        spawnParticle(world, spawnTickCounter, pos,  95, 0.3, 0.3, 0.3);
        spawnParticle(world, spawnTickCounter, pos, 100, 0.4, 0.4, 0.4);
        spawnParticle(world, spawnTickCounter, pos, 105, 0.5, 0.5, 0.5);
        spawnParticle(world, spawnTickCounter, pos, 110, 0.6, 0.6, 0.6);
        spawnParticle(world, spawnTickCounter, pos, 115, 0.7, 0.7, 0.7);
        spawnParticle(world, spawnTickCounter, pos, 120, 0.8, 0.8, 0.8);
        spawnParticle(world, spawnTickCounter, pos, 125, 0.9, 0.9, 0.9);
        spawnParticle(world, spawnTickCounter, pos, 130, 1.0, 1.0, 1.0);
        spawnParticle(world, spawnTickCounter, pos, 135, 1.1, 1.1, 1.1);
        spawnParticle(world, spawnTickCounter, pos, 140, 1.2, 1.2, 1.2);
        spawnParticle(world, spawnTickCounter, pos, 145, 1.3, 1.3, 1.3);
        spawnParticle(world, spawnTickCounter, pos, 150, 1.4, 1.4, 1.4);
        spawnParticle(world, spawnTickCounter, pos, 155, 1.5, 1.5, 1.5);
    }

    private static void spawnParticle(ServerWorld world, Integer spawnTickCounter, Position pos, Integer threshold, double x_offset, double y_offset, double z_offset) {
        if (spawnTickCounter >= threshold) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + x_offset, pos.getY() + y_offset, pos.getZ() + z_offset,
                    1, // count
                    0, 0, 0, // offset
                    0 // speed
            );
        }
    }

    public static void shiftAreaDownByOne(World world, BlockPos origin, List<BlockPos> additionalBlocks) {
        if (world.isClient()) return;

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

    private static void handleBottomLayerBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof BossSpawnPillarBlockEntity) {
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

    private static void storeBlockData(World world, Map<BlockPos, Pair<BlockState, NbtCompound>> blockData, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        NbtCompound nbt = null;
        if (blockEntity != null) {
            nbt = blockEntity.createNbtWithIdentifyingData(world.getRegistryManager());
            world.removeBlockEntity(pos);
        }

        blockData.put(pos, new Pair<>(state, nbt));
    }

    private static void shiftSingleBlock(World world, Map<BlockPos, Pair<BlockState, NbtCompound>> blockData, BlockPos currentPos, BlockPos belowPos) {
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

    private static void handleTopLayerBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof BossSpawnPillarBlockEntity) {
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

    protected static boolean shouldContinueSpawning(World world, BlockEntity blockEntity) {
        BlockPos block0Pos = blockEntity.getPos();
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        BlockState block0 = world.getBlockState(block0Pos);
        BlockState block1 = world.getBlockState(block1Pos);
        BlockState block2 = world.getBlockState(block2Pos);
        BlockState block3 = world.getBlockState(block3Pos);

        // Are the needed Blocks "Spawn Pillars"?
        return block0.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
                && block1.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
                && block2.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
                && block3.isOf(ModBlocks.BOSS_SPAWN_PILLAR);
    }

    protected static void resetSpawnPillars(World world, BlockPos pos) {
        BlockPos block1Pos = pos.add(3, 0, 0);
        BlockPos block2Pos = pos.add(0, 0, 3);
        BlockPos block3Pos = pos.add(3, 0, 3);

        trySetBlockState(world, pos, BossSpawnPillarBlock.LOCKED);
        trySetBlockState(world, block1Pos, BossSpawnPillarBlock.LOCKED);
        trySetBlockState(world, block2Pos, BossSpawnPillarBlock.LOCKED);
        trySetBlockState(world, block3Pos, BossSpawnPillarBlock.LOCKED);
        trySetBlockState(world, pos, BossSpawnPillarBlock.HAS_STARTED_SPAWNED);

    }

    private static void trySetBlockState(World world, BlockPos pos, Property<Boolean> property) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof BossSpawnPillarBlockEntity pillarBE && world.getBlockState(pos).getBlock() != Blocks.AIR) {
            BossSpawnPillarBlockEntity.setBlockState(world, pos, property, false);
        }
    }

    private static void spawnBossEntity(BlockPos pos, World world){
        BlockPos spawnPos = BossSpawnPillarBlockEntity.getBossSpawnPos(pos);
        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
        creeper.refreshPositionAndAngles(Vec3d.of(spawnPos), 360F, 0);
        world.spawnEntity(creeper);
    }

     */
}
