package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Property;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class BossSpawnPillarBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float rotation = 0;
    private int particleTickCounter = 0;
    protected int spawnTickCounter = 0;
    protected boolean executingLogic = false;


    public BossSpawnPillarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPAWN_PILLAR_BE, pos, state);
    }

    // Logic
    public void tick(World world, BlockPos pos, BlockState state) {

        if (!(world.getBlockEntity(pos) instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity)) {
            return;
        }

        if (state.get(BossSpawnPillarBlock.ACTIVATED)) {
            spawnParticleAbovePillar(ParticleTypes.END_ROD, world, bossSpawnPillarBlockEntity);
        }

        if (state.get(BossSpawnPillarBlock.HAS_STARTED_SPAWNED)) {
            if (!world.isClient()) {
                BossSpawnAnimation.tickBossSpawnAnimation(world, state, bossSpawnPillarBlockEntity, bossSpawnPillarBlockEntity.pos);
            }
        }

        if(shouldStartBossSpawn(world, state, bossSpawnPillarBlockEntity)){
            executingLogic = true;
            startBossSpawn(world, bossSpawnPillarBlockEntity, bossSpawnPillarBlockEntity.getPos());
        }
    }

    private static boolean shouldStartBossSpawn(World world, BlockState state, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
        if (world.isClient()) return false;
        BlockPos block0Pos = bossSpawnPillarBlockEntity.getPos();
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        BlockState block0 = world.getBlockState(block0Pos);
        BlockState block1 = world.getBlockState(block1Pos);
        BlockState block2 = world.getBlockState(block2Pos);
        BlockState block3 = world.getBlockState(block3Pos);

        // Are the needed Blocks "Spawn Pillars"? If so, continue
        if (!block1.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
           || !block2.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
           || !block3.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't Spawn Pillars: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        // Did the spawning already begin? If so, don't start spawning again
        if(state == getBlockState(world, block0Pos, BossSpawnPillarBlock.HAS_STARTED_SPAWNED, true)) {
            //TrialChamberBossMod.LOGGER.info("[TCB] hasStartedSpawning: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        // Are the Spawn Pillars ACTIVATED aka have a Spawn Shard on them? If so, continue and return true
        if((block0 == getBlockState(world, block0Pos, BossSpawnPillarBlock.ACTIVATED, false))
                || (block1 == getBlockState(world, block1Pos, BossSpawnPillarBlock.ACTIVATED, false))
                || (block2 == getBlockState(world, block2Pos, BossSpawnPillarBlock.ACTIVATED, false))
                || (block3 == getBlockState(world, block3Pos, BossSpawnPillarBlock.ACTIVATED, false))
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't activated: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        return true;
    }

    protected static boolean couldExecuteLogic(World world, BlockEntity be) {
        BlockPos block0Pos = be.getPos();
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        BlockState block0 = world.getBlockState(block0Pos);
        BlockState block1 = world.getBlockState(block1Pos);
        BlockState block2 = world.getBlockState(block2Pos);
        BlockState block3 = world.getBlockState(block3Pos);

        // Are the needed Blocks "Spawn Pillars"? If so, continue
        if (!block1.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
                || !block2.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
                || !block3.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't Spawn Pillars: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }
        return true;
    }

    private static void startBossSpawn(World world, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity, BlockPos block0Pos) {
        if (world.isClient()) return;
        TrialChamberBossMod.LOGGER.info("Started Boss Spawn: " + bossSpawnPillarBlockEntity.pos);

        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        setBlockState(world, block0Pos, BossSpawnPillarBlock.LOCKED, true);
        setBlockState(world, block1Pos, BossSpawnPillarBlock.LOCKED, true);
        setBlockState(world, block2Pos, BossSpawnPillarBlock.LOCKED, true);
        setBlockState(world, block3Pos, BossSpawnPillarBlock.LOCKED, true);

        setBlockState(world, block0Pos, BossSpawnPillarBlock.HAS_STARTED_SPAWNED, true);

        // While the spawning is happening, make it so if a Spawn Pillar is broken or not active anymore to nuke the animation
    }

    public void spawnParticleAbovePillar(SimpleParticleType particleType, World world, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
        if (!world.isClient()) return;
        if (bossSpawnPillarBlockEntity.isEmpty()) return;
        particleTickCounter++;
        if (particleTickCounter >= 8) {
            double velocityX = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
            double velocityY = ThreadLocalRandom.current().nextDouble(0, 0.02);
            double velocityZ = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);

            // Particles Spawned Client Side
            world.addParticle(particleType, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, velocityX, velocityY, velocityZ);
            particleTickCounter = 0;
        }
    }

    protected static BlockPos getBossSpawnPos(BlockPos blockPos) {
        return blockPos.add(2, 2, 2);
    }

    // Other

    protected static void setBlockState(World world, BlockPos pos, Property<Boolean> property, boolean b) {
        world.setBlockState(pos, world.getBlockState(pos).with(property, b));
    }

    protected static BlockState getBlockState(World world, BlockPos pos, Property<Boolean> property, boolean b) {
        if(!(world.getBlockEntity(pos) instanceof BossSpawnPillarBlockEntity)) {
            return null;
        }
        return world.getBlockState(pos).with(property, b);
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        if (executingLogic) {
            nbt.putInt("spawnTickCounter", spawnTickCounter);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        spawnTickCounter = nbt.getInt("spawnTickCounter");
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }


    // Client Server Sync
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    public void syncInventory() {
        if (this.world != null && !this.world.isClient) {
            BossSpawnPillarBlock.sendSyncPacket(this.world, this.pos, this.inventory);
        }
    }
}