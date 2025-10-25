package net.dzultra.entity.custom;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.block.ModBlocks;
import net.dzultra.entity.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ChainPillarEntity extends Entity {
    private int timeFalling;
    private int timeRaising;
    private double fallingGravity;
    private double raisingVelocity;
    private boolean shouldFall;
    private int raisingDistance;
    private double spawnY;

    public ChainPillarEntity(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);
    }

    private ChainPillarEntity(World world, Vec3d pos, double fallingGravity, double raisingVelocity, int raisingDistance, boolean shouldFall) {
        this(ModEntities.CHAIN_PILLAR_ENTITY, world);
        this.setPosition(pos.x, pos.y, pos.z);
        this.prevX = pos.x;
        this.prevY = pos.y;
        this.prevZ = pos.z;
        this.spawnY = pos.y;

        this.shouldFall = shouldFall;
        this.fallingGravity = fallingGravity;
        this.raisingVelocity = raisingVelocity;
        this.raisingDistance = raisingDistance;
    }

    public static ChainPillarEntity of(World world, Vec3d pos, double fallingVelocity, double raisingVelocity, int raisingDistance, boolean shouldFall) {
        return new ChainPillarEntity(world, pos, fallingVelocity, raisingVelocity, raisingDistance, shouldFall);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    protected double getGravity() {
        return this.fallingGravity;
    }

    public void tick() {
        if (this.getWorld().isClient) {
            super.tick();
            return; // logic only runs on server
        }

        if (this.shouldFall) {
            // --- Falling behavior ---
            this.applyGravity();
            this.move(MovementType.SELF, this.getVelocity());

            if (this.isOnGround()) {
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    this.placeChainPillar(serverWorld, this.getPos());
                    this.discard();
                }
            }
            return;
        }

        // --- Raising behavior ---
        this.noClip = true;

        // constant upward motion (smooth)
        double velY = this.raisingVelocity;
        this.setVelocity(0, velY, 0);
        this.move(MovementType.SELF, this.getVelocity());
        this.timeRaising++;

        // check if reached target
        if (this.getY() >= this.spawnY + this.raisingDistance) {
            this.setVelocity(Vec3d.ZERO);
            this.discard();

            if (this.getWorld() instanceof ServerWorld serverWorld) {
                this.placeChainPillar(serverWorld, this.getPos());
            }
        }

        this.noClip = false;
    }
    private void placeChainPillar(ServerWorld world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos).add(-1, 0, -1);

        List<BlockPos> reinforcedCopperBlocks = List.of(
                blockPos,
                blockPos.add(1, 0, 0),
                blockPos.add(0, 0, 1),
                blockPos.add(1, 0, 1),
                blockPos.add(0, 3, 0),
                blockPos.add(1, 3, 0),
                blockPos.add(0, 3, 1),
                blockPos.add(1, 3, 1)
        );

        List<BlockPos> reinforcedTuffBricks = List.of(
                blockPos.add(0, 1, 0),
                blockPos.add(1, 1, 0),
                blockPos.add(0, 1, 1),
                blockPos.add(1, 1, 1),
                blockPos.add(0, 2, 0),
                blockPos.add(1, 2, 0),
                blockPos.add(0, 2, 1),
                blockPos.add(1, 2, 1)
        );

        for (BlockPos posToPlace : reinforcedCopperBlocks) {
            world.setBlockState(posToPlace, ModBlocks.REINFORCED_COPPER_BLOCK.getDefaultState());
        }
        for (BlockPos posToPlace : reinforcedTuffBricks) {
            world.setBlockState(posToPlace, ModBlocks.REINFORCED_TUFF_BRICKS.getDefaultState());
        }
    }
}