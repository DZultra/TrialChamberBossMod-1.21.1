package net.dzultra.block;

import net.minecraft.block.Block;
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

import javax.swing.text.html.parser.Entity;
import java.util.concurrent.ThreadLocalRandom;

public class BossSpawnPillarBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float rotation = 0;
    private int tickCounter = 0;
    private boolean hasStartedSpawning = false;

    public BossSpawnPillarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPAWN_PILLAR_BE, pos, state);
    }

    // Logic
    public void tick(World world, BlockPos pos, BlockState state) {
        if (!world.isClient()) return;

        if (!(world.getBlockEntity(pos) instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity)) {
            return;
        }
        spawnParticleAbovePillar(ParticleTypes.END_ROD, world, bossSpawnPillarBlockEntity);

        if(shouldStartBossSpawn(world, bossSpawnPillarBlockEntity)){
            startBossSpawn(world, bossSpawnPillarBlockEntity);
        }
    }

    public void spawnParticleAbovePillar(SimpleParticleType particleType, World world, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
        if (bossSpawnPillarBlockEntity.isEmpty()) return;
        tickCounter++;
        if (tickCounter >= 7) {
            double velocityX = ThreadLocalRandom.current().nextDouble(-0.03, 0.03);
            double velocityY = ThreadLocalRandom.current().nextDouble(0, 0.03);
            double velocityZ = ThreadLocalRandom.current().nextDouble(-0.03, 0.03);

            world.addParticle(particleType, pos.getX() + 0.5, pos.getY() + 1.15, pos.getZ() + 0.5, velocityX, velocityY, velocityZ);
            tickCounter = 0;
        }
    }

    private static boolean shouldStartBossSpawn(World world, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
        BlockPos block0Pos = bossSpawnPillarBlockEntity.getPos();
        BlockPos block1Pos = block0Pos.add(0, 0, 2);
        BlockPos block2Pos = block0Pos.add(0, 0, 2);
        BlockPos block3Pos = block0Pos.add(0, 0, 2);

        BlockState block1 = world.getBlockState(block1Pos);
        BlockState block2 = world.getBlockState(block2Pos);
        BlockState block3 = world.getBlockState(block3Pos);

        // Did the spawning already begin? If so, don't start spawning again
        if(bossSpawnPillarBlockEntity.hasStartedSpawning) return false;

        // Are the needed Blocks "Spawn Pillars"? If so, continue
        if (!block1.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
           || !block2.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
           || !block3.isOf(ModBlocks.BOSS_SPAWN_PILLAR)
        ) return false;

        // Are the Spawn Pillars ACTIVATED aka have a Spawn Shard on them? If so, continue and return true
        if(block1 == world.getBlockState(block1Pos).with(BossSpawnPillarBlock.ACTIVATED, false)
                || (block2 == world.getBlockState(block2Pos).with(BossSpawnPillarBlock.ACTIVATED, false))
                || (block3 == world.getBlockState(block3Pos).with(BossSpawnPillarBlock.ACTIVATED, false))
        ) return false;

        return true;
    }

    private static void startBossSpawn(World world, BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
        bossSpawnPillarBlockEntity.hasStartedSpawning = true;
        BlockPos blockpos = bossSpawnPillarBlockEntity.getPos().add(1, 0, 1);

        BlockPos spawnPos = blockpos.add(1, 1, 1);

        CreeperEntity creeper = new CreeperEntity(EntityType.CREEPER, world);
        creeper.refreshPositionAndAngles(Vec3d.ofCenter(spawnPos), 360F, 0);

        bossSpawnPillarBlockEntity.hasStartedSpawning = false;
        bossSpawnPillarBlockEntity.setStack(0, ItemStack.EMPTY);

        bossSpawnPillarBlockEntity.syncInventory();
        bossSpawnPillarBlockEntity.markDirty();
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