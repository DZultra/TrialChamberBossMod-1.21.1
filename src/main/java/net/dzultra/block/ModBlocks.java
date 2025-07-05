package net.dzultra.block;

import net.dzultra.TrialChamberBossMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block BOSS_SPAWN_PILLAR = registerBlock("boss_spawn_pillar",
            new BossSpawnPillarBlock(AbstractBlock.Settings.create()
                    .nonOpaque()
                    .strength(-1.0F, 3600000.0F)
            )
    );

    public static final Block SPAWN_PILLAR = registerBlock("spawn_pillar",
            new BossSpawnPillarBlock(AbstractBlock.Settings.create()
                    .nonOpaque()
                    .strength(-1.0F, 3600000.0F)
            )
    );

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(TrialChamberBossMod.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(TrialChamberBossMod.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }
}
