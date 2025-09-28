package net.dzultra.block.SpawnPillarBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SpawnAnimation {
    private static final int maxSpawnTickCounter = 400;
    // Define tick-based actions
    private static final List<TickAction> actions = List.of(
            new TickAction(
                    tick -> tick == 1,
                    (tickData) -> setRenderSigns(tickData.world, tickData.blockEntity)
            ),
            new TickAction(
                    tick -> tick == 40,
                    (tickData) -> trySetBlockState(tickData.world, tickData.pos, SpawnPillarBlock.SHOULD_RENDER_BEAM, true)
            ),
            new TickAction(
                    tick -> tick >= 80 && tick <= 260,
                    (tickData) -> spawnParticleRays(tickData.world, tickData.pos, tickData.spawnTickCounter)
            ),
            new TickAction(
                    tick -> tick >= 210 && tick <= 260,
                    (tickData) -> {
                        runItemRenderTranslations(SpawnPillarLogic.getAllPillarEntities(tickData.world, tickData.blockEntity), tickData.spawnTickCounter);
                    }
            ),
            /*new TickAction(
                    tick -> tick == 240,
                    (tickData) -> PedestalDownShiftingLogic.shiftPedestalDown(tickData.world, tickData.blockEntity)
            ),
            new TickAction(
                    tick -> tick == 260,
                    (tickData) -> PedestalDownShiftingLogic.shiftPedestalDown(tickData.world, tickData.blockEntity)
            ),*/
            new TickAction(
                    tick -> tick == 280,
                    (tickData) -> spawnBossEntity(tickData.world, tickData.pos)
            ),
            new TickAction(
                    tick -> tick == 300,
                    (tickData) -> trySetBlockState(tickData.world, tickData.pos, SpawnPillarBlock.SHOULD_RENDER_BEAM, false)
            ),
            new TickAction(
                    tick -> true,
                    (tickData) -> tickData.world.getPlayers().forEach((p) -> {
                        p.sendMessage(Text.of("" + tickData.spawnTickCounter), true);
                    })
            ),
            new TickAction(
                    tick -> tick == maxSpawnTickCounter,
                    (tickData) -> {
                        tickData.blockEntity.resetSpawnTickCounter();
                        resetSpawnPillars(tickData.world, tickData.pos);
                        resetItemRenderTranslations(SpawnPillarLogic.getAllPillarEntities(tickData.world, tickData.blockEntity));
                    }
            )
    );

    // -- Main Function to Tick --
    protected static void tickBossSpawnAnimation(ServerWorld world, BlockState state, SpawnPillarBlockEntity spawnPillarBlockEntity, BlockPos pos) {
        // Server Side
        // Only Counts upwards on the Anchor SpawnPillar
        // Different for each SpawnPillar Group
        int spawnTickCounter = spawnPillarBlockEntity.getAndIncrementSpawnTickCounter();

        TickData tickData = new TickData(world, pos, state, spawnPillarBlockEntity, spawnTickCounter);
        actions.forEach(action -> {
            if (action.condition.test(spawnTickCounter)) {
                action.consumer.accept(tickData);
            }
        });
    }

    private static void spawnParticleRays(ServerWorld world, BlockPos pos0, int spawnTickCounter) {
        // Calculate base position (center of block + 1.5 blocks up)
        Vec3d centerPos = new Vec3d(
                pos0.getX() + 0.5,
                pos0.getY() + 1.5,
                pos0.getZ() + 0.5
        );

        // Spawn center particles (all positive)
        spawnParticlesForDirection(world, spawnTickCounter, centerPos, 1, 1);

        // Spawn +X direction particles (negative X, positive Y/Z)
        Vec3d xPos = centerPos.add(3, 0, 0);
        spawnParticlesForDirection(world, spawnTickCounter, xPos, -1, 1);

        // Spawn +Z direction particles (positive X/Y, negative Z)
        Vec3d zPos = centerPos.add(0, 0, 3);
        spawnParticlesForDirection(world, spawnTickCounter, zPos, 1, -1);

        // Spawn +X+Z direction particles (negative X/Z, positive Y)
        Vec3d xzPos = centerPos.add(3, 0, 3);
        spawnParticlesForDirection(world, spawnTickCounter, xzPos, -1, -1);
    }

    protected static void resetSpawnPillars(ServerWorld world, BlockPos block0Pos) {
        // Define relative positions of the 4 pillars
        BlockPos[] positions = {
                block0Pos,
                block0Pos.add(3, 0, 0),
                block0Pos.add(0, 0, 3),
                block0Pos.add(3, 0, 3)
        };

        // Process each pillar
        for (int i = 0; i < positions.length; i++) {
            BlockPos pos = positions[i];
            SpawnPillarBlockEntity blockEntity = (SpawnPillarBlockEntity) world.getBlockEntity(pos);
            BlockState blockState = world.getBlockState(pos);

            if (blockEntity != null) {
                blockEntity.setStack(0, ItemStack.EMPTY); // Clear Spawn Shard
                SpawnPillarBlock.syncAndMarkBlock(world, blockEntity, pos, blockState); // Client Server Sync
            }

            // Update block states
            if (i == 0) { // Special handling for the first block
                trySetBlockState(world, pos, SpawnPillarBlock.ACTIVATED, false);
                trySetBlockState(world, pos, SpawnPillarBlock.RUNNING_LOGIC, false);

                //Leave it LOCKED so you can only use them once, leaving this in for further Development
                trySetBlockState(world, pos, SpawnPillarBlock.LOCKED, false);
            } else {
                trySetBlockState(world, pos, SpawnPillarBlock.ACTIVATED, false);

                //Leave it LOCKED so you can only use them once, leaving this in for further Development
                trySetBlockState(world, pos, SpawnPillarBlock.LOCKED, false);
            }
            world.updateListeners(pos, blockState, world.getBlockState(pos), Block.NOTIFY_ALL);
        }
    }

    private static void runItemRenderTranslations(ArrayList<SpawnPillarBlockEntity> list, int spawnTickCounter) {
        if (list.isEmpty()) return;

        float multiplier = 1.5f;

        float delta = (float) (spawnTickCounter - 210) / (260 - 210) * multiplier;


        for (SpawnPillarBlockEntity entity : list) {
            if (entity == null) continue;
            entity.setXItemRenderOffset(delta);
            entity.setYItemRenderOffset(delta);
            entity.setZItemRenderOffset(delta);
            entity.syncData();
        }
    }

    private static void resetItemRenderTranslations(ArrayList<SpawnPillarBlockEntity> list) {
        if (list.isEmpty()) return;

        list.forEach(entity -> {
            if (entity != null) {
                entity.setXItemRenderOffset(0);
                entity.setYItemRenderOffset(0);
                entity.setZItemRenderOffset(0);
            }
        });
    }

    // -- Helper Functions --
    private static void spawnParticle(ServerWorld world, Integer spawnTickCounter, Position pos, Integer threshold, double x_offset, double y_offset, double z_offset) {
        if (spawnTickCounter >= threshold) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + x_offset, pos.getY() + y_offset, pos.getZ() + z_offset, // pos
                    1, // count
                    0, 0, 0, // velocity
                    0 // speed
            );
        }
    }

    private static void spawnParticlesForDirection(ServerWorld world, int spawnTickCounter, Vec3d position, int xSign, int zSign) {
        for (int i = 0; i < 15; i++) {
            double velocity = 0.1 * (i + 1);
            spawnParticle(
                    world,
                    spawnTickCounter,
                    position,
                    80 + (5 * i),
                    xSign * velocity,
                    velocity,
                    zSign * velocity
            );
        }
    }

    private static void setRenderSigns(ServerWorld world, SpawnPillarBlockEntity spawnPillarBlockEntity){
        ArrayList<SpawnPillarBlockEntity> pillars = SpawnPillarLogic.getAllPillarEntities(world, spawnPillarBlockEntity);
        if (pillars.get(0) != null) {
            pillars.get(0).setX_render_sign(1);
            pillars.get(0).setZ_render_sign(1);
        }
        if (pillars.get(1) != null) {
            pillars.get(1).setX_render_sign(-1);
            pillars.get(1).setZ_render_sign(1);
        }
        if (pillars.get(2) != null) {
            pillars.get(2).setX_render_sign(1);
            pillars.get(2).setZ_render_sign(-1);
        }
        if (pillars.get(3) != null) {
            pillars.get(3).setX_render_sign(-1);
            pillars.get(3).setZ_render_sign(-1);
        }

    }

    protected static void trySetBlockState(ServerWorld world, BlockPos pos, Property<Boolean> property, boolean b) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof SpawnPillarBlockEntity spawnPillarBlockEntity && world.getBlockState(pos).getBlock() != Blocks.AIR) {
            SpawnPillarLogic.setBlockState(world, pos, property, b);
            SpawnPillarBlock.syncAndMarkBlock(world, spawnPillarBlockEntity, pos, spawnPillarBlockEntity.getCachedState());
        }
    }

    private static void spawnBossEntity(World world, BlockPos pos) {
        Vec3d spawnPos = new Vec3d(pos.getX() + 2, pos.getY() + 2.5, pos.getZ() + 2);
        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);

        creeper.refreshPositionAndAngles(spawnPos, 360F, 0);
        world.spawnEntity(creeper);
    }

    record TickData(ServerWorld world, BlockPos pos, BlockState state, SpawnPillarBlockEntity blockEntity,
                    int spawnTickCounter) {
    }

    record TickAction(Predicate<Integer> condition, Consumer<TickData> consumer) {
    }
}
