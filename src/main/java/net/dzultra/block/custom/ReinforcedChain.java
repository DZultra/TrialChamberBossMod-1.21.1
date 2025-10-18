package net.dzultra.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class ReinforcedChain extends ChainBlock {
    public ReinforcedChain(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext entityShapeContext) {
            if(entityShapeContext.getEntity() instanceof PlayerEntity) {
                return VoxelShapes.empty();
            }
        }
        return super.getCollisionShape(state, world, pos, context);
    }


}
