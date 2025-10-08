package net.dzultra.block.SpawnPillarBlock;

import net.dzultra.block.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnPillarBlockEntity extends BlockEntity implements ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private float rotation = 0;
    private int spawnTickCounter = 0; // TickCounter for Counting Ticks after Boss Spawning started
    private int particleTickCounter = 0; // TickCounter for Particle Spawning above Block when it is Activated
    private float x_item_render_offset = 0;
    private float y_item_render_offset = 0;
    private float z_item_render_offset = 0;
    private int x_render_sign = 0;
    private int z_render_sign = 0;
    private int side_pillar_y = 0;
    private int pedestal_rods_offset = 0;
    private int chain_pillar_offset = 0;

    public SpawnPillarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPAWN_PILLAR_BE, pos, state);
    }

    // -- Logic --
    public void tick(World world, BlockPos pos, BlockState state) {
        SpawnPillarLogic.tick(world, pos, state); // Execute Tick Function in Logic Class
    }

    // -- Helper Functions --

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }

    public int getAndIncrementParticleCounter() {
        this.markDirty();
        return particleTickCounter++;
    }

    public void resetParticleCounter() {
        particleTickCounter = 0;
        this.markDirty();
    }

    public void incrementSpawnTickCounter() {
        spawnTickCounter++;
        this.markDirty();
    }

    public int getSpawnTickCounter() {
        this.markDirty();
        return spawnTickCounter;
    }

    public void setSpawnTickCounter(int amount) {
        spawnTickCounter = amount;
        this.markDirty();
    }

    public void resetSpawnTickCounter() {
        spawnTickCounter = 0;
        this.markDirty();
    }

    public float getXItemRenderOffset() {
        this.markDirty();
        return x_item_render_offset;
    }

    public float getYItemRenderOffset() {
        this.markDirty();
        return y_item_render_offset;
    }

    public float getZItemRenderOffset() {
        this.markDirty();
        return z_item_render_offset;
    }

    public void setXItemRenderOffset(float amount) {
        x_item_render_offset = amount;
        this.markDirty();
    }

    public void setYItemRenderOffset(float amount) {
        y_item_render_offset = amount;
        this.markDirty();
    }

    public void setZItemRenderOffset(float amount) {
        z_item_render_offset = amount;
        this.markDirty();
    }

    public int getX_render_sign() {
        this.markDirty();
        return x_render_sign;
    }

    public int getZ_render_sign() {
        this.markDirty();
        return z_render_sign;
    }

    public void setX_render_sign(int amount) {
        x_render_sign = amount;
        this.markDirty();
    }

    public void setZ_render_sign(int amount) {
        z_render_sign = amount;
        this.markDirty();
    }

    public int getSide_pillar_y() {
        this.markDirty();
        return side_pillar_y;
    }

    public void setSide_pillar_y(int amount) {
        side_pillar_y = amount;
        this.markDirty();
    }

    public void decrementSide_pillar_y() {
        side_pillar_y--;
        this.markDirty();
    }

    public int getPedestal_rods_offset() {
        this.markDirty();
        return pedestal_rods_offset;
    }

    public void setPedestal_rods_offset(int amount) {
        pedestal_rods_offset = amount;
        this.markDirty();
    }

    public void incrementPedestal_rods_offset() {
        pedestal_rods_offset++;
        this.markDirty();
    }

    public int getChain_pillar_offset() {
        this.markDirty();
        return chain_pillar_offset;
    }

    public void setChain_pillar_offset(int amount) {
        chain_pillar_offset = amount;
        this.markDirty();
    }

    public void incrementChain_pillar_offset() {
        chain_pillar_offset++;
        this.markDirty();
    }

    public boolean isRunningLogic() {
        return this.getSpawnTickCounter() > 0;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("spawnTickCounter", spawnTickCounter);
        nbt.putInt("particleTickCounter", particleTickCounter);
        nbt.putInt("x_render_sign", x_render_sign);
        nbt.putInt("z_render_sign", z_render_sign);
        nbt.putInt("side_pillar_y", side_pillar_y);
        nbt.putInt("pedestal_rods_offset", pedestal_rods_offset);
        nbt.putInt("chain_pillar_offset", chain_pillar_offset);
        nbt.putFloat("x_item_render_offset", x_item_render_offset);
        nbt.putFloat("y_item_render_offset", y_item_render_offset);
        nbt.putFloat("z_item_render_offset", z_item_render_offset);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        spawnTickCounter = nbt.getInt("spawnTickCounter");
        particleTickCounter = nbt.getInt("particleTickCounter");
        x_render_sign = nbt.getInt("x_render_sign");
        z_render_sign = nbt.getInt("z_render_sign");
        side_pillar_y = nbt.getInt("side_pillar_y");
        pedestal_rods_offset = nbt.getInt("pedestal_rods_offset");
        chain_pillar_offset = nbt.getInt("chain_pillar_offset");
        x_item_render_offset = nbt.getFloat("x_item_render_offset");
        y_item_render_offset = nbt.getFloat("y_item_render_offset");
        z_item_render_offset = nbt.getFloat("z_item_render_offset");
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

    public void syncData() {
        if (this.getWorld() != null && !this.getWorld().isClient()) {
            SpawnPillarBlock.sendSyncPacket(
                    this.getWorld(),
                    this.getPos(),
                    this.getItems(),
                    this.getXItemRenderOffset(), this.getYItemRenderOffset(), this.getZItemRenderOffset(),
                    this.getX_render_sign(), this.getZ_render_sign(),
                    this.getSpawnTickCounter()
            );
        }
    }
}