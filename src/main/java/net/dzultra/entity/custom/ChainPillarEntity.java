package net.dzultra.entity.custom;

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

    @Override
    public void tick() {
        // --- Falling ---
        if (this.shouldFall) {
            this.noClip = false;
            this.applyGravity();
            this.move(MovementType.SELF, this.getVelocity());
            this.timeFalling++;

            if (this.isOnGround()) {
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    this.placeChainPillar(serverWorld, this.getPos());
                    this.discard();
                }
            }
            return;
        }

        // --- Raising ---
        this.noClip = true;

        // Calculate target position and smooth progress
        double targetY = this.spawnY + this.raisingDistance;
        double currentY = this.getY();
        double distanceRemaining = targetY - currentY;

        // Smooth raising using lerp (exponential smoothing)
        double smoothing = 0.15D; // smaller = slower acceleration, smoother overall
        double desiredVelocity = this.raisingVelocity;
        double currentVelocityY = this.getVelocity().y;
        double newVelocityY = currentVelocityY + (desiredVelocity - currentVelocityY) * smoothing;

        // Clamp so it never overshoots
        if (distanceRemaining < newVelocityY) {
            newVelocityY = distanceRemaining;
        }

        this.setVelocity(0.0, newVelocityY, 0.0);
        this.move(MovementType.SELF, this.getVelocity());
        this.timeRaising++;

        // Stop when reached target
        if (this.getY() >= targetY - 0.01) {
            this.setVelocity(Vec3d.ZERO);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                this.placeChainPillar(serverWorld, this.getPos());
            }
            this.discard();
            return;
        }

        // Optional: remove blocks during motion
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            BlockPos blockPos = BlockPos.ofFloored(this.getPos());
            BlockPos pos1 = blockPos.add(-1, 0, 0);
            BlockPos pos2 = blockPos.add(0, 0, -1);
            serverWorld.removeBlock(pos1, false);
            serverWorld.removeBlock(pos2, false);
        }
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