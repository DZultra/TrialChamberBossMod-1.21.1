package net.dzultra.block.SpawnPillarBlock;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.block.BossSpawnPillarBlock;
import net.dzultra.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class SpawnPillarLogic {
    private static int particleTickCounter = 0; // TickCounter for Particle Spawning above Block when it is Activated

    // Tick Function from SpawnPillarBlock, just in a separate class
    protected static void tick(World world, BlockPos pos, BlockState state) { // Executed on Client & Server Side
        if (!(world.getBlockEntity(pos) instanceof SpawnPillarBlockEntity spawnPillarBlockEntity)) return;

        // SpawnParticles for better display of the Item on the SpawnPillar
        if (!spawnPillarBlockEntity.isEmpty() && !world.isClient()) {
            spawnParticleAbovePillar((ServerWorld) world, pos);
        }

        // Once the startBossSpawn() was executed the BlockState "running_logic" is set true
        // This starts the SpawnAnimation
        if (state.get(SpawnPillarBlock.RUNNING_LOGIC) && !world.isClient()) { //
            SpawnAnimation.tickBossSpawnAnimation(world, state, spawnPillarBlockEntity, spawnPillarBlockEntity.getPos());
        }

        // Check whether the Boss Spawning should start and do so if it should
        if(shouldStartBossSpawn(world, state, spawnPillarBlockEntity) && !world.isClient()){
            startBossSpawn(world, spawnPillarBlockEntity, spawnPillarBlockEntity.getPos());
        }
    }

    // -- Logic Functions --

    // Checks whether all needed positions are an ACTIVATED Spawn Pillar
    private static boolean shouldStartBossSpawn(World world, BlockState state, SpawnPillarBlockEntity spawnPillarBlockEntity) {
        if (world.isClient()) return false;
        // Server Side
        BlockPos block0Pos = spawnPillarBlockEntity.getPos();
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        BlockState block0 = world.getBlockState(block0Pos);
        BlockState block1 = world.getBlockState(block1Pos);
        BlockState block2 = world.getBlockState(block2Pos);
        BlockState block3 = world.getBlockState(block3Pos);

        // Are the needed Blocks "Spawn Pillars"? If so, continue
        if (!block1.isOf(ModBlocks.SPAWN_PILLAR)
                || !block2.isOf(ModBlocks.SPAWN_PILLAR)
                || !block3.isOf(ModBlocks.SPAWN_PILLAR)
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't Spawn Pillars: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        // Did the spawning already begin? If so, don't start spawning again
        if(state == isBlockState(world, block0Pos, SpawnPillarBlock.RUNNING_LOGIC, true)) {
            //TrialChamberBossMod.LOGGER.info("[TCB] hasStartedSpawning: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        // Are the Spawn Pillars ACTIVATED aka have a Spawn Shard on them? If so, continue and return true
        if((block0 == isBlockState(world, block0Pos, SpawnPillarBlock.ACTIVATED, false))
                || (block1 == isBlockState(world, block1Pos, SpawnPillarBlock.ACTIVATED, false))
                || (block2 == isBlockState(world, block2Pos, SpawnPillarBlock.ACTIVATED, false))
                || (block3 == isBlockState(world, block3Pos, SpawnPillarBlock.ACTIVATED, false))
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't activated: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        return true;
    }

    // Starting Boss Spawn in terms of setting "running_logic" on one Pillar true
    private static void startBossSpawn(World world, SpawnPillarBlockEntity spawnPillarBlockEntity, BlockPos block0Pos) {
        // Server Side
        TrialChamberBossMod.LOGGER.info("Started Boss Spawn: " + spawnPillarBlockEntity.getPos());

        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        // Started the Spawning so the Shards should not be able to be removed
        setBlockState(world, block0Pos, SpawnPillarBlock.LOCKED, true);
        setBlockState(world, block1Pos, SpawnPillarBlock.LOCKED, true);
        setBlockState(world, block2Pos, SpawnPillarBlock.LOCKED, true);
        setBlockState(world, block3Pos, SpawnPillarBlock.LOCKED, true);

        // We are starting the Boss Spawning Process, so the block is running the Logic.
        // Only applies to one of the 4 Pillars, because only one passes the shouldStartBossSpawn()
        setBlockState(world, block0Pos, SpawnPillarBlock.RUNNING_LOGIC, true);
    }

    // -- Helper Functions --
    protected static void setBlockState(World world, BlockPos pos, Property<Boolean> property, boolean b) {
        world.setBlockState(pos, world.getBlockState(pos).with(property, b));
    }

    protected static BlockState isBlockState(World world, BlockPos pos, Property<Boolean> property, boolean b) {
        if(!(world.getBlockEntity(pos) instanceof SpawnPillarBlockEntity)) {
            return null;
        }
        return world.getBlockState(pos).with(property, b);
    }

    // Spawns Particles above the Pillar to make Item display better
    protected static void spawnParticleAbovePillar(ServerWorld world, BlockPos pos) {
        // Server Side

        particleTickCounter++;
        if (particleTickCounter >= 8) { // This is run every 8 Ticks
            double velocityX = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
            double velocityY = ThreadLocalRandom.current().nextDouble(0, 0.02);
            double velocityZ = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);

            // Server Side spawned Particle
            world.spawnParticles(ParticleTypes.END_ROD,
                    pos.getX(), pos.getY(), pos.getZ(), // pos
                    1, // count
                    velocityX, velocityY, velocityZ, // velocity
                    1 // speed
            );

            particleTickCounter = 0; // Reset Particle Counter to begin the 8 Tick counting again
        }
    }
}
