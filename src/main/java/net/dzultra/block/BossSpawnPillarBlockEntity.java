package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class BossSpawnPillarBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float rotation = 0;
    private int particleTickCounter = 0;
    protected static int spawnTickCounter = 0;
    protected static final int maxSpawnTickTime = 40;

    public BossSpawnPillarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPAWN_PILLAR_BE, pos, state);
    }

    // Logic
    public void tick(World world, BlockPos pos, BlockState state) {

        if (!(world.getBlockEntity(pos) instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity)) {
            return;
        }

        ItemStack stack = bossSpawnPillarBlockEntity.inventory.getFirst();

        if (!stack.isEmpty() && stack.getItem() == ModItems.SPAWN_SHARD) {
            world.setBlockState(bossSpawnPillarBlockEntity.pos, state.with(BossSpawnPillarBlock.ACTIVATED, true));
        } else {
            world.setBlockState(bossSpawnPillarBlockEntity.pos, state.with(BossSpawnPillarBlock.ACTIVATED, false));
        }

        if (state.get(BossSpawnPillarBlock.ACTIVATED)) {
            spawnParticleAbovePillar(ParticleTypes.END_ROD, world, bossSpawnPillarBlockEntity);
        }

        if (state.get(BossSpawnPillarBlock.HAS_STARTED_SPAWNED)) {
            if (!world.isClient()) {
                spawnTickCounter++;
                BossSpawnAnimation.tickBossSpawnAnimation(world, state, bossSpawnPillarBlockEntity, spawnTickCounter);
            }
        }

        if(shouldStartBossSpawn(world, state, bossSpawnPillarBlockEntity)){
            startBossSpawn(world, state, bossSpawnPillarBlockEntity, pos);
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
        if(state == world.getBlockState(bossSpawnPillarBlockEntity.pos).with(BossSpawnPillarBlock.HAS_STARTED_SPAWNED, true)) {
            //TrialChamberBossMod.LOGGER.info("[TCB] hasStartedSpawning: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        // Are the Spawn Pillars ACTIVATED aka have a Spawn Shard on them? If so, continue and return true
        if((block0 == world.getBlockState(block0Pos).with(BossSpawnPillarBlock.ACTIVATED, false))
                || (block1 == world.getBlockState(block1Pos).with(BossSpawnPillarBlock.ACTIVATED, false))
                || (block2 == world.getBlockState(block2Pos).with(BossSpawnPillarBlock.ACTIVATED, false))
                || (block3 == world.getBlockState(block3Pos).with(BossSpawnPillarBlock.ACTIVATED, false))
        ) {
            //TrialChamberBossMod.LOGGER.info("[TCB] Required Blocks aren't activated: " + bossSpawnPillarBlockEntity.pos);
            return false;
        }

        return true;
    }

    private static void startBossSpawn(World world, BlockState state, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity, BlockPos block0Pos) {
        if (world.isClient()) return;
        TrialChamberBossMod.LOGGER.info("Started Boss Spawn1: " + bossSpawnPillarBlockEntity.pos);
        TrialChamberBossMod.LOGGER.info("Started Boss Spawn2: " + block0Pos);

        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);
        TrialChamberBossMod.LOGGER.info("Started Boss Spawn3: " + block0Pos);
        world.setBlockState(block0Pos, state.with(BossSpawnPillarBlock.LOCKED, true));
        world.setBlockState(block1Pos, state.with(BossSpawnPillarBlock.LOCKED, true));
        world.setBlockState(block2Pos, state.with(BossSpawnPillarBlock.LOCKED, true));
        world.setBlockState(block3Pos, state.with(BossSpawnPillarBlock.LOCKED, true));

        BlockPos spawnPos = getBossSpawnPos(bossSpawnPillarBlockEntity.pos);
        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
        creeper.refreshPositionAndAngles(Vec3d.of(spawnPos), 360F, 0);
        world.spawnEntity(creeper);

        world.setBlockState(bossSpawnPillarBlockEntity.pos, state.with(BossSpawnPillarBlock.HAS_STARTED_SPAWNED, true));

        // While the spawning is happening, make it so if a Spawn Pillar is broken or not active anymore to nuke the animation
    }

    protected static void resetSpawnPillars(BlockPos block0Pos, World world, BlockState state) {
        BlockPos block1Pos = block0Pos.add(3, 0, 0);
        BlockPos block2Pos = block0Pos.add(0, 0, 3);
        BlockPos block3Pos = block0Pos.add(3, 0, 3);

        BossSpawnPillarBlockEntity block0Entity = (BossSpawnPillarBlockEntity) world.getBlockEntity(block0Pos);
        BossSpawnPillarBlockEntity block1Entity = (BossSpawnPillarBlockEntity) world.getBlockEntity(block1Pos);
        BossSpawnPillarBlockEntity block2Entity = (BossSpawnPillarBlockEntity) world.getBlockEntity(block2Pos);
        BossSpawnPillarBlockEntity block3Entity = (BossSpawnPillarBlockEntity) world.getBlockEntity(block3Pos);

        block0Entity.setStack(0, ItemStack.EMPTY);
        block1Entity.setStack(0, ItemStack.EMPTY);
        block2Entity.setStack(0, ItemStack.EMPTY);
        block3Entity.setStack(0, ItemStack.EMPTY);

        block0Entity.markDirty();
        block1Entity.markDirty();
        block2Entity.markDirty();
        block3Entity.markDirty();

        block0Entity.syncInventory();
        block1Entity.syncInventory();
        block2Entity.syncInventory();
        block3Entity.syncInventory();
    }

    public void spawnParticleAbovePillar(SimpleParticleType particleType, World world, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
        if (!world.isClient()) return;
        if (bossSpawnPillarBlockEntity.isEmpty()) return;
        particleTickCounter++;
        if (particleTickCounter >= 8) {
            double velocityX = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);
            double velocityY = ThreadLocalRandom.current().nextDouble(0, 0.02);
            double velocityZ = ThreadLocalRandom.current().nextDouble(-0.02, 0.02);

            world.addParticle(particleType, pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, velocityX, velocityY, velocityZ);
            particleTickCounter = 0;
        }
    }

    protected static BlockPos getBossSpawnPos(BlockPos blockPos) {
        return blockPos.add(2, 2, 2);
    }

    // Other

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
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
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
// 118 tut nicht, er locked den SpawnPillar, der die ganze Logik ausführt nicht nachdem er aktiviert ist/das ganze Ding anfangen soll zu spawnen
// Loop des spawnens
// bei reset der SpawnPillars crash