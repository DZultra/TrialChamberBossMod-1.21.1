package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BossSpawnAnimation {
    // This function is only executed on the main BossSpawnPillarEntity which all the Spawning Logic executes
    // And we are fully Server Side
    // spawnTickCounter counts upwards 1 per Tick after the Spawning began
    protected static void tickBossSpawnAnimation(World world, BlockState state, BossSpawnPillarBlockEntity blockEntity, Integer spawnTickCounter) {
        //TrialChamberBossMod.LOGGER.info("[TCB] SpawnTickCounter: " + spawnTickCounter);
        if (spawnTickCounter <= 60) {
            BlockPos pos = blockEntity.getPos();
            //TrialChamberBossMod.LOGGER.info("Spawned Particle at: " + pos);
            world.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2, 0, 0, 0);
        }

        if (spawnTickCounter == BossSpawnPillarBlockEntity.maxSpawnTickTime) {
            BossSpawnPillarBlockEntity.spawnTickCounter = 0;
            world.setBlockState(blockEntity.getPos(), state.with(BossSpawnPillarBlock.HAS_STARTED_SPAWNED, false));
        }
    }
}
