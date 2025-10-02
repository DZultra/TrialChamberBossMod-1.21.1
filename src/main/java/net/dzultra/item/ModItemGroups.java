package net.dzultra.item;

import net.dzultra.TrialChamberBossMod;
import net.dzultra.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup TCB_MOD_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(TrialChamberBossMod.MOD_ID, "tcb_mod_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(Blocks.COPPER_BLOCK))
                    .displayName(Text.translatable("itemgroup.tcb-mod.tcb_mod_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.SPAWN_PILLAR);
                        entries.add(ModItems.TRIAL_CHAMBER_BOSS_SPAWN_EGG);
                        entries.add(ModItems.SPAWN_SHARD);
                        entries.add(ModBlocks.REINFORCED_COPPER_BLOCK);
                        entries.add(ModBlocks.REINFORCED_OXIDIZED_COPPER_BLOCK);
                        entries.add(ModBlocks.REINFORCED_OXIDIZED_CUT_COPPER);
                        entries.add(ModBlocks.REINFORCED_CUT_COPPER_STAIRS);
                        entries.add(ModBlocks.REINFORCED_COPPER_BULB);
                        entries.add(ModBlocks.REINFORCED_TUFF_BRICKS);
                        entries.add(ModBlocks.REINFORCED_CHISELED_TUFF_BRICKS);
                        entries.add(ModBlocks.REINFORCED_POLISHED_TUFF);
                        entries.add(ModBlocks.REINFORCED_CHAIN);
                    }).build());

    public static void registerModItemGroups() {

    }
}
