package net.dzultra.block.SpawnPillarBlock;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SpawnPillarLogic {

    // Tick Function from SpawnPillarBlock, just in a separate class
    protected static void tick(World world, BlockPos pos, BlockState state) {
        // Client & Server Side
        if (!(world.getBlockEntity(pos) instanceof SpawnPillarBlockEntity spawnPillarBlockEntity)) return;

        // SpawnParticles for better display of the Item on the SpawnPillar
        if ((state.get(SpawnPillarBlock.ACTIVATED))) {
            if (!world.isClient()) {
                spawnParticleAbovePillar(((ServerWorld) world), spawnPillarBlockEntity);
            }
        }

        // Once the startBossSpawn() was executed the BlockState "running_logic" is set true
        // This starts the SpawnAnimation
        if (state.get(SpawnPillarBlock.RUNNING_LOGIC) && !world.isClient()) { //
            if (checkIfPillarsExist(world, spawnPillarBlockEntity)) {
                SpawnAnimation.tickBossSpawnAnimation(((ServerWorld) world), state, spawnPillarBlockEntity, pos);
            } else {
                SpawnAnimation.trySetBlockState((ServerWorld) world, pos, SpawnPillarBlock.SHOULD_RENDER_BEAM, false);
                SpawnAnimation.resetSpawnPillars((ServerWorld) world, spawnPillarBlockEntity.getPos());
            }
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
        if(state.get(SpawnPillarBlock.RUNNING_LOGIC)) {
            //TrialChamberBossMod.LOGGER.info("[TCB] hasStartedSpawning: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        // Are the Spawn Pillars ACTIVATED aka have a Spawn Shard on them? If so, continue and return true
        if (!(block0.get(SpawnPillarBlock.ACTIVATED)
                        && block1.get(SpawnPillarBlock.ACTIVATED)
                        && block2.get(SpawnPillarBlock.ACTIVATED)
                        && block3.get(SpawnPillarBlock.ACTIVATED))
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't activated: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }
        return true;
    }

    protected static boolean checkIfPillarsExist(World world, SpawnPillarBlockEntity spawnPillarBlockEntity) {
        if (world.isClient()) return false;
        // Server Side
        BlockPos block0Pos = spawnPillarBlockEntity.getPos();
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        BlockState block1 = world.getBlockState(block1Pos);
        BlockState block2 = world.getBlockState(block2Pos);
        BlockState block3 = world.getBlockState(block3Pos);

        return block1.isOf(ModBlocks.SPAWN_PILLAR)
                && block2.isOf(ModBlocks.SPAWN_PILLAR)
                && block3.isOf(ModBlocks.SPAWN_PILLAR);
    }

    protected static ArrayList<SpawnPillarBlockEntity> getAllPillarEntities(World world, SpawnPillarBlockEntity spawnPillarBlockEntity) {
        if (world.isClient() || !checkIfPillarsExist(world, spawnPillarBlockEntity)) return new ArrayList<SpawnPillarBlockEntity>();
        // Server Side
        BlockPos block0Pos = spawnPillarBlockEntity.getPos();
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        var list = new ArrayList<SpawnPillarBlockEntity>();

        list.add((SpawnPillarBlockEntity) world.getBlockEntity(block0Pos));
        list.add((SpawnPillarBlockEntity) world.getBlockEntity(block1Pos));
        list.add((SpawnPillarBlockEntity) world.getBlockEntity(block2Pos));
        list.add((SpawnPillarBlockEntity) world.getBlockEntity(block3Pos));

        return list;
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

        for (BlockPos pos : new BlockPos[]{block0Pos, block1Pos, block2Pos, block3Pos}) {
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
        }
    }

    // -- Helper Functions --
    protected static void setBlockState(World world, BlockPos pos, Property<Boolean> property, boolean b) {
        world.setBlockState(pos, world.getBlockState(pos).with(property, b));
    }

    // Spawns Particles above the Pillar to make Item display better
    protected static void spawnParticleAbovePillar(ServerWorld world, SpawnPillarBlockEntity blockEntity) {
        int counter = blockEntity.getAndIncrementParticleCounter();
        if (counter >= 8) {
            BlockPos pos = blockEntity.getPos();

            double velocityX = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
            double velocityY = ThreadLocalRandom.current().nextDouble(0, 0.02);
            double velocityZ = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);

            Vec3d velocity = new Vec3d(velocityX, velocityY, velocityZ);
            Vec3d position =  new Vec3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);


            spawnParticle(world, ParticleTypes.END_ROD, position, velocity, 1, 0.015);
            blockEntity.resetParticleCounter();
        }
    }

    private static <T extends ParticleEffect> void spawnParticle (ServerWorld world, T p, Vec3d pos, Vec3d velocity, int amount, double speed) {
        world.spawnParticles(p,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                amount,
                velocity.getX(),
                velocity.getY(),
                velocity.getZ(),
                speed);
    }
}
