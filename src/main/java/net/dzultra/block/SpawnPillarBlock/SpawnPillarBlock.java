package net.dzultra.block.SpawnPillarBlock;


import com.mojang.serialization.MapCodec;
import net.dzultra.block.ModBlockEntities;
import net.dzultra.item.ModItems;
import net.dzultra.networking.SyncTCBSpawnPillarBlockEntityS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnPillarBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");
    public static final BooleanProperty RUNNING_LOGIC = BooleanProperty.of("running_logic");
    public static final BooleanProperty LOCKED = BooleanProperty.of("locked");
    public static final MapCodec<SpawnPillarBlock> CODEC = SpawnPillarBlock.createCodec(SpawnPillarBlock::new);

    public SpawnPillarBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getDefaultState().with(ACTIVATED, false));
        setDefaultState(this.getDefaultState().with(RUNNING_LOGIC, false));
        setDefaultState(this.getDefaultState().with(LOCKED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
        builder.add(RUNNING_LOGIC);
        builder.add(LOCKED);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpawnPillarBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SpawnPillarBlockEntity spawnPillarBlockEntity) {
                ItemScatterer.spawn(world, pos, spawnPillarBlockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.SPAWN_PILLAR_BE,
                (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {

        if (!(world.getBlockEntity(pos) instanceof SpawnPillarBlockEntity spawnPillarBlockEntity)) {
            return ItemActionResult.FAIL;
        }

        if(state.get(SpawnPillarBlock.LOCKED)) {
            return ItemActionResult.CONSUME;
        }

        if (stack.getItem() != ModItems.SPAWN_SHARD && !stack.isEmpty()){
            return ItemActionResult.CONSUME;
        }

        if (spawnPillarBlockEntity.isEmpty() && !stack.isEmpty()) {
            // Block Empty & Hand has Item -> Item from Hand into Block
            spawnPillarBlockEntity.setStack(0, stack.copyWithCount(1)); // Item from Hand onto Block
            stack.decrement(1); // Remove Item from the Hand
            world.setBlockState(pos, state.with(SpawnPillarBlock.ACTIVATED, true)); // Activate Block

            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 2f);

            syncAndMarkBlock(world, spawnPillarBlockEntity, pos, state);
        }

        else if (!spawnPillarBlockEntity.isEmpty() && stack.isEmpty() && !player.isSneaking()) {
            // Block has Item & Hand Empty -> Item from Block into Hand
            ItemStack stackOnSpawnPillar = spawnPillarBlockEntity.getItems().getFirst(); // Item on Block
            player.setStackInHand(Hand.MAIN_HAND, stackOnSpawnPillar); // Give Item on Block to Player
            spawnPillarBlockEntity.setStack(0, ItemStack.EMPTY); // Clear Item on Block
            world.setBlockState(pos, state.with(SpawnPillarBlock.ACTIVATED, false)); // DeActivate Block

            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);

            syncAndMarkBlock(world, spawnPillarBlockEntity, pos, state);
        }

        else if (spawnPillarBlockEntity.isEmpty() && stack.isEmpty()) {
            // Block Empty & Hand Empty -> Do nothing
            return ItemActionResult.CONSUME;
        }

        else if (!spawnPillarBlockEntity.isEmpty() && !stack.isEmpty()) {
            // Block has Item & Hand has Item -> If same ItemStack increment Stack in Hand, if not same ItemStack do nth
            if (stack.isOf(spawnPillarBlockEntity.getStack(0).getItem()) && stack.getCount() < stack.getMaxCount()) {
                spawnPillarBlockEntity.setStack(0, ItemStack.EMPTY); // Clear Block
                stack.increment(1); // Give Item to Hand
                world.setBlockState(pos, state.with(SpawnPillarBlock.ACTIVATED, false)); // DeActivate Block

                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 2f);

                syncAndMarkBlock(world, spawnPillarBlockEntity, pos, state);
            } else {
                return ItemActionResult.CONSUME; // Do nothing but don't place block
            }
        }
        return ItemActionResult.SUCCESS;
    }

    private static void syncAndMarkBlock(World world, SpawnPillarBlockEntity entity, BlockPos pos, BlockState state) {
        entity.markDirty();
        entity.syncInventory();
        world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
    }

    public static void sendSyncPacket(  World world, BlockPos blockpos, DefaultedList<ItemStack> inventory) {
        if (world.isClient()) return;
        SyncTCBSpawnPillarBlockEntityS2CPayload payload = new SyncTCBSpawnPillarBlockEntityS2CPayload(blockpos, inventory);

        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world((ServerWorld) world)) {
            ServerPlayNetworking.send(serverPlayerEntity, payload);
        }
    }
}
