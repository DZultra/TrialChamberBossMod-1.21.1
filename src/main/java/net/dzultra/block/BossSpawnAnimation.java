package net.dzultra.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BossSpawnAnimation {
    protected static final int maxSpawnTickTime = 80;

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

        if (spawnTickCounter == maxSpawnTickTime) {
            spawnBossEntity(block0Entity.getPos(), world);
            BossSpawnPillarBlockEntity.spawnTickCounter = 0;
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
            BossSpawnPillarBlockEntity.setBlockState(world, block0Pos, BossSpawnPillarBlock.HAS_STARTED_SPAWNED, false);

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
