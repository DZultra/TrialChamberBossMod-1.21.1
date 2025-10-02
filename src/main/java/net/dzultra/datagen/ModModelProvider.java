package net.dzultra.datagen;

import net.dzultra.block.ModBlocks;
import net.dzultra.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.*;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        BlockStateModelGenerator.BlockTexturePool reinforced_copper_pool = blockStateModelGenerator.registerCubeAllModelTexturePool(Blocks.CUT_COPPER);

        blockStateModelGenerator.registerSingleton(ModBlocks.SPAWN_PILLAR, TexturedModel.CUBE_BOTTOM_TOP);

        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.REINFORCED_OXIDIZED_COPPER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.REINFORCED_COPPER_BLOCK);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.REINFORCED_TUFF_BRICKS);
        blockStateModelGenerator.registerSingleton(ModBlocks.REINFORCED_CHISELED_TUFF_BRICKS, TexturedModel.CUBE_COLUMN);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.REINFORCED_POLISHED_TUFF);
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.REINFORCED_OXIDIZED_CUT_COPPER);

        blockStateModelGenerator.registerCopperBulb(ModBlocks.REINFORCED_COPPER_BULB);

        blockStateModelGenerator.registerStateWithModelReference(ModBlocks.REINFORCED_CHAIN, Blocks.CHAIN);

        reinforced_copper_pool.stairs(ModBlocks.REINFORCED_CUT_COPPER_STAIRS);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.SPAWN_SHARD, Models.GENERATED);

        itemModelGenerator.register(ModItems.TRIAL_CHAMBER_BOSS_SPAWN_EGG,
                new Model(Optional.of(Identifier.of("item/template_spawn_egg")), Optional.empty()));

        itemModelGenerator.register(ModBlocks.REINFORCED_CHAIN.asItem(),
                new Model(Optional.of(Identifier.of("item/chain")), Optional.empty()));
    }
}
