package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public class BossSpawnAnimation {
    protected static final int maxSpawnTickTime = 220;

    // This function is only executed on the main BossSpawnPillarEntity which all the Spawning Logic executes
    // And we are fully Server Side
    // spawnTickCounter counts upwards 1 per Tick after the Spawning has begun
    protected static void tickBossSpawnAnimation(World world, BlockState state, BossSpawnPillarBlockEntity block0Entity, BlockPos pos, Integer spawnTickCounter) {
        BossSpawnPillarBlockEntity.spawnTickCounter++;
        //TrialChamberBossMod.LOGGER.info("[TCB] SpawnTickCounter: " + spawnTickCounter);

        if(!shouldContinueSpawning(world, block0Entity)) {
            resetSpawnPillars(world, pos);
            return;
        }

        if (spawnTickCounter <= 80) {
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2,
                        1, // count
                        0, 0, 0, // offset
                        0 // speed
                );
            }
        }
        if (spawnTickCounter == 160) {
            BossSpawnPillarBlockEntity.executingLogic = false;

            BlockPos block0Pos = block0Entity.getPos();
            BlockPos block1Pos = block0Pos.add(3, 0, 0);
            BlockPos block2Pos = block0Pos.add(0, 0, 3);
            BlockPos block3Pos = block0Pos.add(3, 0, 3);

            BlockEntity block1Entity = world.getBlockEntity(block1Pos);
            BlockEntity block2Entity = world.getBlockEntity(block2Pos);
            BlockEntity block3Entity = world.getBlockEntity(block3Pos);

            BossSpawnPillarBlockEntity.setBlockState(world, block0Pos, BossSpawnPillarBlock.ACTIVATED, false);
            BossSpawnPillarBlockEntity.setBlockState(world, block1Pos, BossSpawnPillarBlock.ACTIVATED, false);
            BossSpawnPillarBlockEntity.setBlockState(world, block2Pos, BossSpawnPillarBlock.ACTIVATED, false);
            BossSpawnPillarBlockEntity.setBlockState(world, block3Pos, BossSpawnPillarBlock.ACTIVATED, false);

            block0Entity.setStack(0, ItemStack.EMPTY);
            block0Entity.markDirty();
            block0Entity.syncInventory();

            if (block1Entity instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity1) {
                bossSpawnPillarBlockEntity1.setStack(0, ItemStack.EMPTY);
                bossSpawnPillarBlockEntity1.markDirty();
                bossSpawnPillarBlockEntity1.syncInventory();
            }
            if (block2Entity instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity2) {
                bossSpawnPillarBlockEntity2.setStack(0, ItemStack.EMPTY);
                bossSpawnPillarBlockEntity2.markDirty();
                bossSpawnPillarBlockEntity2.syncInventory();
            }
            if (block3Entity instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity3) {
                bossSpawnPillarBlockEntity3.setStack(0, ItemStack.EMPTY);
                bossSpawnPillarBlockEntity3.markDirty();
                bossSpawnPillarBlockEntity3.syncInventory();
            }
            spawnBossEntity(block0Entity.getPos(), world);
        }

        // I hate myself
        BlockPos pos1 = pos.add(3, 0 ,3);
        BlockPos pos2 = pos.add(3, 0 ,0);
        BlockPos pos3 = pos.add(0, 0 ,3);

        List<BlockPos> additionalBlocks = List.of(
                pos.add(0,-1,-2), pos.add(0,0,-2),
                pos.add(0,-1,-3), pos.add(0,0,-3),
                pos.add(-2,-2,0), pos.add(-2,-1,0),
                pos.add(-3,-2,0), pos.add(-3,-1,0),

                pos1.add(0,-1,2), pos1.add(0,0,2),
                pos1.add(0,-1,3), pos1.add(0,0,3),
                pos1.add(2,-2,0), pos1.add(2,-1,0),
                pos1.add(3,-2,0), pos1.add(3,-1,0),

                pos2.add(2,-1,0), pos2.add(2,0,0),
                pos2.add(3,-1,0), pos2.add(3,0,0),
                pos2.add(0,-2,-2), pos2.add(0,-1,-2),
                pos2.add(0,-2,-3), pos2.add(0,-1,-3),

                pos3.add(-2,-1,0), pos3.add(-2,0,0),
                pos3.add(-3,-1,0), pos3.add(-3,0,0),
                pos3.add(0,-2,2), pos3.add(0,-1,2),
                pos3.add(0,-2,3), pos3.add(0,-1,3)
        );
        // Finally done with this shit
        if (spawnTickCounter == 180) {
            shiftAreaDownByOne(world, block0Entity.getPos().add(1, 0, 1), additionalBlocks);
        }
        if (spawnTickCounter == 200) {
            shiftAreaDownByOne(world, block0Entity.getPos().add(1, 0, 1), additionalBlocks);
        }

        if(BossSpawnPillarBlockEntity.spawnTickCounter == maxSpawnTickTime) {
            BossSpawnPillarBlockEntity.spawnTickCounter = 0;
            BossSpawnPillarBlockEntity.setBlockState(world, block0Entity.getPos(), BossSpawnPillarBlock.HAS_STARTED_SPAWNED, false);
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

    // Helper methods
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
}
