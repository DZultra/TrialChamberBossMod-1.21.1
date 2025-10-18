package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.block.SpawnPillarBlock.SpawnPillarBlock;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block SPAWN_PILLAR = registerBlock("spawn_pillar",
            new SpawnPillarBlock(AbstractBlock.Settings.create()
                    .nonOpaque()
                    .strength(-1.0F, 3600000.0F)
            )
    );

    public static final Block REINFORCED_COPPER_BLOCK = registerReinforcedBlock("reinforced_copper_block", Blocks.WAXED_COPPER_BLOCK);
    public static final Block REINFORCED_OXIDIZED_COPPER_BLOCK = registerReinforcedBlock("reinforced_oxidized_copper_block", Blocks.WAXED_OXIDIZED_COPPER);
    public static final Block REINFORCED_TUFF_BRICKS = registerReinforcedBlock("reinforced_tuff_bricks", Blocks.TUFF_BRICKS);
    public static final Block REINFORCED_CHISELED_TUFF_BRICKS = registerReinforcedBlock("reinforced_chiseled_tuff_bricks", Blocks.CHISELED_TUFF_BRICKS);
    public static final Block REINFORCED_POLISHED_TUFF = registerReinforcedBlock("reinforced_polished_tuff", Blocks.POLISHED_TUFF);
    public static final Block REINFORCED_COPPER_BULB = registerReinforcedBulbBlock("reinforced_copper_bulb", Blocks.WAXED_COPPER_BULB);
    public static final Block REINFORCED_CHAIN = registerReinforcedChainBlock("reinforced_chain", Blocks.CHAIN);
    public static final Block REINFORCED_CUT_COPPER_STAIRS = registerReinforcedStairsBlock("reinforced_cut_copper_stairs", Blocks.WAXED_CUT_COPPER_STAIRS);
    public static final Block REINFORCED_OXIDIZED_CUT_COPPER_STAIRS = registerReinforcedStairsBlock("reinforced_oxidized_cut_copper_stairs", Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
    public static final Block REINFORCED_OXIDIZED_CUT_COPPER = registerReinforcedBlock("reinforced_oxidized_cut_copper", Blocks.WAXED_OXIDIZED_CUT_COPPER);

    private static Block registerReinforcedBlock(String name, Block block) {
        return registerBlock(name, new Block(AbstractBlock.Settings.copy(block).strength(-1F, 3600000.0F)));
    }

    private static Block registerReinforcedBulbBlock(String name, Block block) {
        return registerBlock(name, new BulbBlock(AbstractBlock.Settings.copy(block).strength(-1F, 3600000.0F)));
    }

    private static Block registerReinforcedStairsBlock(String name, Block block) {
        return registerBlock(name, new StairsBlock(block.getDefaultState(), AbstractBlock.Settings.copy(block).strength(-1F, 3600000.0F)));
    }

    private static Block registerReinforcedChainBlock(String name, Block block) {
        return registerBlock(name, new ChainBlock(AbstractBlock.Settings.copy(block).strength(-1F, 3600000.0F).nonOpaque()));
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(TrialChamberBossMod.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(TrialChamberBossMod.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }
    public static void registerModBlocks() {}
}
