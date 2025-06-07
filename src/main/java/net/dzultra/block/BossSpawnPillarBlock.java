package net.dzultra.block;


import com.mojang.serialization.MapCodec;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BossSpawnPillarBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final BooleanProperty ACTIVATED = BooleanProperty.of("activated");
    public static final BooleanProperty HAS_STARTED_SPAWNED = BooleanProperty.of("has_started_spawn");
    public static final BooleanProperty LOCKED = BooleanProperty.of("locked");
    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 13, 14);
    public static final MapCodec<BossSpawnPillarBlock> CODEC = BossSpawnPillarBlock.createCodec(BossSpawnPillarBlock::new);

    public BossSpawnPillarBlock(Settings settings) {
        super(settings);
        setDefaultState(this.getDefaultState().with(ACTIVATED, false));
        setDefaultState(this.getDefaultState().with(HAS_STARTED_SPAWNED, false));
        setDefaultState(this.getDefaultState().with(LOCKED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
        builder.add(HAS_STARTED_SPAWNED);
        builder.add(LOCKED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BossSpawnPillarBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity) {
                if (bossSpawnPillarBlockEntity.isExecutingLogic()) {
                    BossSpawnAnimation.resetSpawnPillars(world, pos);
                }

                ItemScatterer.spawn(world, pos, bossSpawnPillarBlockEntity);
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

        if (!(world.getBlockEntity(pos) instanceof BossSpawnPillarBlockEntity bossSpawnPillarBlockEntity)) {
            return ItemActionResult.FAIL;
        }

        if(state == world.getBlockState(pos).with(BossSpawnPillarBlock.LOCKED, true)) {
            return ItemActionResult.CONSUME;
        }

        if (stack.getItem() != ModItems.SPAWN_SHARD && !stack.isEmpty()){
            return ItemActionResult.CONSUME;
        }

        if (bossSpawnPillarBlockEntity.isEmpty() && !stack.isEmpty()) {
            // Block Empty & Hand has Item -> Item from Hand into Block
            bossSpawnPillarBlockEntity.setStack(0, stack.copyWithCount(1));
            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 2f);
            stack.decrement(1);
            world.setBlockState(pos, state.with(BossSpawnPillarBlock.ACTIVATED, true));
            bossSpawnPillarBlockEntity.markDirty();
            bossSpawnPillarBlockEntity.syncInventory();
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
        }

        else if (!bossSpawnPillarBlockEntity.isEmpty() && stack.isEmpty() && !player.isSneaking()) {
            // Block has Item & Hand Empty -> Item from Block into Hand
            ItemStack stackOnSpawnPillar = bossSpawnPillarBlockEntity.getItems().getFirst();
            player.setStackInHand(Hand.MAIN_HAND, stackOnSpawnPillar);
            world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
            bossSpawnPillarBlockEntity.setStack(0, ItemStack.EMPTY);
            world.setBlockState(pos, state.with(BossSpawnPillarBlock.ACTIVATED, false));
            bossSpawnPillarBlockEntity.markDirty();
            bossSpawnPillarBlockEntity.syncInventory();
            world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
        }

        else if (bossSpawnPillarBlockEntity.isEmpty() && stack.isEmpty()) {
            // Block Empty & Hand Empty -> Do nothing
            world.setBlockState(pos, state.with(BossSpawnPillarBlock.ACTIVATED, false));
            bossSpawnPillarBlockEntity.syncInventory();
            return ItemActionResult.CONSUME;
        }

        else if (!bossSpawnPillarBlockEntity.isEmpty() && !stack.isEmpty()) {
            // Block has Item & Hand has Item -> If same ItemStack increment Stack in Hand, if not same ItemStack do nth
            if (stack.isOf(bossSpawnPillarBlockEntity.getStack(0).getItem()) && stack.getCount() < stack.getMaxCount()) {
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 2f);
                bossSpawnPillarBlockEntity.setStack(0, ItemStack.EMPTY);
                stack.increment(1);
                world.setBlockState(pos, state.with(BossSpawnPillarBlock.ACTIVATED, false));
                bossSpawnPillarBlockEntity.markDirty();
                bossSpawnPillarBlockEntity.syncInventory();
                world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
            } else {
                sendSyncPacket(world, pos, bossSpawnPillarBlockEntity.getItems());
                return ItemActionResult.CONSUME; // Do nothing but don't place block
            }
        }
        return ItemActionResult.SUCCESS;
    }

    public static void sendSyncPacket(World world, BlockPos blockpos, DefaultedList<ItemStack> inventory) {
        if (world.isClient()) return;
        SyncTCBSpawnPillarBlockEntityS2CPayload payload = new SyncTCBSpawnPillarBlockEntityS2CPayload(blockpos, inventory);

        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world((ServerWorld) world)) {
            ServerPlayNetworking.send(serverPlayerEntity, payload);
        }
    }
}
